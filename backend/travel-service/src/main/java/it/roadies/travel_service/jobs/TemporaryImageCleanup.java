package it.roadies.travel_service.jobs;

import it.roadies.travel_service.data.dao.ImageRepository;
import it.roadies.travel_service.data.entity.Image;
import it.roadies.travel_service.data.entity.enumerations.ImageStatus;
import it.roadies.travel_service.services.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemporaryImageCleanup {

    private final ImageRepository imageRepository;
    private final ImageService imageService;

    //Questo metodo viene eseguito ogni giorno alle 3:00
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupTemporaryImages() {
        log.info("Starting cleaning up temporary images");

        LocalDateTime limitDate = LocalDateTime.now().minusHours(24);

        List<Image> temporaryImages = imageRepository.findAllByStatusAndCreatedAtBefore(ImageStatus.TEMPORARY, limitDate);
        if (temporaryImages.isEmpty()) {
            log.info("No temporary images to clean up");
            return;
        }

        int deletedCount = 0;
        for (Image image : temporaryImages) {
            try {
                imageService.deleteImageFromMinio(image.getPath());
                imageRepository.delete(image);
                deletedCount++;
            } catch (Exception e) {
                log.error("Error deleting temporary image with Id: {} {}",image.getId(), e.getMessage());
            }
        }
        log.info("Deleted {} temporary images", deletedCount);
    }
}
