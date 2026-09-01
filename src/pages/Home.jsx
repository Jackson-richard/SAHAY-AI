import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useSession } from '../context/SessionContext';
import { useTranslation } from '../utils/translation';

export default function Home() {
    const { profile, caseInfo, checkins, distressScore, trend, concernLevel } = useSession();
    const { t } = useTranslation();
    const navigate = useNavigate();

    return (
        <div className="flex-grow pt-[64px] pb-xl px-container-margin md:px-lg max-w-lg mx-auto w-full flex flex-col">
            <header className="fixed top-0 left-0 w-full bg-surface flex items-center justify-between px-md h-touch-target z-50 border-b border-outline-variant">
                <span className="font-headline text-headline-lg font-bold text-primary tracking-tight">SAHAY-AI</span>
            </header>

            <div className="mt-lg flex flex-col gap-xs fade-up">
                <h1 className="font-display text-display-lg text-on-surface">Hello, {profile?.fullName}</h1>
                <p className="font-body text-body-lg text-on-surface-variant">Welcome to your SAHAY-AI wellbeing dashboard.</p>
            </div>

            <div className="mt-lg pt-sm fade-up fade-up-1">
                <button
                    onClick={() => navigate('/checkin')}
                    className="btn-primary bg-primary text-on-primary font-label text-label-lg h-touch-target rounded-full w-full flex items-center justify-center gap-xs shadow-md">
                    <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>chat_bubble</span>
                    <span>Start Wellbeing Check-in</span>
                </button>
            </div>

            <div className="mt-xl pt-lg border-t border-outline-variant fade-up fade-up-2">
                <h2 className="font-headline text-headline-md text-on-surface mb-sm">Active Case Information</h2>
                <div className="bg-surface-container-lowest rounded-xl p-md border border-outline-variant font-body text-body-md text-on-surface">
                    {caseInfo?.id ? (
                        <div className="flex items-center gap-xs">
                            <span className="material-symbols-outlined text-primary" style={{ fontSize: '20px' }}>gavel</span>
                            <span className="font-mono bg-surface-container-low px-xs py-1 rounded">{caseInfo.id}</span>
                        </div>
                    ) : (
                        <span className="text-on-surface-variant italic">No case ID provided.</span>
                    )}
                </div>
            </div>

            {checkins && checkins.length > 0 ? (
                <div className="mt-lg pt-lg border-t border-outline-variant fade-up fade-up-3">
                    <h2 className="font-headline text-headline-md text-on-surface mb-sm">Recent Status</h2>
                    <div className="bg-surface-container-lowest rounded-xl p-md border border-outline-variant font-body text-body-md text-on-surface flex flex-col gap-sm">
                        <div className="flex justify-between items-center">
                            <span className="text-on-surface-variant font-label text-label-sm">Distress Score</span>
                            <span className="font-bold">{distressScore !== null ? distressScore : '-'} / 100</span>
                        </div>
                        <div className="flex justify-between items-center">
                            <span className="text-on-surface-variant font-label text-label-sm">Trend</span>
                            <span className="font-bold flex items-center gap-1">
                                {trend === 'Up' && <span className="material-symbols-outlined text-error" style={{ fontSize: '16px' }}>trending_up</span>}
                                {trend === 'Down' && <span className="material-symbols-outlined text-primary" style={{ fontSize: '16px' }}>trending_down</span>}
                                {trend === 'Stable' && <span className="material-symbols-outlined text-outline" style={{ fontSize: '16px' }}>trending_flat</span>}
                                {trend}
                            </span>
                        </div>
                        <div className="flex justify-between items-center">
                            <span className="text-on-surface-variant font-label text-label-sm">Concern Level</span>
                            <span className={`font-bold ${concernLevel === 'Stable' ? 'text-primary' : 'text-error'}`}>{concernLevel}</span>
                        </div>
                        <div className="pt-sm mt-xs border-t border-surface-variant">
                            <span className="text-on-surface-variant text-sm block mb-xs">Completed Check-ins: {checkins.length}</span>
                            <button onClick={() => navigate('/checkin-history')} className="text-primary font-label text-label-sm uppercase flex items-center gap-1">
                                View History <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>arrow_forward</span>
                            </button>
                        </div>
                    </div>
                </div>
            ) : (
                <div className="mt-lg pt-lg border-t border-outline-variant fade-up fade-up-3 text-center py-md">
                    <p className="font-body text-body-md text-on-surface-variant italic">No wellbeing check-ins yet.</p>
                </div>
            )}
        </div>
    );
}
