package it.roadies.travel_service.services.impl;

import it.roadies.shared.i18n.MessageLang;
import it.roadies.travel_service.data.dao.TravelDepartureRepository;
import it.roadies.shared.contracts.TravelBatchResponse;
import it.roadies.travel_service.data.entity.TravelDeparture;
import it.roadies.travel_service.data.mapper.TravelDepartureMapper;
import it.roadies.travel_service.exceptions.NotEnoughSeatsException;
import it.roadies.travel_service.exceptions.NotValidTravelId;
import it.roadies.travel_service.exceptions.StatusException;
import it.roadies.travel_service.services.TravelDepartureService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import it.roadies.travel_service.data.entity.enumerations.Status;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TravelDepartureServiceImpl implements TravelDepartureService {

    private final TravelDepartureRepository travelDepartureRepository;
    private final MessageLang messageLang;

    @Transactional
    @Override
    public void reserveSeats(UUID travelDepartureId, Integer spots){
        TravelDeparture travel = travelDepartureRepository.findByIdWithLock(travelDepartureId);
        if (travel.getStatus() != Status.CONFIRMED) {
            throw new StatusException(messageLang.getMessage("error.departure.not.confirmed"));
        }

        int newSlotsNumber = travel.getAvailableSlots() - spots;
        if (newSlotsNumber >= 0) {
            travel.setAvailableSlots(newSlotsNumber);
            travelDepartureRepository.save(travel);
        } else {
            throw new NotEnoughSeatsException("Posti insufficienti per questo viaggio");
        }
    }

    @Transactional
    @Override
    public void releaseSeats(UUID travelDepartureId, Integer spots) {
        TravelDeparture travel = travelDepartureRepository.findByIdWithLock(travelDepartureId);

        travel.setAvailableSlots(travel.getAvailableSlots() + spots);
        travelDepartureRepository.save(travel);
    }

    public boolean isValidTravel(UUID travelDepartureId){
        return travelDepartureRepository.existsById(travelDepartureId);
    }

    public BigDecimal getTravelPriceById(UUID travelDepartureId){
        return travelDepartureRepository.findPriceById(travelDepartureId).orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.departure.not.found")));
    }

    @Override
    public List<TravelBatchResponse> getTravelsBatch(List<UUID> travelIds) {
        List<TravelDeparture> departures = travelDepartureRepository.findAllById(travelIds);
        return departures.stream()
                .map(d -> new TravelBatchResponse(
                        d.getTravel().getId(),
                        d.getId(),
                        d.getTravel() != null ? d.getTravel().getTitle() : null,
                        d.getStartDate(),
                        d.getEndDate()
                ))
                .toList();
    }
}