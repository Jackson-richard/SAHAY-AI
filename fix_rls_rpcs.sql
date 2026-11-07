-- RPC to securely fetch check-ins for the victim's session
CREATE OR REPLACE FUNCTION public.get_victim_checkins(p_session_token UUID)
RETURNS JSONB
SET search_path = public, pg_temp
LANGUAGE plpgsql SECURITY DEFINER
AS $$
DECLARE
    v_case_id TEXT;
    result JSONB;
BEGIN
    SELECT case_id INTO v_case_id FROM public.profiles WHERE id = p_session_token;
    IF v_case_id IS NULL THEN
        RETURN '[]'::jsonb;
    END IF;

    SELECT COALESCE(jsonb_agg(to_jsonb(t)), '[]'::jsonb) INTO result
    FROM (
        SELECT *
        FROM public.check_ins
        WHERE case_id = v_case_id
        ORDER BY created_at ASC
    ) t;

    RETURN result;
END;
$$;

-- RPC to securely fetch chat messages for the victim's session
CREATE OR REPLACE FUNCTION public.get_victim_chat_history(p_session_token UUID)
RETURNS JSONB
SET search_path = public, pg_temp
LANGUAGE plpgsql SECURITY DEFINER
AS $$
DECLARE
    v_case_id TEXT;
    result JSONB;
BEGIN
    SELECT case_id INTO v_case_id FROM public.profiles WHERE id = p_session_token;
    IF v_case_id IS NULL THEN
        RETURN '[]'::jsonb;
    END IF;

    SELECT COALESCE(jsonb_agg(to_jsonb(t)), '[]'::jsonb) INTO result
    FROM (
        SELECT *
        FROM public.chat_messages
        WHERE case_id = v_case_id
        ORDER BY created_at ASC
    ) t;

    RETURN result;
END;
$$;
