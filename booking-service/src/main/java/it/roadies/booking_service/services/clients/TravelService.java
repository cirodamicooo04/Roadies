package it.roadies.booking_service.services.clients;

import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import it.roadies.booking_service.clients.TravelServiceClient;
import it.roadies.booking_service.config.i8n.MessageLang;
import it.roadies.booking_service.exceptions.ServiceUnavailableException;
import it.roadies.booking_service.exceptions.TravelNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelService {

    private final TravelServiceClient travelServiceClient;
    private final MessageLang messageLang;

    @Retry(name = "travelService")
    @CircuitBreaker(name = "travelService")
    public void verifyTravelExists(UUID travelId, UUID activityId) {
        log.info("Verifica esistenza viaggio/attività");

        try {
            if (travelId == null) {
                travelServiceClient.verifyActivityExists(activityId);
                log.info("Esistenza attività {} verificata con successo", activityId);
            } else {
                travelServiceClient.verifyTravelExists(travelId);
                log.info("Esistenza viaggio {} verificata con successo", travelId);
            }

        } catch (FeignException.NotFound e) {
            if (travelId == null) {
                log.warn("Attività {} non esiste", activityId);
                throw new TravelNotFoundException(messageLang.getMessage("error.activity.not.found"));
            }

            log.warn("Viaggio {} non esiste", travelId);
            throw new TravelNotFoundException(messageLang.getMessage("error.travel.not.found"));

        } catch (FeignException.Unauthorized | FeignException.Forbidden e) {
            log.warn("Accesso non autorizzato al travel-service. Status: {}", e.status());
            throw new ServiceUnavailableException(messageLang.getMessage("error.service.authorization"));

        } catch (RetryableException e) {
            log.error("Travel-service non raggiungibile durante la verifica viaggio/attività", e);
            throw new ServiceUnavailableException(messageLang.getMessage("error.retry.travel"));

        } catch (FeignException e){
            log.error("Errore imprevisto durante la verifica viaggio/attività", e);
            throw new ServiceUnavailableException(messageLang.getMessage("error.any.travel"));
        }
    }


    @CircuitBreaker(name = "travelService")
    @Retry(name = "travelService")
    public BigDecimal priceForTravel(UUID travelId, UUID activityId){
        log.info("Recupero prezzo viaggio/attività");
        try {
            if (travelId!=null) {
                log.info("Prezzo viaggio recuperato con successo: {}", travelId);
                return travelServiceClient.getTravelPrice(travelId);
            } else return travelServiceClient.getActivityPrice(activityId);

        } catch (Exception e) {
            log.error("Errore imprevisto durante la verifica viaggio/attività", e);
            throw new ServiceUnavailableException(messageLang.getMessage("error.any.travel"));
        }
    }
}
