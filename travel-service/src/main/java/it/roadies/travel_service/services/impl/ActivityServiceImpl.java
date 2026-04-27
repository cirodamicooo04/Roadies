package it.roadies.travel_service.services.impl;

import it.roadies.travel_service.data.dao.ActivityDepartureRepository;
import it.roadies.travel_service.data.dao.ActivityRepository;
import it.roadies.travel_service.data.dao.specification.ActivitySpecification;
import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureUpdateRequest;
import it.roadies.travel_service.data.dto.response.ActivityDepartureResponse;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.data.dto.response.ActivitySummaryResponse;
import it.roadies.travel_service.data.entity.Activity;
import it.roadies.travel_service.data.entity.ActivityDeparture;
import it.roadies.travel_service.data.entity.TravelDeparture;
import it.roadies.travel_service.data.entity.enumerations.Status;
import it.roadies.travel_service.data.mapper.ActivityMapper;
import it.roadies.travel_service.services.ActivityService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.PropertySource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityMapper activityMapper;
    private final ActivityRepository activityRepository;
    private final ActivityDepartureRepository activityDepartureRepository;

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
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        return activityMapper.toResponse(activity);
    }

    @Transactional
    public void deleteActivityById(UUID id, String ownerId){
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this activity");
        activityRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ActivitySummaryResponse> searchActivities(String destination, BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<Activity> specification = Specification.where(ActivitySpecification.hasDestination(destination))
                .and(ActivitySpecification.hasPriceRange(minPrice, maxPrice)
                        .and(ActivitySpecification.isStandalone()));

        List<Activity> activities = activityRepository.findAll(specification);
        return activities.stream().map(activityMapper::toSummaryResponse).toList();
    }

    @Transactional
    public ActivityDepartureResponse addDeparture(UUID activityId, ActivityDepartureCreateRequest request,  String ownerId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to add a departure to this activity");

        ActivityDeparture departure = activityMapper.toDepartureEntity(request);
        departure.setActivity(activity);

        activity.getDepartures().add(departure);
        validateActivity(activity);

        activityDepartureRepository.save(departure);
        return activityMapper.toDeparturesResponse(departure);
    }

    @Transactional
    public void deleteDeparture(UUID activityId, UUID departureId, String ownerId){
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this departure");

        ActivityDeparture departure = activityDepartureRepository.findById(departureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departure not found"));
        if (!departure.getActivity().getId().equals(activityId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure not found in this activity");

        if (departure.getStatus() == Status.CONFIRMED) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't delete a confirmed departure");
        activity.getDepartures().remove(departure);
        activityDepartureRepository.delete(departure);
    }

    @Transactional
    public ActivityDepartureResponse updateDeparture(UUID activityId, UUID departureId, ActivityDepartureUpdateRequest request, String ownerId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update this departure");

        ActivityDeparture departure = activityDepartureRepository.findById(departureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departure not found"));
        if (!departure.getActivity().getId().equals(activityId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure not found in this activity");
        if (departure.getStatus() == Status.CONFIRMED) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't update a confirmed departure");

        activityMapper.updateDepartureEntity(request, departure);
        validateActivity(activity);

        activityDepartureRepository.save(departure);
        return activityMapper.toDeparturesResponse(departure);
    }

    @Transactional(readOnly = true)
    public List<ActivityDepartureResponse> getDepartures(UUID activityId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        return activity.getDepartures().stream().filter(d -> d.getStartTimestamp().isAfter(LocalDateTime.now())).sorted(Comparator.comparing(ActivityDeparture::getStartTimestamp)).map(activityMapper::toDeparturesResponse).toList();
    }

    @Transactional
    public ActivityDepartureResponse confirmDeparture(UUID activityId, UUID departureId, String ownerId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to confirm this departure");

        ActivityDeparture departure = activityDepartureRepository.findById(departureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departure not found"));
        if (!departure.getActivity().getId().equals(activityId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure not found in this activity");
        if (departure.getStatus() == Status.CONFIRMED) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This departure is already confirmed");
        departure.setStatus(Status.CONFIRMED);
        activityDepartureRepository.save(departure);
        return activityMapper.toDeparturesResponse(departure);
    }

    public List<String> getUniqueDestinations() {
        return activityRepository.findUniqueDestinations();
    }
}
