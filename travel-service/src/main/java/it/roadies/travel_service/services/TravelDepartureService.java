package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dao.TravelDepartureRepository;
import it.roadies.travel_service.data.dto.client.ReservationResponse;
import it.roadies.travel_service.data.dto.client.ReserveSeatsRequest;
import it.roadies.travel_service.data.entity.TravelDeparture;
import it.roadies.travel_service.data.mapper.TravelDepartureMapper;
import it.roadies.travel_service.exceptions.NotEnoughSeatsException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TravelDepartureService {

    private final TravelDepartureMapper travelDepartureMapper;
    private final TravelDepartureRepository travelDepartureRepository;

    @Transactional
    public void reserveSeats(UUID id, Integer peopleCount) {
        TravelDeparture travelDeparture = travelDepartureRepository.findByIdWithLock(id);

        int newSlotsNumber = travelDeparture.getAvailableSlots() - peopleCount;
        if (newSlotsNumber >= 0) {
            travelDeparture.setAvailableSlots(newSlotsNumber);
        } else {
            throw new NotEnoughSeatsException("Posti insufficienti per questo viaggio");
        }
        travelDepartureRepository.save(travelDeparture);
    }

    @Transactional
    public void releaseSeats(UUID id, Integer peopleCount) {
        TravelDeparture travelDeparture = travelDepartureRepository.findByIdWithLock(id);

        travelDeparture.setAvailableSlots(travelDeparture.getAvailableSlots() + peopleCount);
        travelDepartureRepository.save(travelDeparture);
    }
}