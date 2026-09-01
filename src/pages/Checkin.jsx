import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSession } from '../context/SessionContext';
import { analyzeCheckin } from '../utils/analyzeCheckin';
import { extractSignals } from '../services/aiSignalExtractor';
import { useTranslation } from '../utils/translation';

export default function Checkin() {
    const {
        preferredLanguage,
        checkins,
        setCheckins,
        setDistressScore,
        setTrend,
        setConcernType,
        setConcernLevel,
        setSupportRouting
    } = useSession();
    const navigate = useNavigate();
    const { lang } = useTranslation();

    const [responseText, setResponseText] = useState('');
    const [isAnalyzing, setIsAnalyzing] = useState(false);

    const handleSubmit = async () => {
        if (!responseText.trim() || isAnalyzing) return;
        setIsAnalyzing(true);

        try {
            // 1. Convert free-text to structured NLP signals (Mock Local AI)
            const nlpSignals = await extractSignals(responseText, preferredLanguage);

            // 2. Deterministic Scoring Engine
            const result = analyzeCheckin(nlpSignals, checkins);

            // 3. Update Context State
            const newRecord = {
                id: `chk_${Date.now()}`,
                timestamp: new Date().toISOString(),
                language: preferredLanguage,
                responses: [responseText], // Store raw text purely in array for context mapping (internal state only)
                signals: result.signals,
                distressScore: result.distressScore,
                trend: result.trend,
                concernType: result.concernType,
                concernLevel: result.concernLevel,
                nlpConfidence: nlpSignals.confidence
            };

            const updatedCheckins = [...checkins, newRecord];
            setCheckins(updatedCheckins);
            setDistressScore(result.distressScore);
            setTrend(result.trend);
            setConcernType(result.concernType);
            setConcernLevel(result.concernLevel);
            setSupportRouting(result.supportRouting);

            // 4. Route to Results
            navigate('/checkin-result');
        } catch (error) {
            console.error("Extraction error", error);
            // Safe fallback implementation without crashing
            setIsAnalyzing(false);
        }
    };

    return (
        <div className="flex-grow pt-[64px] pb-xl px-container-margin md:px-lg max-w-lg mx-auto w-full flex flex-col">
            <header className="fixed top-0 left-0 w-full bg-surface flex items-center justify-between px-md h-touch-target z-50 border-b border-outline-variant">
                <button
                    className="w-touch-target h-touch-target flex items-center justify-center text-primary hover:bg-surface-container-low rounded-full transition-colors -ml-sm"
                    onClick={() => navigate('/home')} aria-label="Cancel">
                    <span className="material-symbols-outlined">close</span>
                </button>
                <span className="font-headline text-label-lg font-bold text-on-surface tracking-tight">Wellbeing Check-in</span>
                <div className="w-touch-target text-right text-on-surface-variant font-label text-label-sm">{lang.toUpperCase()}</div>
            </header>

            <div className="mt-md flex flex-col flex-grow gap-lg fade-up">
                <div className="bg-primary-container text-on-primary-container p-md rounded-xl rounded-tl-sm self-start max-w-[85%] shadow-sm">
                    <p className="font-body text-body-lg">How have you been feeling since your last check-in?</p>
                </div>

                <div className="self-end w-full fade-up fade-up-1">
                    <textarea
                        className="field-input w-full p-md border border-outline-variant rounded-xl rounded-br-sm font-body text-body-md text-on-surface bg-surface-container-lowest focus:outline-none min-h-[160px] resize-none"
                        placeholder="Type your response here..."
                        value={responseText}
                        onChange={(e) => setResponseText(e.target.value)}
                        disabled={isAnalyzing}
                    />
                </div>
            </div>

            <div className="mt-xl pt-lg sticky bottom-0 bg-background pb-md fade-up fade-up-2">
                <button
                    onClick={handleSubmit}
                    disabled={!responseText.trim() || isAnalyzing}
                    className={`btn-primary font-label text-label-lg h-touch-target rounded-full w-full flex items-center justify-center shadow-sm transition-all ${responseText.trim() && !isAnalyzing ? 'bg-primary text-on-primary' : 'bg-surface-variant text-on-surface-variant opacity-50 cursor-not-allowed'}`}>
                    {isAnalyzing ? (
                        <span className="flex items-center gap-2">
                            <span className="material-symbols-outlined animate-spin" style={{ fontSize: '18px' }}>progress_activity</span>
                            Analyzing Response...
                        </span>
                    ) : (
                        'Complete Check-in'
                    )}
                </button>
            </div>
        </div>
    );
}
