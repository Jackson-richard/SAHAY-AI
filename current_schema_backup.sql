-- IDEMPOTENT COMPLETE ARCHITECTURE SCHEMA WITH STRICT RLS & AUTHENTICATION (SAHAY-AI)
-- Execute this script in the Supabase SQL Editor.
-- This script safely preserves existing data and migrates structures.

-- 1. Create Tables Safely
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    case_id TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    role TEXT DEFAULT 'victim' CHECK (role IN ('victim', 'admin')),
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.admin_users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    department TEXT NOT NULL,
    role TEXT DEFAULT 'admin',
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.cases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id TEXT UNIQUE NOT NULL REFERENCES public.profiles(case_id) ON DELETE CASCADE,
    status TEXT NOT NULL,
    priority TEXT NOT NULL,
    department TEXT NOT NULL,
    support_category TEXT NOT NULL,
    official_note TEXT,
    assessment_progress_days INTEGER DEFAULT 0,
    current_distress_score INTEGER DEFAULT 0,
    final_weekly_score INTEGER,
    score_status TEXT DEFAULT 'IN_PROGRESS', 
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.check_ins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id TEXT NOT NULL REFERENCES public.cases(case_id) ON DELETE CASCADE,
    assessment_period_id TEXT NOT NULL,
    assessment_day DATE NOT NULL DEFAULT CURRENT_DATE,
    score INTEGER NOT NULL,
    mood_level INTEGER,
    fear_level INTEGER,
    safety_level INTEGER,
    answers JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id TEXT NOT NULL REFERENCES public.cases(case_id) ON DELETE CASCADE,
    assessment_period_id TEXT NOT NULL,
    assessment_day DATE NOT NULL DEFAULT CURRENT_DATE,
    sender TEXT NOT NULL,
    message TEXT NOT NULL,
    ai_analysis_summary JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.trusted_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id TEXT NOT NULL REFERENCES public.cases(case_id) ON DELETE CASCADE,
    contact_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    relationship TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.automated_followups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id TEXT NOT NULL REFERENCES public.cases(case_id) ON DELETE CASCADE,
    followup_type TEXT NOT NULL,
    status TEXT NOT NULL,
    scheduled_for TIMESTAMPTZ NOT NULL,
    result_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.distress_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id TEXT NOT NULL REFERENCES public.cases(case_id) ON DELETE CASCADE,
    final_score INTEGER NOT NULL,
    risk_level TEXT NOT NULL,
    reasoning JSONB,
    status TEXT DEFAULT 'OPEN',
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.interventions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_id UUID REFERENCES public.distress_alerts(id) ON DELETE CASCADE,
    case_id TEXT NOT NULL REFERENCES public.cases(case_id) ON DELETE CASCADE,
    action_taken TEXT NOT NULL,
    officer_id UUID NOT NULL REFERENCES auth.users(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id TEXT NOT NULL REFERENCES public.cases(case_id) ON DELETE CASCADE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    recorded_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID NOT NULL REFERENCES auth.users(id),
    action_type TEXT NOT NULL,
    target_case_id TEXT,
    details JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Safe Non-Destructive Migrations 
-- Ensures existing tables gain the new requirement pillars without dropping their core contents
ALTER TABLE public.cases 
  ADD COLUMN IF NOT EXISTS assessment_progress_days INTEGER DEFAULT 0,
  ADD COLUMN IF NOT EXISTS current_distress_score INTEGER DEFAULT 0,
  ADD COLUMN IF NOT EXISTS final_weekly_score INTEGER,
  ADD COLUMN IF NOT EXISTS score_status TEXT DEFAULT 'IN_PROGRESS';

ALTER TABLE public.check_ins 
  ADD COLUMN IF NOT EXISTS assessment_period_id TEXT NOT NULL DEFAULT 'DEFAULT',
  ADD COLUMN IF NOT EXISTS assessment_day DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE public.chat_messages 
  ADD COLUMN IF NOT EXISTS assessment_period_id TEXT NOT NULL DEFAULT 'DEFAULT',
  ADD COLUMN IF NOT EXISTS assessment_day DATE NOT NULL DEFAULT CURRENT_DATE,
  ADD COLUMN IF NOT EXISTS ai_analysis_summary JSONB;

-- Safely Create Unique Index to prevent duplicate identical escalation alerts
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'unique_active_distress_alert' AND n.nspname = 'public'
    ) THEN
        CREATE UNIQUE INDEX unique_active_distress_alert
        ON public.distress_alerts(case_id, risk_level)
        WHERE status = 'OPEN';
    END IF;
