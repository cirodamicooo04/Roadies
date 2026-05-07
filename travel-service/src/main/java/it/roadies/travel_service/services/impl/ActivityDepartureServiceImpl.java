package it.roadies.travel_service.services.impl;

import it.roadies.travel_service.data.dao.ActivityDepartureRepository;
import it.roadies.travel_service.data.entity.ActivityDeparture;
import it.roadies.travel_service.exceptions.NotEnoughSeatsException;
import it.roadies.travel_service.services.ActivityDepartureService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityDepartureServiceImpl implements ActivityDepartureService {
    private final ActivityDepartureRepository activitySessionRepository;

    @Transactional
    public void reserveSeats(UUID id, Integer peopleCount) {
        ActivityDeparture activityDeparture = activitySessionRepository.findByIdWithLock(id);

        int newSlotsNumber = activityDeparture.getAvailableSlots() - peopleCount;
        if (newSlotsNumber >= 0) {
            activityDeparture.setAvailableSlots(newSlotsNumber);
        } else {
            throw new NotEnoughSeatsException("Posti insufficienti per questo viaggio");
        }
        activitySessionRepository.save(activityDeparture);
    }

    @Transactional
    public void releaseSeats(UUID id, Integer peopleCount) {
        ActivityDeparture activityDeparture = activitySessionRepository.findByIdWithLock(id);

        activityDeparture.setAvailableSlots(activityDeparture.getAvailableSlots() + peopleCount);
        activitySessionRepository.save(activityDeparture);
    }
}
