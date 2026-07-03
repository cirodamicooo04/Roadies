package it.roadies.travel_service.services.impl;

import it.roadies.shared.i18n.MessageLang;
import it.roadies.travel_service.data.dao.ActivityDepartureRepository;
import it.roadies.travel_service.data.dao.ActivityRepository;
import it.roadies.travel_service.data.dao.ImageRepository;
import it.roadies.travel_service.data.dao.specification.ActivitySpecification;
import it.roadies.shared.contracts.ReviewActivityUpdateEvent;
import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureUpdateRequest;
import it.roadies.travel_service.data.dto.request.ActivityUpdateRequest;
import it.roadies.travel_service.data.dto.response.ActivityDepartureResponse;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.data.dto.response.ActivitySummaryResponse;
import it.roadies.travel_service.data.entity.Activity;
import it.roadies.travel_service.data.entity.ActivityDeparture;
import it.roadies.travel_service.data.entity.Image;
import it.roadies.travel_service.data.entity.TravelDeparture;
import it.roadies.travel_service.data.entity.enumerations.Continent;
import it.roadies.travel_service.data.entity.enumerations.ImageStatus;
import it.roadies.travel_service.data.entity.enumerations.Status;
import it.roadies.travel_service.data.mapper.ActivityMapper;
import it.roadies.travel_service.exceptions.NotValidTravelId;
import it.roadies.travel_service.exceptions.StatusException;
import it.roadies.travel_service.services.ActivityService;
import it.roadies.travel_service.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityMapper activityMapper;
    private final ActivityRepository activityRepository;
    private final ActivityDepartureRepository activityDepartureRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final MessageLang messageLang;

    private void validateActivity(Activity activity){
        if (activity.getTravel() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.standalone.activity.cant.have.travel"));

        for (ActivityDeparture departure : activity.getDepartures()){
            //price check
            if (departure.getPrice() == null || departure.getPrice().compareTo(BigDecimal.ZERO) <= 0){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departures.price.not.valid"));
            }

            if (!departure.getStartTimestamp().isBefore(departure.getEndTimestamp())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departures.dates.not.valid"));
            }
            //same day check
            if (!departure.getStartTimestamp().toLocalDate().isEqual(departure.getEndTimestamp().toLocalDate())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departures.dates.not.equal"));
            }
        }
    }

    @Transactional
    public ActivityResponse createActivity(ActivityCreateRequest request, String ownerId){
        Activity activity = activityMapper.toEntity(request);
        validateActivity(activity);
        activity.setOwnerId(ownerId);

        Activity savedActivity = activityRepository.save(activity);

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            List<Image> images = imageRepository.findAllById(request.getImageIds());
            images.forEach(i -> {
                // Controllo sicurezza: L'immagine è tua?
                if (!i.getOwnerId().equals(ownerId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.image.not.owned"));
                }
                // Controllo sicurezza: L'immagine non deve essere già associata a qualcos'altro
                if (i.getTravel() != null || (i.getActivity() != null && !i.getActivity().getId().equals(savedActivity.getId()))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.image.already.associated"));
                }

                i.setActivity(savedActivity);
                i.setStatus(ImageStatus.PERMANENT);
            });
            imageRepository.saveAll(images);
            savedActivity.setImages(images);
        }
        return activityMapper.toResponse(activity);
    }


    public ActivityResponse getActivityById(UUID id){
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        return activityMapper.toResponse(activity);
    }

    @Transactional
    public void deleteActivityById(UUID id, String ownerId){
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.activity.not.owned"));

        if (activity.getImages() != null) {
            for (Image image : activity.getImages()) {
                imageService.deleteImageFromMinio(image.getPath());
            }
        }

        activityRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ActivitySummaryResponse> searchActivities(Continent continent,String country,String destination, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Activity> specification = Specification.where(ActivitySpecification.hasDestination(destination))
                .and(ActivitySpecification.hasPriceRange(minPrice, maxPrice))
                        .and(ActivitySpecification.isStandalone())
                        .and(ActivitySpecification.hasContinent(continent))
                        .and(ActivitySpecification.hasCountry(country));

        Page<Activity> activities = activityRepository.findAll(specification, pageable);
        return activities.map(activityMapper::toSummaryResponse);
    }

    @Transactional
    public ActivityDepartureResponse addDeparture(UUID activityId, ActivityDepartureCreateRequest request,  String ownerId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.activity.not.owned"));

        ActivityDeparture departure = activityMapper.toDepartureEntity(request);
        departure.setActivity(activity);

        activity.getDepartures().add(departure);
        validateActivity(activity);

        activityDepartureRepository.save(departure);
        return activityMapper.toDeparturesResponse(departure);
    }

    @Transactional
    public void deleteDeparture(UUID activityId, UUID departureId, String ownerId){
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.activity.not.owned"));

        ActivityDeparture departure = activityDepartureRepository.findById(departureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.departure.not.found")));
        if (!departure.getActivity().getId().equals(activityId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.not.found.in.this.activity"));

        if (departure.getStatus() == Status.CONFIRMED) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.already.confirmed"));
        activity.getDepartures().remove(departure);
        activityDepartureRepository.delete(departure);
    }

    @Transactional
    public ActivityDepartureResponse updateDeparture(UUID activityId, UUID departureId, ActivityDepartureUpdateRequest request, String ownerId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.activity.not.owned"));

        ActivityDeparture departure = activityDepartureRepository.findById(departureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.departure.not.found")));
        if (!departure.getActivity().getId().equals(activityId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.not.found.in.this.activity"));
        if (departure.getStatus() == Status.CONFIRMED) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.already.confirmed"));

        activityMapper.updateDepartureEntity(request, departure);
        validateActivity(activity);

        if (request.getMaxSlots() != null){
            departure.setMaxSlots(request.getMaxSlots());
        }

        activityDepartureRepository.save(departure);
        return activityMapper.toDeparturesResponse(departure);
    }

    @Transactional(readOnly = true)
    public List<ActivityDepartureResponse> getDepartures(UUID activityId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        return activity.getDepartures().stream().filter(d -> d.getStartTimestamp().isAfter(LocalDateTime.now())).sorted(Comparator.comparing(ActivityDeparture::getStartTimestamp)).map(activityMapper::toDeparturesResponse).toList();
    }

    @Transactional
    public ActivityDepartureResponse confirmDeparture(UUID activityId, UUID departureId, String ownerId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.activity.not.owned"));

        ActivityDeparture departure = activityDepartureRepository.findById(departureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.departure.not.found")));
        if (!departure.getActivity().getId().equals(activityId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.not.found.in.this.activity"));
        if (departure.getStatus() == Status.CONFIRMED) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.already.confirmed"));
        departure.setStatus(Status.CONFIRMED);
        activityDepartureRepository.save(departure);
        return activityMapper.toDeparturesResponse(departure);
    }

    public List<String> getUniqueDestinations(Continent continent, String country) {
        Specification<Activity> specification = Specification.where(ActivitySpecification.hasContinent(continent)).and(ActivitySpecification.hasCountry(country));
        List<Activity> activities = activityRepository.findAll(specification);

        return activities.stream().map(Activity::getDestination).filter(Objects::nonNull).distinct().sorted().toList();
    }

    @Transactional
    public void updateActivityReviews(ReviewActivityUpdateEvent event) {
        Activity activity = activityRepository.findById(event.getActivityId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        activity.setAverageRating(event.getAverageRating());
        activity.setNumberOfRatings(event.getNumberOfRatings());
        activityRepository.save(activity);
    }

    @Transactional
    public ActivityResponse updateActivity(UUID id, ActivityUpdateRequest request, String ownerId) {
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        if (!activity.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.activity.not.owned"));

        boolean hasConfirmedDepartures = activity.getDepartures().stream().anyMatch(d -> d.getStatus() == Status.CONFIRMED);
        if (hasConfirmedDepartures) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.activity.has.confirmed.departures"));

        activityMapper.updateActivityFromDto(request, activity);
        validateActivity(activity);

        if (request.getImageIds() != null) {
            List<Image> requestedImages = imageRepository.findAllById(request.getImageIds());
            List<Image> currentImages = activity.getImages();

            for (Image currentImage : currentImages) {
                if (!request.getImageIds().contains(currentImage.getId())) {
                    imageService.deleteImageFromMinio(currentImage.getPath());
                    imageRepository.delete(currentImage);
                }
            }

            //associo le nuove immagini
            for (Image img : requestedImages) {
                if (!img.getOwnerId().equals(ownerId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.image.not.owned"));
                }
                if (img.getTravel() != null || (img.getActivity() != null && !img.getActivity().getId().equals(activity.getId()))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.image.already.associated"));
                }

                if (img.getStatus() == ImageStatus.TEMPORARY) {
                    img.setActivity(activity);
                    img.setStatus(ImageStatus.PERMANENT);
                }
            }
            activity.setImages(requestedImages);
        }


        activityRepository.save(activity);
        return activityMapper.toResponse(activity);
    }

    //REVIEW AREA
    @Override
    public boolean isValidActivityAndIsNotIntoATravel(UUID activityId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(()->new StatusException(""));
        if (activity.getTravel()!=null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage(""));
        }
        return true;
    }
}
