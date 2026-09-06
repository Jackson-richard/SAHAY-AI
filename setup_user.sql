DO $$ 
DECLARE
  v_user_id UUID := gen_random_uuid();
BEGIN
  -- Insert into auth.users using pgcrypto for password
  INSERT INTO auth.users (
    instance_id, id, aud, role, email, encrypted_password, email_confirmed_at, 
    created_at, updated_at, confirmation_token, email_change, email_change_token_new, recovery_token
  )
  SELECT 
    '00000000-0000-0000-0000-000000000000', 
    v_user_id, 
    'authenticated', 
    'authenticated', 
    'demo@sahay.gov.in', 
    crypt('admin123', gen_salt('bf')), 
    now(), 
    now(), 
    now(), 
    '', 
    '', 
    '', 
    ''
  WHERE NOT EXISTS (
    SELECT 1 FROM auth.users WHERE email = 'demo@sahay.gov.in'
  );

  -- Get the actual ID whether newly created or existing
  SELECT id INTO v_user_id FROM auth.users WHERE email = 'demo@sahay.gov.in';

  -- Insert into public.admin_users
  INSERT INTO public.admin_users (id, email, department, role, created_at) 
  VALUES (v_user_id, 'demo@sahay.gov.in', 'System Admin', 'admin', now())
  ON CONFLICT (id) DO NOTHING;
END $$;

NOTIFY pgrst, 'reload schema';
