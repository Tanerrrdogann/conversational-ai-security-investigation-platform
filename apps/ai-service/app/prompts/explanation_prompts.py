from app.schemas import AlertExplanationRequest, InvestigationPromptRequest


def build_alert_explanation_prompt(payload: AlertExplanationRequest) -> str:
    return (
        "You are a security investigation assistant. Explain the alert using only the provided facts.\n"
        f"Title: {payload.title}\n"
        f"Severity: {payload.severity}\n"
        f"Reason: {payload.reason}\n"
        f"Source: {payload.source}\n"
        f"Triggered rule: {payload.triggeredRule}\n"
        f"Supporting evidence: {payload.supportingEvidence}\n"
        f"Anomaly contribution: {payload.anomalyContribution}\n"
        f"Related incidents: {payload.relatedIncidents}\n"
        f"Recommendation: {payload.recommendation}\n"
        f"Mode: {payload.mode}\n"
        "Output must stay grounded in the supplied evidence."
    )


def build_investigation_prompt(payload: InvestigationPromptRequest) -> str:
    return (
        "You are a conversational AI security investigation assistant. Answer using only the supplied context.\n"
        f"Intent: {payload.intent}\n"
        f"Mode: {payload.mode}\n"
        f"Target type: {payload.targetType}\n"
        f"Target id: {payload.targetId}\n"
        f"Target label: {payload.targetLabel}\n"
        f"Context summary: {payload.contextSummary}\n"
        f"Facts: {payload.facts}\n"
        f"Recommendations: {payload.recommendations}\n"
        f"Conversation history: {[turn.model_dump() for turn in payload.conversationHistory]}\n"
        f"User message: {payload.userMessage}\n"
        "Stay grounded. Do not invent facts. Use the recommendations only when relevant."
    )
