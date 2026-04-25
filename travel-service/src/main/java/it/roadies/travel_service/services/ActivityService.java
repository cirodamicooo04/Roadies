package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.data.entity.Activity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public interface ActivityService {
    ActivityResponse createActivity(ActivityCreateRequest request, String ownerId);
    ActivityResponse getActivityById(UUID id);
    void deleteActivityById(UUID id, String ownerId);
}
