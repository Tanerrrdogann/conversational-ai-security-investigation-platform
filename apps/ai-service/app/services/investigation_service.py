from app.prompts.explanation_prompts import build_investigation_prompt
from app.schemas import InvestigationPromptRequest, InvestigationResponse
from app.services.llm_service import llm_service


class InvestigationService:
    def investigate(self, payload: InvestigationPromptRequest) -> InvestigationResponse:
        prompt_summary = build_investigation_prompt(payload)
        answer, engine = self._compose_answer(payload, prompt_summary)
        return InvestigationResponse(
            answer=answer,
            mode=payload.mode,
            promptSummary=prompt_summary,
            sessionId=payload.sessionId,
            intent=payload.intent,
            recommendations=payload.recommendations,
            engine=engine,
        )

    def _compose_answer(self, payload: InvestigationPromptRequest, prompt_summary: str) -> tuple[str, str]:
        llm_answer = llm_service.generate(
            system_prompt=(
                "You are a grounded security investigation copilot. "
                "Answer the analyst's exact question first. "
                "Only use supplied platform facts. "
                "If the facts are insufficient, say what is missing instead of changing the topic. "
                "Keep the response concise, make recommendations actionable, "
                "and answer in the same language as the analyst."
            ),
            user_prompt=(
                f"{prompt_summary}\n\n"
                f"Intent: {payload.intent}\n"
                f"Mode: {payload.mode}\n"
                f"Analyst message: {payload.userMessage}\n"
                f"Target: {payload.targetLabel}\n"
                f"Facts:\n- " + "\n- ".join(payload.facts[:8]) + "\n"
                f"Recommendations:\n- " + "\n- ".join(payload.recommendations[:5]) + "\n"
            ),
        )
        if llm_answer:
            return llm_answer, f"llm:{llm_service.provider}"

        fact_lines = "; ".join(payload.facts[:5])
        history_note = ""
        if payload.conversationHistory:
            history_note = (
                f" This answer continues the existing investigation thread with "
                f"{len(payload.conversationHistory)} prior turn(s)."
            )

        fallback_engine = self._fallback_engine()

        if payload.intent == "TOP_RISKY_ACTOR":
            return (
                f"The highest-priority actor right now is {payload.targetLabel}. "
                f"Key evidence: {fact_lines}.{history_note}"
            ), fallback_engine
        if payload.intent == "ALERT_EXPLANATION":
            return (
                f"Alert {payload.targetLabel} should be investigated because {fact_lines}.{history_note}"
            ), fallback_engine
        if payload.intent == "INCIDENT_SUMMARY":
            return (
                f"Incident {payload.targetLabel} represents a connected chain of suspicious activity. "
                f"Grounded summary: {fact_lines}.{history_note}"
            ), fallback_engine
        if payload.intent == "ACTOR_INVESTIGATION":
            return (
                f"Actor {payload.targetLabel} looks risky based on the collected evidence. "
                f"Most relevant facts: {fact_lines}.{history_note}"
            ), fallback_engine
        if payload.intent == "RECOMMENDATION":
            steps = ", ".join(payload.recommendations[:3])
            return (
                f"For {payload.targetLabel}, the next best investigation steps are: {steps}. "
                f"Supporting context: {fact_lines}.{history_note}"
            ), fallback_engine
        return (
            f"Current platform view: {fact_lines}. "
            f"Use follow-up questions to drill into the actor, alert, or incident that matters most.{history_note}"
        ), fallback_engine

    def _fallback_engine(self) -> str:
        return f"grounded-fallback ({llm_service.last_error})" if llm_service.last_error else "grounded-fallback"


investigation_service = InvestigationService()
