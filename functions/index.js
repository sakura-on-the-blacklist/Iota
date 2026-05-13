const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {setGlobalOptions} = require("firebase-functions");
const fetch = require("node-fetch");

setGlobalOptions({maxInstances: 10});

const GROQ_API_KEY = "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";

exports.analyzeHabit = onCall(async (request) => {
    const {habit, identity, location} = request.data || {};

    if (!habit || !identity) {
        throw new HttpsError("invalid-argument", "Habit and identity are required.");
    }

    const prompt = `
    You are a habit coach using the Atomic Habits framework.
    User Identity: "${identity}"
    Proposed Habit: "${habit}"
    Location/Context: "${location || "not specified"}"

    Evaluate:
    1. RELEVANCE: Does it help them become "${identity}"?
    2. SPECIFICITY: Is it concrete/measurable?
    3. SUSTAINABILITY: Is it doable long-term?

    Respond ONLY with a valid JSON object:
    {
      "valid": true/false,
      "reason": "short explanation",
      "suggestion": "better phrasing if invalid",
      "frequencyDays": ["MON","TUE","WED","THU","FRI","SAT","SUN"],
      "goalValue": number,
      "goalUnit": "times/minutes/hours/km/steps/glasses/pages/kg/calories/reps",
      "startTime": "HH:mm"
    }`;

    try {
        const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${GROQ_API_KEY}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                model: "llama-3.1-8b-instant",
                messages: [{role: "user", content: prompt}],
                response_format: { type: "json_object" },
                temperature: 0.5
            })
        });

        if (!response.ok) throw new Error("Groq API failure");

        const data = await response.json();
        const aiResponse = JSON.parse(data.choices[0].message.content);

        return {
            valid: Boolean(aiResponse.valid),
            reason: aiResponse.reason || "",
            suggestion: aiResponse.suggestion || "",
            frequencyDays: aiResponse.frequencyDays || [],
            goalValue: aiResponse.goalValue || 0,
            goalUnit: aiResponse.goalUnit || "",
            startTime: aiResponse.startTime || ""
        };
    } catch (error) {
        console.error("Analysis Error:", error);
        throw new HttpsError("internal", "AI analysis failed.");
    }
});