-- Supabase schema for SAHAY-AI MVP

-- Create cases table
CREATE TABLE IF NOT EXISTS public.cases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    official_case_id VARCHAR(50) NOT NULL UNIQUE,
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,
    case_stage VARCHAR(50) NOT NULL DEFAULT 'registration',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- Create profiles table linked to cases
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_id UUID REFERENCES public.cases(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    language_pref VARCHAR(10) DEFAULT 'en',
    consent_general BOOLEAN DEFAULT FALSE,
    consent_voice BOOLEAN DEFAULT FALSE,
    consent_trusted_contact BOOLEAN DEFAULT FALSE,
    is_demo BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- Create checkins table
CREATE TABLE IF NOT EXISTS public.checkins (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    case_id UUID REFERENCES public.cases(id) ON DELETE CASCADE,
    chat_log JSONB,
    score INTEGER NOT NULL,
    concern_level VARCHAR(50) NOT NULL, -- 'stable', 'increasing', 'critical'
    signals JSONB, -- Array of string signals
    safety_concern BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- Create alerts table
CREATE TABLE IF NOT EXISTS public.alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    checkin_id UUID REFERENCES public.checkins(id) ON DELETE CASCADE,
    case_id UUID REFERENCES public.cases(id) ON DELETE CASCADE,
    alert_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'pending', -- 'pending', 'reviewed', 'resolved'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()),
    resolved_at TIMESTAMP WITH TIME ZONE
);

-- Enable RLS
ALTER TABLE public.cases ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.checkins ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.alerts ENABLE ROW LEVEL SECURITY;

-- Create policies (for demo MVP, assuming anonymous key has read/write if authenticated by matching case_id or similar, but for simplicity here we might allow anon access temporarily or define basic rules)
-- A production system would match auth.uid() to a user mapping.
-- For hackathon demo, we will allow open read/write to allow the client to function without complex Auth. 
CREATE POLICY "Allow anonymous read checkins" ON public.checkins FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anonymous insert checkins" ON public.checkins FOR INSERT TO anon WITH CHECK (true);

CREATE POLICY "Allow anonymous read cases" ON public.cases FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anonymous insert cases" ON public.cases FOR INSERT TO anon WITH CHECK (true);

CREATE POLICY "Allow anonymous read profiles" ON public.profiles FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anonymous insert profiles" ON public.profiles FOR INSERT TO anon WITH CHECK (true);
CREATE POLICY "Allow anonymous update profiles" ON public.profiles FOR UPDATE TO anon USING (true) WITH CHECK (true);

CREATE POLICY "Allow anonymous read alerts" ON public.alerts FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anonymous update alerts" ON public.alerts FOR UPDATE TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anonymous insert alerts" ON public.alerts FOR INSERT TO anon WITH CHECK (true);

-- Create interventions table
CREATE TABLE IF NOT EXISTS public.interventions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_id UUID REFERENCES public.cases(id) ON DELETE CASCADE,
    profile_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    action VARCHAR(255) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.interventions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow anonymous read interventions" ON public.interventions FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anonymous insert interventions" ON public.interventions FOR INSERT TO anon WITH CHECK (true);
CREATE POLICY "Allow anonymous update interventions" ON public.interventions FOR UPDATE TO anon USING (true) WITH CHECK (true);