END $$;


-- 3. ENABLE RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cases ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.check_ins ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chat_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.trusted_contacts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.automated_followups ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.distress_alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.interventions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.locations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;


-- 4. Define Admin Helper
CREATE OR REPLACE FUNCTION public.is_admin() RETURNS BOOLEAN 
SET search_path = public, pg_temp
AS $$
BEGIN
  RETURN EXISTS (
    SELECT 1 FROM public.admin_users WHERE id = auth.uid()
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- 5. Idempotently Apply Explicit Ownership/Admin Policies
DO $$
BEGIN
    DROP POLICY IF EXISTS "Victims read own profile" ON public.profiles;
    DROP POLICY IF EXISTS "Admins read own row" ON public.admin_users;
    DROP POLICY IF EXISTS "Admins read admin_users" ON public.admin_users;
    DROP POLICY IF EXISTS "Victims read own cases" ON public.cases;
    DROP POLICY IF EXISTS "Admins update cases" ON public.cases;
    DROP POLICY IF EXISTS "Victims submit own checkins" ON public.check_ins;
    DROP POLICY IF EXISTS "Victims read own checkins" ON public.check_ins;
    DROP POLICY IF EXISTS "Victims read own messages" ON public.chat_messages;
    DROP POLICY IF EXISTS "Victims insert own messages" ON public.chat_messages;
    DROP POLICY IF EXISTS "Victims read own contacts" ON public.trusted_contacts;
    DROP POLICY IF EXISTS "Victims read own followups" ON public.automated_followups;
    DROP POLICY IF EXISTS "Read distress alerts" ON public.distress_alerts;
    DROP POLICY IF EXISTS "Admin read interventions" ON public.interventions;
    DROP POLICY IF EXISTS "Admin insert interventions" ON public.interventions;
    DROP POLICY IF EXISTS "Victims log locations" ON public.locations;
    DROP POLICY IF EXISTS "Audit logs internal" ON public.audit_logs;
END $$;

-- Policy fixes specific recursion loop: admins strictly only need `id = auth.uid()` to identify globally.
CREATE POLICY "Victims read own profile" ON public.profiles FOR SELECT USING (id = auth.uid() OR public.is_admin());
CREATE POLICY "Admins read own row" ON public.admin_users FOR SELECT USING (id = auth.uid()); 
CREATE POLICY "Victims read own cases" ON public.cases FOR SELECT USING (
    case_id IN (SELECT case_id FROM public.profiles WHERE id = auth.uid()) OR public.is_admin()
);
CREATE POLICY "Admins update cases" ON public.cases FOR UPDATE USING (public.is_admin());
CREATE POLICY "Victims submit own checkins" ON public.check_ins FOR INSERT WITH CHECK (
    case_id IN (SELECT case_id FROM public.profiles WHERE id = auth.uid())
);
CREATE POLICY "Victims read own checkins" ON public.check_ins FOR SELECT USING (
    case_id IN (SELECT case_id FROM public.profiles WHERE id = auth.uid()) OR public.is_admin()
);
CREATE POLICY "Victims read own messages" ON public.chat_messages FOR SELECT USING (
    case_id IN (SELECT case_id FROM public.profiles WHERE id = auth.uid()) OR public.is_admin()
);
CREATE POLICY "Victims insert own messages" ON public.chat_messages FOR INSERT WITH CHECK (
    case_id IN (SELECT case_id FROM public.profiles WHERE id = auth.uid())
);
CREATE POLICY "Victims read own contacts" ON public.trusted_contacts FOR SELECT USING (
    case_id IN (SELECT case_id FROM public.profiles WHERE id = auth.uid()) OR public.is_admin()
);
CREATE POLICY "Victims read own followups" ON public.automated_followups FOR SELECT USING (
    case_id IN (SELECT case_id FROM public.profiles WHERE id = auth.uid()) OR public.is_admin()
);
CREATE POLICY "Read distress alerts" ON public.distress_alerts FOR SELECT USING (
    case_id IN (SELECT case_id FROM public.profiles WHERE id = auth.uid()) OR public.is_admin()
);
CREATE POLICY "Admin read interventions" ON public.interventions FOR SELECT USING (public.is_admin());
CREATE POLICY "Admin insert interventions" ON public.interventions FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Victims log locations" ON public.locations FOR INSERT WITH CHECK (
    case_id IN (SELECT case_id FROM public.profiles WHERE id = auth.uid())
);
CREATE POLICY "Audit logs internal" ON public.audit_logs FOR SELECT USING (public.is_admin());


