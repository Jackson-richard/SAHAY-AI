import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSession } from '../context/SessionContext';
import { useTranslation } from '../utils/translation';

export default function Consent() {
    const { setConsent } = useSession();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const [general, setGeneral] = useState(false);
    const [voice, setVoice] = useState(false);
    const [trusted, setTrusted] = useState(false);

    const handleConsent = () => {
        setConsent({
            isProvided: true,
            general: general,
            voice: voice,
            trusted: trusted,
            timestamp: new Date().toISOString()
        });
        navigate('/home');
    };

    return (
        <div className="flex-grow pt-[64px] pb-xl px-container-margin md:px-lg max-w-2xl mx-auto w-full flex flex-col">
            <header className="fixed top-0 left-0 w-full bg-surface flex items-center justify-center px-md h-touch-target z-50 border-b border-outline-variant">
                <span className="font-headline text-headline-lg font-bold text-primary tracking-tight">SAHAY-AI</span>
            </header>

            <div className="mt-lg md:mt-xl flex flex-col gap-sm fade-up">
                <h1 className="font-display text-display-lg text-on-surface whitespace-pre-line">Your choice.<br />Your control.</h1>
                <p className="font-body text-body-lg text-on-surface-variant max-w-prose">
                    SAHAY-AI monitors your wellbeing signals to identify concerning changes for appropriate support.
                    Wellbeing check-ins are voluntary, and AI does not provide medical or legal diagnosis.
                </p>
            </div>

            <div className="mt-lg flex flex-col gap-sm fade-up fade-up-1">
                <div className="info-card bg-surface-container-lowest rounded-xl p-md shadow-sm border border-surface-container-low flex items-start gap-md">
                    <div className="bg-surface-container-low rounded-full p-xs flex-shrink-0 text-primary mt-0.5">
                        <span className="material-symbols-outlined">front_hand</span>
                    </div>
                    <div>
                        <h2 className="font-label text-label-lg text-on-surface">CONSENT-BASED</h2>
                        <p className="font-body text-body-md text-on-surface-variant mt-base">You choose whether to participate. You can withdraw at any time.</p>
                    </div>
                </div>

                <div className="info-card bg-surface-container-lowest rounded-xl p-md shadow-sm border border-surface-container-low flex items-start gap-md">
                    <div className="bg-surface-container-low rounded-full p-xs flex-shrink-0 text-primary mt-0.5">
                        <span className="material-symbols-outlined">lock</span>
                    </div>
                    <div>
                        <h2 className="font-label text-label-lg text-on-surface">PRIVATE</h2>
                        <p className="font-body text-body-md text-on-surface-variant mt-base">Your information is protected and accessed only by authorized personnel.</p>
                    </div>
                </div>
            </div>

            <div className="mt-lg pt-sm flex flex-col gap-sm fade-up fade-up-2">
                <h3 className="font-headline text-headline-md text-on-surface mb-xs">Your Preferences</h3>

                <label className="flex items-start gap-sm cursor-pointer group p-sm bg-surface-container-lowest rounded-lg border border-outline-variant hover:bg-surface-container-low transition-colors" htmlFor="consent-general">
                    <input className="w-5 h-5 mt-0.5 rounded border-outline text-primary focus:ring-primary focus:ring-2 focus:ring-offset-2 bg-surface-container-lowest cursor-pointer flex-shrink-0" id="consent-general" type="checkbox" checked={general} onChange={(e) => setGeneral(e.target.checked)} />
                    <div className="flex-grow">
                        <span className="font-label text-label-lg text-on-surface select-none block">Required: Wellbeing Monitoring</span>
                        <span className="font-body text-body-sm text-on-surface-variant block mt-xs">I consent to periodic wellbeing check-ins and analysis of my signals.</span>
                    </div>
                </label>

                <label className="flex items-start gap-sm cursor-pointer group p-sm bg-surface-container-lowest rounded-lg border border-outline-variant hover:bg-surface-container-low transition-colors" htmlFor="consent-voice">
                    <input className="w-5 h-5 mt-0.5 rounded border-outline text-primary focus:ring-primary focus:ring-2 focus:ring-offset-2 bg-surface-container-lowest cursor-pointer flex-shrink-0" id="consent-voice" type="checkbox" checked={voice} onChange={(e) => setVoice(e.target.checked)} />
                    <div className="flex-grow">
                        <span className="font-label text-label-lg text-on-surface select-none block">Optional: Voice Check-ins</span>
                        <span className="font-body text-body-sm text-on-surface-variant block mt-xs">I consent to use my microphone for voice check-ins and adaptive voice outreach.</span>
                    </div>
                </label>

                <label className="flex items-start gap-sm cursor-pointer group p-sm bg-surface-container-lowest rounded-lg border border-outline-variant hover:bg-surface-container-low transition-colors" htmlFor="consent-trusted">
                    <input className="w-5 h-5 mt-0.5 rounded border-outline text-primary focus:ring-primary focus:ring-2 focus:ring-offset-2 bg-surface-container-lowest cursor-pointer flex-shrink-0" id="consent-trusted" type="checkbox" checked={trusted} onChange={(e) => setTrusted(e.target.checked)} />
                    <div className="flex-grow">
                        <span className="font-label text-label-lg text-on-surface select-none block">Optional: Trusted Contact Notifications</span>
                        <span className="font-body text-body-sm text-on-surface-variant block mt-xs">I consent to SAHAY sending generic wellbeing support notifications to my trusted person if needed.</span>
                    </div>
                </label>
            </div>

            <div className="mt-auto pt-xl pb-md flex flex-col gap-lg fade-up fade-up-3">
                <div className="flex flex-col gap-sm">
                    <button
                        onClick={handleConsent}
                        disabled={!general}
                        className={`btn-primary font-label text-label-lg h-touch-target rounded-full w-full flex items-center justify-center shadow-sm ${general ? 'bg-primary text-on-primary' : 'bg-surface-variant text-on-surface-variant opacity-50 cursor-not-allowed'}`}>
                        Give Consent &amp; Continue
                    </button>
                </div>
                <p className="text-center font-label text-label-sm text-on-surface-variant opacity-70 mb-lg">
                    You can change your consent preferences at any time in Profile &gt; Settings.
                </p>
            </div>
        </div>
    );
}
