package com.example.securityplatform.investigation;

import com.example.securityplatform.anomaly.dto.AnomalyFeatureRequest;
import com.example.securityplatform.anomaly.dto.AnomalyScoreResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(@Value("${app.ai-service.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(10_000);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public InvestigationResponse investigate(InvestigationPromptRequest request) {
        return restClient.post()
                .uri("/ai/investigate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(InvestigationResponse.class);
    }

    public InvestigationResponse explainAlert(AlertExplanationRequest request) {
        return restClient.post()
                .uri("/ai/explain-alert")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(InvestigationResponse.class);
    }

    public AnomalyScoreResponse scoreAnomaly(AnomalyFeatureRequest request) {
        return restClient.post()
                .uri("/ai/anomaly/score")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AnomalyScoreResponse.class);
    }
}