-- 6. MULTI-FACTOR 7-DAY SCORING POSTGRES TRIGGER --
CREATE OR REPLACE FUNCTION public.process_seven_day_assessment()
RETURNS TRIGGER 
SET search_path = public, pg_temp
AS $$
DECLARE
    total_distinct_checkin_days INTEGER := 0;
    base_checkin_score INTEGER := 0;
    chat_penalty INTEGER := 0;
    calc_final_score INTEGER := 0;
    immediate_crisis BOOLEAN := false;
BEGIN
    SELECT COUNT(DISTINCT assessment_day) INTO total_distinct_checkin_days 
    FROM public.check_ins 
    WHERE case_id = NEW.case_id AND assessment_period_id = NEW.assessment_period_id;
    
    SELECT COALESCE(AVG(daily_avg), 0) INTO base_checkin_score FROM (
        SELECT AVG(score) as daily_avg FROM public.check_ins 
        WHERE case_id = NEW.case_id AND assessment_period_id = NEW.assessment_period_id
        GROUP BY assessment_day
    ) AS daily_data;

    SELECT COALESCE(SUM(day_penalty), 0) INTO chat_penalty FROM (
       SELECT 
         GREATEST(
            MAX(CASE WHEN (ai_analysis_summary->>'feeling_unsafe')::boolean = true THEN 20 ELSE 0 END),
            MAX(CASE WHEN (ai_analysis_summary->>'emotional_distress_level')::float > 8 THEN 15 ELSE 0 END)
         ) as day_penalty
       FROM public.chat_messages
       WHERE case_id = NEW.case_id AND assessment_period_id = NEW.assessment_period_id
       GROUP BY assessment_day
    ) as daily_penalties;

    IF chat_penalty > 40 THEN chat_penalty = 40; END IF;
    calc_final_score := LEAST(GREATEST(base_checkin_score + chat_penalty, 0), 100);

    -- Instant Crisis Evaluation (AI Override before 7 days)
    SELECT EXISTS (
       SELECT 1 FROM public.chat_messages 
       WHERE case_id = NEW.case_id AND assessment_period_id = NEW.assessment_period_id
       AND ((ai_analysis_summary->>'urgency') = 'high' OR (ai_analysis_summary->>'feeling_unsafe')::boolean = true)
    ) INTO immediate_crisis;

    IF immediate_crisis THEN
       INSERT INTO public.distress_alerts (case_id, final_score, risk_level, reasoning, status)
       VALUES (NEW.case_id, calc_final_score, 'CRITICAL', '{"reason": "Immediate emergency detected from unstructured chat context"}', 'OPEN')
       ON CONFLICT (case_id, risk_level) WHERE status='OPEN' DO NOTHING;
    END IF;

    -- Progressive Scoring Updates
    IF total_distinct_checkin_days < 7 THEN
        UPDATE public.cases 
        SET current_distress_score = calc_final_score, 
            assessment_progress_days = total_distinct_checkin_days,
            score_status = 'IN_PROGRESS'
        WHERE case_id = NEW.case_id; 
    ELSE
        UPDATE public.cases 
        SET current_distress_score = calc_final_score,
            final_weekly_score = calc_final_score,
            assessment_progress_days = total_distinct_checkin_days,
            score_status = 'COMPLETED'
        WHERE case_id = NEW.case_id;

        IF calc_final_score > 75 THEN
            INSERT INTO public.distress_alerts (case_id, final_score, risk_level, reasoning, status)
            VALUES (NEW.case_id, calc_final_score, 'HIGH', '{"reason": "7-day baseline assessment threshold exceeded 75 points"}', 'OPEN')
            ON CONFLICT (case_id, risk_level) WHERE status='OPEN' DO NOTHING;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 7. Tracking Hooks bound tightly to insertion actions Updates
DROP TRIGGER IF EXISTS checkin_assessment_trigger ON public.check_ins;
CREATE TRIGGER checkin_assessment_trigger AFTER INSERT OR UPDATE ON public.check_ins FOR EACH ROW EXECUTE FUNCTION process_seven_day_assessment();

DROP TRIGGER IF EXISTS chat_assessment_trigger ON public.chat_messages;
CREATE TRIGGER chat_assessment_trigger 
AFTER INSERT OR UPDATE OF ai_analysis_summary 
ON public.chat_messages 
FOR EACH ROW EXECUTE FUNCTION process_seven_day_assessment();
