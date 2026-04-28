package com.example.securityplatform.incidents;

import com.example.securityplatform.incidents.repository.IncidentRepository;
import com.example.securityplatform.common.Severity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<IncidentResponse> list() {
        return incidentRepository.findAll()
                .stream()
                .map(incident -> new IncidentResponse(
                        incident.getIncidentId(),
                        incident.getTitle(),
                        incident.getSeverity(),
                        incident.getSummary(),
                        incident.getActorId(),
                        incident.getIncidentScore(),
                        incident.getStatus().name(),
                        incident.getCreatedAt()
                ))
                .toList();
    }
}
