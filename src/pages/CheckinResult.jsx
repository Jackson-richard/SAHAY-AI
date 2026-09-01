import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useSession } from '../context/SessionContext';

export default function CheckinResult() {
    const { distressScore, trend, concernType, concernLevel, checkins } = useSession();
    const navigate = useNavigate();

    if (distressScore === null) {
        return (
            <div className="flex-grow flex items-center justify-center p-lg">
                <p>No results available. Please complete a check-in.</p>
                <button onClick={() => navigate('/home')} className="text-primary ml-sm">Go Home</button>
            </div>
        );
    }

    const isStable = concernLevel === 'Stable';
    const isSafety = concernType === 'Safety';

    let explanation = "Your recent responses remain stable.";
    if (trend === 'Up') {
        explanation = "Your latest responses show more distress than your previous check-in.";
    } else if (trend === 'Down') {
        explanation = "Your responses indicate an improvement in wellbeing compared to your previous check-in.";
    }

    if (isSafety) {
        explanation = "You indicated a safety concern. This signal has been noted for priority review by authorized support personnel.";
    }

    return (
        <div className="flex-grow pt-[64px] pb-xl px-container-margin md:px-lg max-w-lg mx-auto w-full flex flex-col">
            <header className="fixed top-0 left-0 w-full bg-surface flex items-center justify-center px-md h-touch-target z-50 border-b border-outline-variant">
                <span className="font-headline text-label-lg font-bold text-on-surface tracking-tight">Check-in Summary</span>
            </header>

            <div className="mt-lg flex flex-col gap-sm fade-up text-center mb-md">
                <div className={`mx-auto flex items-center justify-center w-20 h-20 rounded-full mb-xs shadow-sm ${isStable ? 'bg-primary-container text-on-primary-container' : 'bg-error-container text-on-error-container'}`}>
                    <span className="font-display text-[32px] font-bold">{distressScore}</span>
                </div>
                <h1 className="font-display text-display-lg text-on-surface">Distress Score</h1>
            </div>

            <div className="bg-surface-container-lowest rounded-xl p-md border border-outline-variant shadow-sm fade-up fade-up-1">
                <div className="flex flex-col gap-md">
                    <div className="flex justify-between items-center pb-sm border-b border-surface-variant">
                        <span className="text-on-surface-variant font-label text-label-lg">Concern Level</span>
                        <span className={`font-bold ${isStable ? 'text-primary' : 'text-error'}`}>{concernLevel}</span>
                    </div>

                    <div className="flex justify-between items-center pb-sm border-b border-surface-variant">
                        <span className="text-on-surface-variant font-label text-label-lg">Trend Indicator</span>
                        <span className="font-bold flex items-center gap-1">
                            {trend === 'Up' && <span className="material-symbols-outlined text-error" style={{ fontSize: '18px' }}>trending_up</span>}
                            {trend === 'Down' && <span className="material-symbols-outlined text-primary" style={{ fontSize: '18px' }}>trending_down</span>}
                            {trend === 'Stable' && <span className="material-symbols-outlined text-outline" style={{ fontSize: '18px' }}>trending_flat</span>}
                            {trend}
                        </span>
                    </div>

                    <div className="flex justify-between items-center pb-sm border-b border-surface-variant">
                        <span className="text-on-surface-variant font-label text-label-lg">Primary Signal Type</span>
                        <span className="font-bold">{concernType}</span>
                    </div>

                    <div className="pt-xs">
                        <p className="font-body text-body-md text-on-surface-variant">{explanation}</p>
                    </div>
                </div>
            </div>

            <div className="mt-xl pt-lg fade-up fade-up-2 sticky bottom-xl bg-background">
                <button
                    onClick={() => navigate('/home')}
                    className="btn-primary bg-primary text-on-primary font-label text-label-lg h-touch-target rounded-full w-full flex items-center justify-center shadow-sm">
                    Return to Dashboard
                </button>
            </div>
        </div>
    );
}
