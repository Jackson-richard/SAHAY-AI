CREATE OR REPLACE FUNCTION public.compute_distress_score(
    p_analysis JSONB,
    p_text TEXT,
    p_prev_score INT
) RETURNS INT AS $$
DECLARE
    emo_val FLOAT;
    fear_val FLOAT;
    unsafe_val BOOLEAN;
    emo_score INT;
    fear_score INT;
    safety_score INT;
    engage_score INT;
    base_score INT;
    change_score INT;
    final_score INT;
BEGIN
    IF p_analysis IS NULL THEN RETURN p_prev_score; END IF;
    
    emo_val := COALESCE((p_analysis->>'emotional_distress_level')::FLOAT, 0);
    fear_val := COALESCE((p_analysis->>'fear_level')::FLOAT, 0);
    unsafe_val := COALESCE((p_analysis->>'feeling_unsafe')::BOOLEAN, false);
    
    emo_score := ROUND((emo_val / 10.0) * 40.0);
    fear_score := ROUND((fear_val / 10.0) * 25.0);
    safety_score := CASE WHEN unsafe_val THEN 15 ELSE 0 END;
    
    engage_score := ROUND((LEAST(LENGTH(COALESCE(p_text, '')), 100) / 100.0) * 10);
    
    base_score := emo_score + fear_score + safety_score + engage_score;
    change_score := CASE WHEN base_score > p_prev_score THEN 10 ELSE 0 END;
    
    final_score := LEAST(GREATEST(base_score + change_score, 0), 100);
    RETURN final_score;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION public.set_check_in_score()
RETURNS TRIGGER 
SET search_path = public, pg_temp
AS $$
DECLARE
    prev_score INT;
    emo_val FLOAT; fear_val FLOAT; unsafe_val BOOLEAN;
    emo_score INT; fear_score INT; safety_score INT; engage_score INT;
BEGIN
    SELECT current_distress_score INTO prev_score FROM public.cases WHERE case_id = NEW.case_id;
    prev_score := COALESCE(prev_score, 0);
    
    NEW.score := public.compute_distress_score(NEW.answers, COALESCE(NEW.answers->>'text', ''), prev_score);
    
    emo_val := COALESCE((NEW.answers->>'emotional_distress_level')::FLOAT, 0);
    fear_val := COALESCE((NEW.answers->>'fear_level')::FLOAT, 0);
    unsafe_val := COALESCE((NEW.answers->>'feeling_unsafe')::BOOLEAN, false);
    
    emo_score := ROUND((emo_val / 10.0) * 40.0);
    fear_score := ROUND((fear_val / 10.0) * 25.0);
    safety_score := CASE WHEN unsafe_val THEN 15 ELSE 0 END;
    engage_score := ROUND((LEAST(LENGTH(COALESCE(NEW.answers->>'text', '')), 100) / 100.0) * 10);

    -- Populate missing metadata to match Android's structure
    NEW.answers := NEW.answers || jsonb_build_object(
        'emotional', emo_score::TEXT,
        'fear', fear_score::TEXT,
        'safety', safety_score::TEXT,
        'engagement', engage_score::TEXT,
        'change', CASE WHEN (emo_score + fear_score + safety_score + engage_score) > prev_score THEN '10' ELSE '0' END,
        'risk_level', CASE 
            WHEN NEW.score >= 75 THEN 'Critical'
            WHEN NEW.score >= 50 THEN 'High'
            WHEN NEW.score >= 25 THEN 'Moderate'
            ELSE 'Low' END
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.process_interaction_assessment()
RETURNS TRIGGER 
SET search_path = public, pg_temp
AS $$
DECLARE
    prev_score INT;
    calc_score INT;
    analysis_json JSONB;
    text_content TEXT;
    is_chat BOOLEAN := false;
BEGIN
    IF TG_TABLE_NAME = 'chat_messages' THEN
        is_chat := true;
    END IF;

    SELECT current_distress_score INTO prev_score FROM public.cases WHERE case_id = NEW.case_id;
    prev_score := COALESCE(prev_score, 0);

    IF is_chat THEN
        IF NEW.sender <> 'User' OR NEW.ai_analysis_summary IS NULL THEN
            RETURN NEW;
        END IF;
        analysis_json := NEW.ai_analysis_summary;
        text_content := NEW.message;
        calc_score := public.compute_distress_score(analysis_json, text_content, prev_score);
    ELSE
        calc_score := NEW.score;
        analysis_json := NEW.answers;
    END IF;

    UPDATE public.cases 
    SET current_distress_score = calc_score,
        assessment_progress_days = assessment_progress_days + (CASE WHEN NOT is_chat THEN 1 ELSE 0 END)
    WHERE case_id = NEW.case_id;

    IF (analysis_json->>'urgency') = 'high' OR (analysis_json->>'feeling_unsafe')::boolean = true THEN
       INSERT INTO public.distress_alerts (case_id, final_score, risk_level, reasoning, status)
       VALUES (NEW.case_id, calc_score, 'CRITICAL', '{"reason": "Immediate emergency detected from unstructured context"}', 'OPEN')
       ON CONFLICT (case_id, risk_level) WHERE status='OPEN' DO NOTHING;
    END IF;
    
    IF calc_score >= 75 THEN
        INSERT INTO public.distress_alerts (case_id, final_score, risk_level, reasoning, status)
        VALUES (NEW.case_id, calc_score, 'HIGH', '{"reason": "Score reached High Risk"}', 'OPEN')
        ON CONFLICT (case_id, risk_level) WHERE status='OPEN' DO NOTHING;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS checkin_score_trigger ON public.check_ins;
CREATE TRIGGER checkin_score_trigger BEFORE INSERT OR UPDATE ON public.check_ins FOR EACH ROW EXECUTE FUNCTION public.set_check_in_score();

DROP TRIGGER IF EXISTS checkin_assessment_trigger ON public.check_ins;
CREATE TRIGGER checkin_assessment_trigger AFTER INSERT OR UPDATE ON public.check_ins FOR EACH ROW EXECUTE FUNCTION public.process_interaction_assessment();

DROP TRIGGER IF EXISTS chat_assessment_trigger ON public.chat_messages;
CREATE TRIGGER chat_assessment_trigger AFTER INSERT OR UPDATE OF ai_analysis_summary ON public.chat_messages FOR EACH ROW EXECUTE FUNCTION public.process_interaction_assessment();
