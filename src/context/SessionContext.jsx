import React, { createContext, useContext, useState, useEffect } from 'react';

// Unified Application/Session State Context
const SessionContext = createContext();

export function useSession() {
    return useContext(SessionContext);
}

export function SessionProvider({ children }) {
    // Try to load initial state from localStorage (Demo first)
    const loadState = (key, fallback) => {
        try {
            const item = localStorage.getItem(`sahay_${key}`);
            return item ? JSON.parse(item) : fallback;
        } catch (error) {
            return fallback;
        }
    };

    // State Definitions matching requirements
    const [profile, setProfile] = useState(() => loadState('profile', null)); // { fullName, mobileNumber, age }
    const [caseInfo, setCaseInfo] = useState(() => loadState('case', null)); // { id }
    const [consent, setConsent] = useState(() => loadState('consent', null)); // { isProvided: boolean, timestamp: string }
    const [preferredLanguage, setPreferredLanguage] = useState(() => loadState('preferredLanguage', 'en'));
    const [trustedPerson, setTrustedPerson] = useState(() => loadState('trustedPerson', null)); // { name, phone }

    const [checkins, setCheckins] = useState(() => loadState('checkins', []));
    const [distressScore, setDistressScore] = useState(() => loadState('distressScore', null));
    const [trend, setTrend] = useState(() => loadState('trend', 'Stable')); // 'Stable', 'Up', 'Down'
    const [concernType, setConcernType] = useState(() => loadState('concernType', 'Wellbeing')); // Wellbeing, Safety, Other
    const [concernLevel, setConcernLevel] = useState(() => loadState('concernLevel', 'Stable')); // Stable, Elevated, Increasing Concern
    const [supportRouting, setSupportRouting] = useState(() => loadState('supportRouting', null));

    // Sync to local storage dynamically whenever state changes (Fallback / Demo persistence)
    useEffect(() => {
        if (profile) localStorage.setItem('sahay_profile', JSON.stringify(profile));
        else localStorage.removeItem('sahay_profile');
    }, [profile]);

    useEffect(() => {
        if (caseInfo) localStorage.setItem('sahay_case', JSON.stringify(caseInfo));
        else localStorage.removeItem('sahay_case');
    }, [caseInfo]);

    useEffect(() => {
        if (consent) localStorage.setItem('sahay_consent', JSON.stringify(consent));
        else localStorage.removeItem('sahay_consent');
    }, [consent]);

    useEffect(() => {
        localStorage.setItem('sahay_preferredLanguage', JSON.stringify(preferredLanguage));
    }, [preferredLanguage]);

    useEffect(() => {
        if (trustedPerson) localStorage.setItem('sahay_trustedPerson', JSON.stringify(trustedPerson));
        else localStorage.removeItem('sahay_trustedPerson');
    }, [trustedPerson]);

    useEffect(() => {
        localStorage.setItem('sahay_checkins', JSON.stringify(checkins));
    }, [checkins]);

    useEffect(() => {
        if (distressScore !== null) localStorage.setItem('sahay_distressScore', JSON.stringify(distressScore));
        else localStorage.removeItem('sahay_distressScore');
    }, [distressScore]);

    useEffect(() => {
        localStorage.setItem('sahay_trend', JSON.stringify(trend));
    }, [trend]);

    useEffect(() => {
        localStorage.setItem('sahay_concernType', JSON.stringify(concernType));
    }, [concernType]);

    useEffect(() => {
        localStorage.setItem('sahay_concernLevel', JSON.stringify(concernLevel));
    }, [concernLevel]);

    useEffect(() => {
        if (supportRouting) localStorage.setItem('sahay_supportRouting', JSON.stringify(supportRouting));
        else localStorage.removeItem('sahay_supportRouting');
    }, [supportRouting]);

    // Expose the state and setters
    const value = {
        profile, setProfile,
        caseInfo, setCaseInfo,
        consent, setConsent,
        preferredLanguage, setPreferredLanguage,
        trustedPerson, setTrustedPerson,
        checkins, setCheckins,
        distressScore, setDistressScore,
        trend, setTrend,
        concernType, setConcernType,
        concernLevel, setConcernLevel,
        supportRouting, setSupportRouting
    };

    return (
        <SessionContext.Provider value={value}>
            {children}
        </SessionContext.Provider>
    );
}
