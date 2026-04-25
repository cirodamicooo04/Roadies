package it.roadies.travel_service.services.impl;

import it.roadies.travel_service.data.dao.ActivityRepository;
import it.roadies.travel_service.data.dao.TagRepository;
import it.roadies.travel_service.data.dao.TravelDepartureRepository;
import it.roadies.travel_service.data.dao.TravelRepository;
import it.roadies.travel_service.data.dao.specification.TravelSpecification;
import it.roadies.travel_service.data.dto.request.*;
import it.roadies.travel_service.data.dto.response.*;
import it.roadies.travel_service.data.entity.*;
import it.roadies.travel_service.data.entity.enumerations.Status;
import it.roadies.travel_service.data.mapper.ActivityMapper;
import it.roadies.travel_service.data.mapper.TravelMapper;
import it.roadies.travel_service.services.TravelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TravelServiceImpl implements TravelService {
    private final TravelMapper travelMapper;
    private final TagRepository tagRepository;
    private final TravelRepository travelRepository;
    private final TravelDepartureRepository travelDepartureRepository;
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    private void validateTravelLogic(Travel travel){
        for (TravelDeparture departure : travel.getDepartures()){
            if (!departure.getStartDate().isBefore(departure.getEndDate())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure start date must be before end date");
            }
        }

        if (travel.getActivities() != null) {
            for (Activity activity : travel.getActivities()) {
                if (activity.getDayNumber() != null && activity.getDayNumber() > travel.getDurationDays()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "L'attività " + activity.getName() + " è assegnata al giorno " + activity.getDayNumber() + " ma il viaggio dura solo " + travel.getDurationDays() + " giorni");
                }
            }
        }
    }

    @Transactional
    public TravelResponse createTravel(TravelCreateRequest travelCreateRequest, String ownerId){
        Travel travel = travelMapper.toEntity(travelCreateRequest, ownerId);
        validateTravelLogic(travel);

        if(travelCreateRequest.getTagScores() != null){
            List<TravelTagRequest> tagScores = travelCreateRequest.getTagScores();
            for (TravelTagRequest ts : tagScores) {
                Tag tag = tagRepository.findById(ts.getTagId()).orElseThrow(() -> new RuntimeException("Tag not found"));
                TravelTag travelTag = new TravelTag();
                travelTag.setTravel(travel);
                travelTag.setTag(tag);
                travelTag.setScore(ts.getScore());
                travel.getTagScores().add(travelTag);
            }
        }

        travelRepository.save(travel);
        return travelMapper.toResponse(travel);
    }

    public TravelResponse getTravelById(UUID id){
        Travel travel = travelRepository.findById(id).orElseThrow(() -> new RuntimeException("Travel not found"));
        return travelMapper.toResponse(travel);
    }

    @Transactional
    public void deleteTravelById(UUID id, String ownerId){
        Travel travel = travelRepository.findById(id).orElseThrow(() -> new RuntimeException("Travel not found"));
        if (!ownerId.equals(travel.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this travel");
        }
        if (travelDepartureRepository.existsByTravel(travel)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't delete a travel with active departures");
        }
        travelRepository.delete(travel);
    }

    @Transactional
    public TravelResponse updateTravel(TravelUpdateRequest travelUpdateRequest, String ownerId, UUID travelId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new RuntimeException("Travel not found"));
        //controllo sull'owner
        if (!ownerId.equals(travel.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update this travel");
        }

        //check se ci sono partenze confermate
        boolean hasConfirmedDepartures = travel.getDepartures().stream().anyMatch(d -> d.getStatus() == Status.CONFIRMED);

        if (hasConfirmedDepartures && travelUpdateRequest.getActivities() != null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't update the activities of this travel because it has confirmed departures");
        }

        //se ci sono partenze confermate non si può modificare
        if (travelUpdateRequest.getDestination() != null && !travelUpdateRequest.getDestination().equals(travel.getDestination()) && hasConfirmedDepartures) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't update the destination of this travel because it has confirmed departures");
        }

        travelMapper.updateTravelFromDto(travelUpdateRequest, travel);
        validateTravelLogic(travel);

        if (travelUpdateRequest.getTagScores() != null){
            travel.getTagScores().clear();
            for (TravelTagRequest ts : travelUpdateRequest.getTagScores()) {
                Tag tag =  tagRepository.findById(ts.getTagId()).orElseThrow(() -> new RuntimeException("Tag not found"));
                TravelTag travelTag = new TravelTag();
                travelTag.setTravel(travel);
                travelTag.setTag(tag);
                travelTag.setScore(ts.getScore());
                travel.getTagScores().add(travelTag);
            }
        }

        Travel updatedTravel = travelRepository.save(travel);
        return travelMapper.toResponse(updatedTravel);
    }

    @Transactional(readOnly = true)
    public List<TravelSummaryResponse> searchTravels(String destination, BigDecimal minPrice, BigDecimal maxPrice, Integer minDurationDays, Integer maxDurationDays){
        Specification<Travel> travelSpecification = Specification.where(TravelSpecification.hasDestination(destination))
                .and(TravelSpecification.hasPriceRange(minPrice, maxPrice))
                .and(TravelSpecification.hasDurationRange(minDurationDays, maxDurationDays));

        List<Travel> travels = travelRepository.findAll(travelSpecification);
        return travels.stream().map(travelMapper::toSummaryResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrganizerTravelsActivityResponse getOrganizerTravelsActivity(String ownerId){
        List<Travel> travels = travelRepository.findAllByOwnerId(ownerId);
        List<TravelSummaryResponse> travelsSummary = travels.stream().map(travelMapper::toSummaryResponse).toList();

        List<Activity> activities = activityRepository.findAllByOwnerId(ownerId);
        List<ActivitySummaryResponse> activitiesSummary = activities.stream().map(activityMapper::toSummaryResponse).toList();

        OrganizerTravelsActivityResponse response = new OrganizerTravelsActivityResponse();
        response.setTravels(travelsSummary);
        response.setActivities(activitiesSummary);
        return response;
    }

    @Transactional
    public TravelDepartureResponse addDeparture(UUID travelId, TravelDepartureCreateRequest departureCreateRequest, String ownerId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new RuntimeException("Travel not found"));
        if (!travel.getOwnerId().equals(ownerId)) {throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to add a departure to this travel");}
        TravelDeparture departure = travelMapper.toDepartureEntity(departureCreateRequest);
        departure.setTravel(travel);

        travel.getDepartures().add(departure);
        validateTravelLogic(travel);

        TravelDeparture savedDeparture = travelDepartureRepository.save(departure);
        return travelMapper.toDepartureResponse(savedDeparture);
    }

    @Transactional
    public void deleteDeparture(UUID travelId, UUID departureId, String ownerId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new RuntimeException("Travel not found"));
        if (!travel.getOwnerId().equals(ownerId)) {throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this departure");}
        TravelDeparture departure = travelDepartureRepository.findById(departureId).orElseThrow(() -> new RuntimeException("Departure not found"));
        if (departure.getStatus() == Status.CONFIRMED){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't delete a confirmed departure");}
        travel.getDepartures().remove(departure);
        travelDepartureRepository.delete(departure);
    }

    @Transactional(readOnly = true)
    public List<TravelDepartureResponse> getTravelDepartures(UUID travelId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new RuntimeException("Travel not found"));
        List<TravelDeparture> departures = travel.getDepartures();
        return departures.stream().filter(d -> d.getStartDate().isAfter(LocalDate.now())).sorted(Comparator.comparing(TravelDeparture::getStartDate)).map(travelMapper::toDepartureResponse).toList();
    }

    @Transactional
    public TravelDepartureResponse updateDeparture(UUID travelId, UUID departureId, TravelDepartureUpdateRequest request, String ownerId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new RuntimeException("Travel not found"));
        if (!travel.getOwnerId().equals(ownerId)){throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update this departure");}
        TravelDeparture departure = travelDepartureRepository.findById(departureId).orElseThrow(() -> new RuntimeException("Departure not found"));
        if (departure.getStatus().equals(Status.CONFIRMED)){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't update a confirmed departure");}

        travelMapper.updateDepartureEntity(request, departure);
        validateTravelLogic(travel);

        travelDepartureRepository.save(departure);
        return travelMapper.toDepartureResponse(departure);
    }

    @Transactional
    public TravelDepartureResponse confirmDeparture(UUID travelId, UUID departureId, String ownerId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new RuntimeException("Travel not found"));
        if (!travel.getOwnerId().equals(ownerId)){throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to confirm this departure");}
        TravelDeparture departure = travelDepartureRepository.findById(departureId).orElseThrow(() -> new RuntimeException("Departure not found"));
        if (departure.getStatus() == Status.CONFIRMED){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This departure is already confirmed");}
        departure.setStatus(Status.CONFIRMED);
        travelDepartureRepository.save(departure);
        return travelMapper.toDepartureResponse(departure);
    }
}
