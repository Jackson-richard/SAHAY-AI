import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSession } from '../context/SessionContext';
import { useTranslation } from '../utils/translation';

export default function Register() {
    const { setProfile, setCaseInfo, setPreferredLanguage, setTrustedPerson } = useSession();
    const navigate = useNavigate();
    const { t, lang } = useTranslation();

    const [form, setForm] = useState({
        caseId: '',
        fullName: '',
        mobile: '',
        age: '',
        prefLang: lang,
        trustedName: '',
        trustedMobile: '',
        trustedRelation: ''
    });
    const [error, setError] = useState(null);

    const handleChange = (e) => setForm({ ...form, [e.target.id]: e.target.value });
    const handleLangChange = (e) => {
        setForm({ ...form, prefLang: e.target.value });
        setPreferredLanguage(e.target.value);
    };

    const handleRegister = (e) => {
        e.preventDefault();
        setError(null);

        if (!form.fullName || form.fullName.trim().length < 2) return setError(t('Please enter your full name.'));
        if (!form.mobile || form.mobile.trim().length < 5) return setError(t('Please enter a valid mobile number.'));
        if (!form.age || form.age < 13 || form.age > 120) return setError(t('Please enter a valid age.'));

        // Save profile
        setProfile({
            fullName: form.fullName.trim(),
            mobileNumber: form.mobile.trim(),
            age: parseInt(form.age, 10)
        });

        // Save existing case if provided
        if (form.caseId.trim()) {
            setCaseInfo({ id: form.caseId.trim(), stage: 'registration' });
        } else {
            setCaseInfo({ id: null, stage: 'registration' });
        }

        // Save trusted person if provided
        if (form.trustedName.trim()) {
            setTrustedPerson({
                name: form.trustedName.trim(),
                phone: form.trustedMobile.trim(),
                relation: form.trustedRelation.trim()
            });
        }

        // Navigate to consent
        navigate('/consent');
    };

    return (
        <div className="flex-grow pt-[64px] pb-xl px-container-margin md:px-lg max-w-lg mx-auto w-full flex flex-col">
            <header className="fixed top-0 left-0 w-full bg-surface flex items-center justify-center px-md h-touch-target z-50 border-b border-outline-variant">
                <span className="font-headline text-headline-lg font-bold text-primary tracking-tight">SAHAY-AI</span>
            </header>

            <div className="mt-lg flex flex-col gap-xs fade-up">
                <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-primary-container text-on-primary-container mb-sm">
                    <span className="material-symbols-outlined icon-fill" style={{ fontSize: '28px' }}>person_add</span>
                </div>
                <h1 className="font-display text-display-lg text-on-surface">{t('register_title')}</h1>
                <p className="font-body text-body-lg text-on-surface-variant">Create your profile to begin wellbeing monitoring.</p>
            </div>

            <form onSubmit={handleRegister} className="mt-lg flex flex-col gap-md fade-up fade-up-2">
                <div>
                    <label className="font-label text-label-lg text-on-surface block mb-xs" htmlFor="fullName">Full Name</label>
                    <input id="fullName" value={form.fullName} onChange={handleChange} type="text" className="field-input w-full px-md py-sm border border-outline-variant rounded-xl font-body text-body-md text-on-surface bg-surface-container-lowest focus:outline-none" placeholder="Enter your full name" required />
                </div>

                <div>
                    <label className="font-label text-label-lg text-on-surface block mb-xs" htmlFor="mobile">Mobile Number</label>
                    <input id="mobile" value={form.mobile} onChange={handleChange} type="tel" className="field-input w-full px-md py-sm border border-outline-variant rounded-xl font-body text-body-md text-on-surface bg-surface-container-lowest focus:outline-none" placeholder="+91 XXXXX XXXXX" required />
                </div>

                <div>
                    <label className="font-label text-label-lg text-on-surface block mb-xs" htmlFor="age">Age</label>
                    <input id="age" value={form.age} onChange={handleChange} type="number" min="13" max="120" className="field-input w-full px-md py-sm border border-outline-variant rounded-xl font-body text-body-md text-on-surface bg-surface-container-lowest focus:outline-none" placeholder="Enter your age" required />
                </div>

                <div>
                    <label className="font-label text-label-lg text-on-surface block mb-xs" htmlFor="prefLang">Preferred Language</label>
                    <select id="prefLang" value={form.prefLang} onChange={handleLangChange} className="field-input w-full px-md py-sm border border-outline-variant rounded-xl font-body text-body-md text-on-surface bg-surface-container-lowest focus:outline-none cursor-pointer">
                        <option value="en">English</option>
                        <option value="ta">Tamil (தமிழ்)</option>
                        <option value="hi">Hindi (हिन्दी)</option>
                        <option value="ml">Malayalam (മലയാളം)</option>
                        <option value="te">Telugu (తెలుగు)</option>
                    </select>
                </div>

                <div className="pt-sm pb-xs border-t border-outline-variant mt-sm">
                    <h3 className="font-headline text-headline-md text-on-surface">Trusted Person (Optional)</h3>
                    <p className="font-body text-body-sm text-on-surface-variant mb-md mt-xs">Provide a trusted contact details if you would like someone to be notified in emergencies.</p>

                    <div className="flex flex-col gap-md">
                        <div>
                            <label className="font-label text-label-lg text-on-surface block mb-xs" htmlFor="trustedName">Their Name</label>
                            <input id="trustedName" value={form.trustedName} onChange={handleChange} type="text" className="field-input w-full px-md py-sm border border-outline-variant rounded-xl font-body text-body-md text-on-surface bg-surface-container-lowest focus:outline-none" placeholder="" />
                        </div>
                        <div>
                            <label className="font-label text-label-lg text-on-surface block mb-xs" htmlFor="trustedMobile">Their Mobile</label>
                            <input id="trustedMobile" value={form.trustedMobile} onChange={handleChange} type="tel" className="field-input w-full px-md py-sm border border-outline-variant rounded-xl font-body text-body-md text-on-surface bg-surface-container-lowest focus:outline-none" placeholder="" />
                        </div>
                        <div>
                            <label className="font-label text-label-lg text-on-surface block mb-xs" htmlFor="trustedRelation">Relationship</label>
                            <input id="trustedRelation" value={form.trustedRelation} onChange={handleChange} type="text" className="field-input w-full px-md py-sm border border-outline-variant rounded-xl font-body text-body-md text-on-surface bg-surface-container-lowest focus:outline-none" placeholder="e.g. Sister, Friend" />
                        </div>
                    </div>
                </div>

                <div className="pt-sm pb-xs border-t border-outline-variant mt-xs">
                    <h3 className="font-headline text-headline-md text-on-surface">Existing Complaint / Case ID (if available)</h3>
                    <p className="font-body text-body-sm text-on-surface-variant mb-md mt-xs">Enter the complaint/case ID provided during registration.</p>
                    <div>
                        <input id="caseId" value={form.caseId} onChange={handleChange} type="text" className="field-input w-full px-md py-sm border border-outline-variant rounded-xl font-body text-body-md text-on-surface bg-surface-container-lowest focus:outline-none" placeholder="e.g. CNR/CASE-2026-0001" />
                    </div>
                </div>

                {error && (
                    <div className="bg-error-container text-on-error-container rounded-xl p-md flex items-center gap-sm mt-xs">
                        <span className="material-symbols-outlined icon-fill text-error" style={{ fontSize: '18px' }}>error</span>
                        <span className="font-body text-body-md">{error}</span>
                    </div>
                )}

                <button type="submit" className="btn-primary bg-primary text-on-primary font-label text-label-lg h-touch-target rounded-full w-full flex items-center justify-center gap-xs shadow-sm mt-sm mb-lg">
                    <span>Continue to Consent</span>
                    <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_forward</span>
                </button>
            </form>
        </div>
    );
}
