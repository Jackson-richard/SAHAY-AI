-- Strictly secure unauthenticated execution
-- All SECURITY DEFINER functions MUST set search_path
-- And we must ensure only those who need them can call them. But they are used by anon.

-- 1. Drop all insecure anon policies
DO $$ 
BEGIN
    DROP POLICY IF EXISTS "Anon victims submit checkins" ON public.check_ins;
    DROP POLICY IF EXISTS "Anon victims read checkins" ON public.check_ins;
    DROP POLICY IF EXISTS "Anon victims read messages" ON public.chat_messages;
    DROP POLICY IF EXISTS "Anon victims insert messages" ON public.chat_messages;
END $$;

-- 2. Validate Case Existence
CREATE OR REPLACE FUNCTION public.validate_case_existence(p_case_id TEXT)
RETURNS BOOLEAN
SET search_path = public, pg_temp
LANGUAGE plpgsql SECURITY DEFINER
AS $$
BEGIN
    RETURN EXISTS (SELECT 1 FROM public.cases WHERE case_id = p_case_id);
END;
$$;

-- 3. Register Victim and Return Session Token (which maps to profiles.id safely defaulted to gen_random_uuid)
CREATE OR REPLACE FUNCTION public.register_victim(
    p_case_id TEXT,
    p_name TEXT,
    p_phone TEXT,
    p_trusted_phone TEXT,
    p_language TEXT,
    p_consent_given BOOLEAN
) RETURNS JSONB
SET search_path = public, pg_temp
LANGUAGE plpgsql SECURITY DEFINER
AS $$
DECLARE
    case_exists BOOLEAN;
    already_registered BOOLEAN;
    new_session_token UUID;
BEGIN
    SELECT EXISTS(SELECT 1 FROM public.cases WHERE case_id = p_case_id) INTO case_exists;
    IF NOT case_exists THEN
        RETURN jsonb_build_object('success', false, 'error', 'Case ID does not exist.');
    END IF;

    SELECT EXISTS(SELECT 1 FROM public.profiles WHERE case_id = p_case_id) INTO already_registered;
    IF already_registered THEN
        RETURN jsonb_build_object('success', false, 'error', 'This case is already registered. Please contact an authorized officer for assistance.');
    END IF;

    new_session_token := gen_random_uuid();

    INSERT INTO public.profiles (
        id, case_id, name, phone, trusted_person_mobile, preferred_language, consent_given, consent_given_at
    ) VALUES (
        new_session_token, p_case_id, p_name, p_phone, p_trusted_phone, p_language, p_consent_given,
        CASE WHEN p_consent_given THEN now() ELSE null END
    );

    RETURN jsonb_build_object('success', true, 'session_token', new_session_token);
END;
$$;

-- 4. Get Victim Dashboard via Session Token
CREATE OR REPLACE FUNCTION public.get_victim_dashboard(p_session_token UUID)
RETURNS JSONB
SET search_path = public, pg_temp
LANGUAGE plpgsql SECURITY DEFINER
AS $$
DECLARE
    result JSONB;
BEGIN
    SELECT jsonb_build_object(
        'profile', to_jsonb(p),
        'case_record', to_jsonb(c)
    ) INTO result
    FROM public.profiles p
    JOIN public.cases c ON c.case_id = p.case_id
    WHERE p.id = p_session_token;

    RETURN COALESCE(result, '{}'::jsonb);
END;
$$;

-- 5. Submit Check In via Session Token
CREATE OR REPLACE FUNCTION public.submit_victim_checkin(
    p_session_token UUID,
    p_score INT,
    p_answers JSONB
)
RETURNS JSONB
SET search_path = public, pg_temp
LANGUAGE plpgsql SECURITY DEFINER
AS $$
DECLARE
    v_case_id TEXT;
    v_period TEXT;
BEGIN
    -- Authenticate session
    SELECT case_id INTO v_case_id FROM public.profiles WHERE id = p_session_token;
    IF v_case_id IS NULL THEN
        RETURN jsonb_build_object('success', false, 'error', 'Invalid session');
    END IF;

    -- Dynamically generate 7-day period ID on backend to prevent client spoofing
    v_period := 'WEEK_' || CAST(TRUNC(EXTRACT(EPOCH FROM now()) / 604800) AS TEXT);

    INSERT INTO public.check_ins (
        case_id, assessment_period_id, assessment_day, score, answers
    ) VALUES (
        v_case_id, v_period, CURRENT_DATE, p_score, p_answers
    );
    
    RETURN jsonb_build_object('success', true);
END;
$$;

