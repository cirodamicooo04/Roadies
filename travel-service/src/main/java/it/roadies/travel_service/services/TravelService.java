package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dao.TagRepository;
import it.roadies.travel_service.data.dao.TravelDepartureRepository;
import it.roadies.travel_service.data.dao.TravelRepository;
import it.roadies.travel_service.data.dto.request.TravelCreateRequest;
import it.roadies.travel_service.data.dto.request.TravelTagRequest;
import it.roadies.travel_service.data.dto.request.TravelUpdateRequest;
import it.roadies.travel_service.data.dto.response.TravelResponse;
import it.roadies.travel_service.data.entity.*;
import it.roadies.travel_service.data.entity.enumerations.Status;
import it.roadies.travel_service.data.mapper.TravelMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TravelService {
    private final TravelMapper travelMapper;
    private final TagRepository tagRepository;
    private final TravelRepository travelRepository;
    private final TravelDepartureRepository travelDepartureRepository;

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
}
