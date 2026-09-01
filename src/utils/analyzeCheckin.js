/**
 * Local Deterministic Prototype Analysis Engine
 * DO NOT USE IN PRODUCTION OR AS A MEDICAL DIAGNOSTIC TOOL.
 * This converts check-in responses into structured signals and scores.
 */
export function analyzeCheckin(responses, previousCheckins = []) {
    // 1. Signal Extraction
    let distressBase = 0;
    let safetyFlags = 0;
    let engagementScore = 0;

    // Support both AI NLP object and legacy array formats
    if (responses && !Array.isArray(responses) && typeof responses.engagement !== 'undefined') {
        const r = responses;
        distressBase = (r.emotionalDistress * 10) + (r.sleepDifficulty * 4);
        safetyFlags = r.safetyConcern > 0.5 ? 1 : 0;
        engagementScore = r.engagement * 10;
    } else if (Array.isArray(responses)) {
        engagementScore = 10;
        responses.forEach(r => {
            if (r && r.weight) distressBase += r.weight;
            if (r && r.isSafetyRisk) safetyFlags += 1;
            engagementScore += 5;
        });
    }

    // 2. Dynamic Distress Score (0 - 100)
    // Prototype equation
    let rawScore = (distressBase * 5) - (engagementScore * 0.5) + (safetyFlags * 30);
    let finalScore = Math.max(0, Math.min(100, Math.round(rawScore)));

    // 3. Trend Analysis
    let trend = 'Stable';
    if (previousCheckins.length > 0) {
        const lastScore = previousCheckins[previousCheckins.length - 1].distressScore || 0;
        const diff = finalScore - lastScore;
        if (diff > 10) trend = 'Up';
        else if (diff < -10) trend = 'Down';
    } else {
        if (finalScore > 60) trend = 'Up';
    }

    // 4. Concern Type
    let concernType = 'Wellbeing';
    if (safetyFlags > 0) {
        concernType = 'Safety';
    } else if (finalScore > 80) {
        concernType = 'Other'; // Just an example classification
    }

    // 5. Concern Level
    let concernLevel = 'Stable';
    if (safetyFlags > 0 || finalScore > 75 || (trend === 'Up' && finalScore > 60)) {
        concernLevel = 'Increasing Concern';
    } else if (finalScore > 40) {
        concernLevel = 'Elevated';
    }

    // 6. Secure Support Routing (Prototype Metadata)
    let supportRouting = null;
    if (concernLevel !== 'Stable') {
        supportRouting = {
            priority: concernLevel === 'Increasing Concern' ? 'High' : 'Medium',
            concernType: concernType,
            concernLevel: concernLevel,
            routedAt: new Date().toISOString(),
            pathway: concernType === 'Safety' ? 'Priority Review via Dashboard' : 'Standard Check-in Queue'
        };
    }

    return {
        signals: {
            distressBase,
            safetyFlags,
            engagementScore,
            measuredAt: new Date().toISOString()
        },
        distressScore: finalScore,
        trend,
        concernType,
        concernLevel,
        supportRouting
    };
}
