package it.roadies.review_service.service.client;

import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import it.roadies.review_service.clients.TravelServiceClient;
import it.roadies.review_service.conf.i8n.MessageLang;
import it.roadies.review_service.data.entity.ReviewType;
import it.roadies.review_service.exceptions.ServiceUnavailableException;
import it.roadies.review_service.exceptions.TravelNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelService {

    private final TravelServiceClient travelServiceClient;
    private final MessageLang messageLang;

    @Retry(name = "travelService")
    @CircuitBreaker(name = "travelService")
    public void verifyTravelExists(UUID travelId, ReviewType reviewType) {
        log.info("Verifica esistenza viaggio/attività");

        try {
            if (reviewType == ReviewType.TRAVEL) {
                travelServiceClient.verifyTravelExists(travelId);
                log.info("Esistenza viaggio {} verificata con successo", travelId);
            } else {
                travelServiceClient.verifyActivityExistsAndIsNotIntoATravel(travelId);
                log.info("Esistenza attività {} verificata con successo", travelId);
            }

        } catch (FeignException.NotFound e) {
            if (reviewType == ReviewType.ACTIVITY) {
                log.warn("Attività {} non esiste", travelId);
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

        } catch (Exception e) {
            log.error("Errore imprevisto durante la verifica viaggio/attività", e);
            throw new ServiceUnavailableException(messageLang.getMessage("error.any.travel"));
        }
    }

}
