import { useSession } from '../context/SessionContext';

export function useTranslation() {
    const { preferredLanguage } = useSession();

    const t = (key, params = {}) => {
        const lang = preferredLanguage || 'en';
        let text = key;

        // Use global fallback to access legacy translations without requiring a heavy import
        if (window.SAHAY && window.SAHAY.translations && window.SAHAY.translations[lang]) {
            text = window.SAHAY.translations[lang][key] || window.SAHAY.translations['en'][key] || key;
        }

        Object.keys(params).forEach(pKey => {
            text = text.replace(new RegExp(`{${pKey}}`, 'g'), params[pKey]);
        });

        return text;
    };

    return { t, lang: preferredLanguage || 'en' };
}
