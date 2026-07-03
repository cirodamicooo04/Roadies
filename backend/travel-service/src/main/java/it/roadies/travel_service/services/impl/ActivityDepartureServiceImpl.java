package it.roadies.travel_service.services.impl;

import it.roadies.shared.i18n.MessageLang;
import it.roadies.travel_service.data.dao.ActivityDepartureRepository;
import it.roadies.shared.contracts.ActivityBatchResponse;
import it.roadies.travel_service.data.entity.ActivityDeparture;
import it.roadies.travel_service.data.entity.enumerations.Status;
import it.roadies.travel_service.exceptions.NotEnoughSeatsException;
import it.roadies.travel_service.exceptions.StatusException;
import it.roadies.travel_service.services.ActivityDepartureService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityDepartureServiceImpl implements ActivityDepartureService {
    private final ActivityDepartureRepository activitySessionRepository;
    private final MessageLang messageLang;

    @Transactional
    public void reserveSeats(UUID id, Integer peopleCount) {
        ActivityDeparture activityDeparture = activitySessionRepository.findByIdWithLock(id);
        log.info("Provo a riservare i posti");

        if (activityDeparture.getStatus() != Status.CONFIRMED) {
            throw new StatusException(messageLang.getMessage("error.departure.not.confirmed"));
        }

        int newSlotsNumber = activityDeparture.getAvailableSlots() - peopleCount;
        if (newSlotsNumber >= 0) {
            log.info("Posti riservati per il travel {}", id);
            activityDeparture.setAvailableSlots(newSlotsNumber);
        } else {
            log.warn("Posti non disponibili per il travel {}", id);
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

    public boolean isValidActivity(UUID travelDepartureId){
        return activitySessionRepository.existsById(travelDepartureId);
    }

    public BigDecimal getActivityPriceById(UUID activityDepartureId){
        return activitySessionRepository.findPriceById(activityDepartureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.departure.not.found")));
    }

    @Override
    public List<ActivityBatchResponse> getActivitiesBatch(List<UUID> activityIds) {
        List<ActivityDeparture> departures = activitySessionRepository.findAllById(activityIds);
        return departures.stream()
                .map(d -> new ActivityBatchResponse(
                        d.getActivity().getId(),
                        d.getId(),
                        d.getActivity() != null ? d.getActivity().getName() : null,
                        d.getStartTimestamp(),
                        d.getEndTimestamp()
                ))
                .toList();
    }
}
