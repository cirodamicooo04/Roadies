package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dao.TagRepository;
import it.roadies.travel_service.data.dao.TravelDepartureRepository;
import it.roadies.travel_service.data.dao.TravelRepository;
import it.roadies.travel_service.data.dto.request.TravelCreateRequest;
import it.roadies.travel_service.data.dto.request.TravelTagRequest;
import it.roadies.travel_service.data.dto.response.TravelResponse;
import it.roadies.travel_service.data.entity.Tag;
import it.roadies.travel_service.data.entity.Travel;
import it.roadies.travel_service.data.entity.TravelTag;
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

    @Transactional
    public TravelResponse createTravel(TravelCreateRequest travelCreateRequest, String ownerId){
        Travel travel = travelMapper.toEntity(travelCreateRequest, ownerId);

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
        if (travelDepartureRepository.findByTravel(travel)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't delete a travel with active departures");
        }
        travelRepository.delete(travel);
    }
}
