package it.roadies.travel_service.services.impl;

import it.roadies.travel_service.data.dao.ActivityRepository;
import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.data.entity.Activity;
import it.roadies.travel_service.data.entity.ActivityDeparture;
import it.roadies.travel_service.data.mapper.ActivityMapper;
import it.roadies.travel_service.services.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityMapper activityMapper;
    private final ActivityRepository activityRepository;

    private void validateActivity(Activity activity){
        if (activity.getTravel() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Standalone activity can't have a travel");

        for (ActivityDeparture departure : activity.getDepartures()){
            if (!departure.getStartTimestamp().isBefore(departure.getEndTimestamp())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure start date must be before end date");
            }
        }
    }

    @Transactional
    public ActivityResponse createActivity(ActivityCreateRequest request, String ownerId){
        Activity activity = activityMapper.toEntity(request);
        validateActivity(activity);
        activity.setOwnerId(ownerId);

        activityRepository.save(activity);
        return activityMapper.toResponse(activity);
    }

    public ActivityResponse getActivityById(UUID id){
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new RuntimeException("Activity not found"));
        return activityMapper.toResponse(activity);
    }

    @Transactional
    public void deleteActivityById(UUID id, String ownerId){
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new RuntimeException("Activity not found"));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this activity");
        activityRepository.deleteById(id);
    }
}
