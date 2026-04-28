from fastapi import APIRouter, Body

from app.schemas import (
    AlertExplanationRequest,
    AnomalyFeatureRequest,
    AnomalyScoreResponse,
    InvestigationPromptRequest,
    InvestigationResponse,
)
from app.services.anomaly_service import anomaly_service
from app.services.explanation_service import explanation_service
from app.services.investigation_service import investigation_service

router = APIRouter()


@router.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@router.post("/ai/anomaly/score", response_model=AnomalyScoreResponse)
def score_anomaly(payload: AnomalyFeatureRequest | None = Body(default=None)) -> AnomalyScoreResponse:
    return anomaly_service.score(payload)


@router.post("/ai/explain-alert", response_model=InvestigationResponse)
def explain_alert(payload: AlertExplanationRequest) -> InvestigationResponse:
    return explanation_service.explain_alert(payload)


@router.post("/ai/investigate", response_model=InvestigationResponse)
def investigate(payload: InvestigationPromptRequest) -> InvestigationResponse:
    return investigation_service.investigate(payload)
