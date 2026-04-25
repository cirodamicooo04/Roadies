package it.roadies.travel_service.services.impl;

import it.roadies.travel_service.data.dao.TravelDepartureRepository;
import it.roadies.travel_service.data.entity.TravelDeparture;
import it.roadies.travel_service.data.mapper.TravelDepartureMapper;
import it.roadies.travel_service.services.TravelDepartureService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TravelDepartureServiceImpl implements TravelDepartureService {

    private final TravelDepartureRepository travelDepartureRepository;

    @Transactional
    @Override
    public void reserveSeats(UUID travelDepartureId, Integer spots){
        TravelDeparture travel = travelDepartureRepository.findByIdWithLock(travelDepartureId);

        int newSlotsNumber = travel.getAvailableSlots() - spots;
        if (newSlotsNumber >= 0) {
            travel.setAvailableSlots(newSlotsNumber);
            travelDepartureRepository.save(travel);
        } else {
            //qui andrò a modificare non appena aggiungiamo la gestione delle eccezioni
            throw new RuntimeException("Posti insufficienti per questo viaggio");
        }
    }

    @Transactional
    @Override
    public void releaseSeats(UUID travelDepartureId, Integer spots) {
        TravelDeparture travel = travelDepartureRepository.findByIdWithLock(travelDepartureId);

        travel.setAvailableSlots(travel.getAvailableSlots() + spots);
        travelDepartureRepository.save(travel);
    }
}