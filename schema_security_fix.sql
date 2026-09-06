-- Undo unsafe anonymous profile policies
DROP POLICY IF EXISTS "Anon select profile" ON public.profiles;
DROP POLICY IF EXISTS "Anon insert profile" ON public.profiles;
DROP POLICY IF EXISTS "Anon update profile" ON public.profiles;
DROP POLICY IF EXISTS "Anon case validation" ON public.cases;

-- Restore strict profiles policy (only admins read profiles)
CREATE POLICY "Admins read profiles" ON public.profiles FOR SELECT USING (public.is_admin());

-- Secure RPC to validate case existence without returning case rows
CREATE OR REPLACE FUNCTION public.validate_case_existence(p_case_id TEXT)
RETURNS BOOLEAN
LANGUAGE plpgsql SECURITY DEFINER
AS $$
BEGIN
    RETURN EXISTS (SELECT 1 FROM public.cases WHERE case_id = p_case_id);
END;
$$;

-- Secure RPC for victim registration
CREATE OR REPLACE FUNCTION public.register_victim(
    p_case_id TEXT,
    p_name TEXT,
    p_phone TEXT,
    p_trusted_phone TEXT,
    p_language TEXT,
    p_consent_given BOOLEAN
) RETURNS JSONB
LANGUAGE plpgsql SECURITY DEFINER
AS $$
DECLARE
    case_exists BOOLEAN;
    already_registered BOOLEAN;
BEGIN
    SELECT EXISTS(SELECT 1 FROM public.cases WHERE case_id = p_case_id) INTO case_exists;
    IF NOT case_exists THEN
        RETURN jsonb_build_object('success', false, 'error', 'Case ID does not exist.');
    END IF;

    SELECT EXISTS(SELECT 1 FROM public.profiles WHERE case_id = p_case_id) INTO already_registered;
    IF already_registered THEN
        RETURN jsonb_build_object('success', false, 'error', 'This case is already registered. Please contact an authorized officer for assistance.');
    END IF;

    INSERT INTO public.profiles (
        case_id, name, phone, trusted_person_mobile, preferred_language, consent_given, consent_given_at
    ) VALUES (
        p_case_id, p_name, p_phone, p_trusted_phone, p_language, p_consent_given,
        CASE WHEN p_consent_given THEN now() ELSE null END
    );

    RETURN jsonb_build_object('success', true);
END;
$$;

-- Secure RPC to fetch victim dashboard data matching case_id (read only, bypasses RLS safely)
CREATE OR REPLACE FUNCTION public.get_victim_dashboard(p_case_id TEXT)
RETURNS JSONB
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
    WHERE p.case_id = p_case_id;

    RETURN COALESCE(result, '{}'::jsonb);
END;
$$;
