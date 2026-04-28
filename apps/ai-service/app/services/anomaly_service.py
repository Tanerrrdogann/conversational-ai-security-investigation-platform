import numpy as np
from sklearn.ensemble import IsolationForest

from app.schemas import AnomalyFeatureRequest, AnomalyScoreResponse


class AnomalyService:
    def __init__(self) -> None:
        self.model = IsolationForest(
            random_state=42,
            contamination=0.15,
            n_estimators=100,
        )
        self.model.fit(self._baseline_samples())

    def score(self, payload: AnomalyFeatureRequest | None) -> AnomalyScoreResponse:
        if payload is None:
            payload = AnomalyFeatureRequest(
                requestCount=1,
                uniquePortCount=0,
                failedLoginCount=0,
                uniqueEndpointCount=1,
                accessDeniedCount=0,
                privilegedEndpointAccessCount=0,
                offHoursActivity=0,
                ruleMatchCount=0,
                actorRiskScore=0,
                incidentScore=0,
            )

        vector = np.array([[
            payload.requestCount,
            payload.uniquePortCount,
            payload.failedLoginCount,
            payload.uniqueEndpointCount,
            payload.accessDeniedCount,
            payload.privilegedEndpointAccessCount,
            payload.offHoursActivity,
            payload.ruleMatchCount,
            payload.actorRiskScore,
            payload.incidentScore,
        ]], dtype=float)

        raw_score = float(-self.model.score_samples(vector)[0])
        anomaly_score = max(0.0, min(1.0, raw_score / 0.6))

        summary = (
            f"rule_match_count={payload.ruleMatchCount}, "
            f"actor_risk_score={payload.actorRiskScore}, "
            f"incident_score={payload.incidentScore}, "
            f"off_hours_activity={payload.offHoursActivity}"
        )
        return AnomalyScoreResponse(
            anomalyScore=round(anomaly_score, 4),
            modelName="isolation-forest-baseline",
            featureSummary=summary,
        )

    def _baseline_samples(self) -> np.ndarray:
        return np.array([
            [1, 0, 0, 1, 0, 0, 0, 0, 5, 10],
            [1, 0, 0, 1, 0, 0, 0, 0, 8, 12],
            [1, 1, 0, 1, 0, 0, 0, 1, 12, 18],
            [1, 0, 1, 1, 0, 0, 0, 1, 18, 22],
            [1, 1, 1, 1, 0, 1, 1, 2, 35, 45],
            [1, 1, 0, 1, 1, 1, 1, 2, 40, 50],
            [1, 1, 1, 1, 1, 1, 1, 3, 55, 65],
            [1, 1, 1, 1, 0, 1, 0, 2, 25, 35],
        ], dtype=float)


anomaly_service = AnomalyService()
