from pydantic import BaseModel
from typing import List


class AlertExplanationRequest(BaseModel):
    title: str
    severity: str
    reason: str
    source: str
    triggeredRule: str
    supportingEvidence: str | None = None
    anomalyContribution: float | None = None
    relatedIncidents: str | None = None
    recommendation: str | None = None
    mode: str = "technical"


class AnomalyFeatureRequest(BaseModel):
    requestCount: int
    uniquePortCount: int
    failedLoginCount: int
    uniqueEndpointCount: int
    accessDeniedCount: int
    privilegedEndpointAccessCount: int
    offHoursActivity: int
    ruleMatchCount: int
    actorRiskScore: int
    incidentScore: int


class AnomalyScoreResponse(BaseModel):
    anomalyScore: float
    modelName: str
    featureSummary: str


class InvestigationTurn(BaseModel):
    role: str
    message: str


class InvestigationPromptRequest(BaseModel):
    sessionId: str
    intent: str
    userMessage: str
    mode: str = "technical"
    targetType: str
    targetId: str
    targetLabel: str
    contextSummary: str
    facts: List[str]
    recommendations: List[str]
    conversationHistory: List[InvestigationTurn]


class InvestigationResponse(BaseModel):
    answer: str
    mode: str
    promptSummary: str
    sessionId: str | None = None
    intent: str | None = None
    recommendations: List[str] = []
    engine: str = "grounded-fallback"
