package it.roadies.travel_service.services.impl;

import it.roadies.travel_service.conf.i8n.MessageLang;
import it.roadies.travel_service.controller.client.BookingClient;
import it.roadies.travel_service.data.dao.*;
import it.roadies.travel_service.data.dao.specification.TravelSpecification;
import it.roadies.travel_service.data.dto.event.ReviewTravelUpdateEvent;
import it.roadies.travel_service.data.dto.request.*;
import it.roadies.travel_service.data.dto.response.*;
import it.roadies.travel_service.data.entity.*;
import it.roadies.travel_service.data.entity.enumerations.Continent;
import it.roadies.travel_service.data.entity.enumerations.ImageStatus;
import it.roadies.travel_service.data.entity.enumerations.Status;
import it.roadies.travel_service.data.mapper.ActivityMapper;
import it.roadies.travel_service.data.mapper.TravelMapper;
import it.roadies.travel_service.services.ImageService;
import it.roadies.travel_service.services.TravelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.View;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelServiceImpl implements TravelService {
    private final TravelMapper travelMapper;
    private final TagRepository tagRepository;
    private final TravelRepository travelRepository;
    private final TravelDepartureRepository travelDepartureRepository;
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final BookingClient bookingClient;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final MessageLang messageLang;

    private void validateTravelLogic(Travel travel){
        for (TravelDeparture departure : travel.getDepartures()){
            if (!departure.getStartDate().isBefore(departure.getEndDate())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departures.dates.not.valid"));
            }
        }

        if (travel.getActivities() != null) {
            for (Activity activity : travel.getActivities()) {
                if (activity.getDayNumber() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.daynumber.not.present");
                }
                if (activity.getDayNumber() > travel.getDurationDays() || activity.getDayNumber() < 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.daynumber.not.valid"));
                }
//                if (activity.getContinent() != travel.getContinent()) {
//                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.activity.continent.not.valid"));
//                }
//                if (activity.getCountry() != null && !activity.getCountry().equals(travel.getCountry())) {
//                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.activity.country.not.valid"));
//                }
            }
        }
    }

    @Transactional
    public TravelResponse createTravel(TravelCreateRequest travelCreateRequest, String ownerId){
        Travel travel = travelMapper.toEntity(travelCreateRequest, ownerId);
        validateTravelLogic(travel);

        if (travelCreateRequest.getActivities() != null ) {
            boolean activityWithDepartures = travelCreateRequest.getActivities().stream().anyMatch(a -> a.getDepartures() != null);
            if (activityWithDepartures) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.activities.with.departures.travel"));
            }
        }

        Map<UUID, Tag> tagsMap = tagRepository.findAll().stream().collect(Collectors.toMap(Tag::getId, t -> t));

        if (!tagsMap.isEmpty()) {
            if (travelCreateRequest.getTagScores() == null) {
                log.info("No tag scores provided first");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.tag.number.not.valid"));
            }
            Set<UUID> providedTagIds = travelCreateRequest.getTagScores().stream()
                    .map(TravelTagRequest::getTagId)
                    .collect(Collectors.toSet());
            
            if (!providedTagIds.containsAll(tagsMap.keySet())) {
                log.info("Provided tag ids {}, tags in db: {}", providedTagIds, tagsMap.keySet());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.tag.number.not.valid"));
            }
        }

        if(travelCreateRequest.getTagScores() != null){
            Map<UUID, TravelTagRequest> uniqueTags = travelCreateRequest.getTagScores().stream()
                    .collect(Collectors.toMap(TravelTagRequest::getTagId, ts -> ts, (existing, replacement) -> existing));

            for (TravelTagRequest ts : uniqueTags.values()) {
                Tag tag = tagsMap.get(ts.getTagId());
                if (tag == null) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.tag.not.found"));}
                TravelTag travelTag = new TravelTag();
                travelTag.setTravel(travel);
                travelTag.setTag(tag);
                travelTag.setScore(ts.getScore());
                travel.getTagScores().add(travelTag);
            }
        }

        Travel savedTravel = travelRepository.save(travel);
        if (travelCreateRequest.getImageIds() != null && !travelCreateRequest.getImageIds().isEmpty()) {
            List<Image> images = imageRepository.findAllById(travelCreateRequest.getImageIds());
            images.forEach(i -> {
                if (!i.getOwnerId().equals(ownerId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.image.not.owned"));
                }

                if (i.getActivity() != null || (i.getTravel() != null && !i.getTravel().getId().equals(travel.getId()))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.image.already.associated"));
                }

                i.setTravel(savedTravel);
                i.setStatus(ImageStatus.PERMANENT);
            });
            imageRepository.saveAll(images);
            savedTravel.setImages(images);
        }

        if (travelCreateRequest.getActivities() != null && !travelCreateRequest.getActivities().isEmpty()) {
            for (int j = 0; j < travelCreateRequest.getActivities().size(); j++) {
                ActivityCreateRequest activityRequest = travelCreateRequest.getActivities().get(j);
                if (activityRequest.getImageIds() != null && !activityRequest.getImageIds().isEmpty()) {
                    Activity savedActivity = savedTravel.getActivities().get(j);
                    List<Image> activityImages = imageRepository.findAllById(activityRequest.getImageIds());
                    activityImages.forEach(img -> {
                        if (!img.getOwnerId().equals(ownerId)) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.image.not.owned"));
                        }
                        if (img.getTravel() != null || (img.getActivity() != null && !img.getActivity().getId().equals(savedActivity.getId()))) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.image.already.associated"));
                        }
                        img.setActivity(savedActivity);
                        img.setStatus(ImageStatus.PERMANENT);
                    });
                    imageRepository.saveAll(activityImages);
                    savedActivity.setImages(activityImages);
                }
            }
        }

        return travelMapper.toResponse(travel);
    }

    public TravelResponse getTravelById(UUID id){
        Travel travel = travelRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        return travelMapper.toResponse(travel);
    }

    @Transactional
    public void deleteTravelById(UUID id, String ownerId){
        Travel travel = travelRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        if (!ownerId.equals(travel.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.travel.not.owned"));
        }

        boolean hasConfirmedDepartures = travel.getDepartures().stream().anyMatch(d -> d.getStatus() == Status.CONFIRMED);
        if (hasConfirmedDepartures) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.travel.has.confirmed.departures"));}

        if (travel.getImages() != null){
            for (Image image : travel.getImages()) {
                imageService.deleteImageFromMinio(image.getPath());
            }
        }
        travelRepository.delete(travel);
    }

    @Transactional
    public TravelResponse updateTravel(TravelUpdateRequest travelUpdateRequest, String ownerId, UUID travelId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        //controllo sull'owner
        if (!ownerId.equals(travel.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.travel.not.owned"));
        }

        //check se ci sono partenze confermate
        boolean hasConfirmedDepartures = travel.getDepartures().stream().anyMatch(d -> d.getStatus() == Status.CONFIRMED);
        if (hasConfirmedDepartures) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.travel.has.confirmed.departures"));}

        travelMapper.updateTravelFromDto(travelUpdateRequest, travel);
        validateTravelLogic(travel);

        Map<UUID, Tag> tagsMap = tagRepository.findAll().stream().collect(Collectors.toMap(Tag::getId, t -> t));

        if (travelUpdateRequest.getTagScores() != null){
            Map<UUID, TravelTagRequest> incomingTags = travelUpdateRequest.getTagScores().stream()
                    .collect(Collectors.toMap(TravelTagRequest::getTagId, ts -> ts, (existing, replacement) -> existing));

            for (TravelTag existingTag : travel.getTagScores()) {
                TravelTagRequest incoming = incomingTags.get(existingTag.getTag().getId());
                if (incoming != null) {
                    existingTag.setScore(incoming.getScore());
                    incomingTags.remove(existingTag.getTag().getId());
                }
            }

            for (TravelTagRequest ts : incomingTags.values()) {
                Tag tag = tagsMap.get(ts.getTagId());
                if (tag == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.tag.not.found"));
                }
                TravelTag travelTag = new TravelTag();
                travelTag.setTravel(travel);
                travelTag.setTag(tag);
                travelTag.setScore(ts.getScore());
                travel.getTagScores().add(travelTag);
            }
        }

        if (travelUpdateRequest.getImageIds() != null) {
            List<Image> requestedImages = imageRepository.findAllById(travelUpdateRequest.getImageIds());

            List<Image> currentImages = travel.getImages();
            for (Image currentImage : currentImages) {
                if (!travelUpdateRequest.getImageIds().contains(currentImage.getId())) {
                    imageService.deleteImageFromMinio(currentImage.getPath());
                    imageRepository.delete(currentImage);
                }
            }

            for (Image img : requestedImages) {
                if (!img.getOwnerId().equals(ownerId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.image.not.owned"));
                }

                if (img.getActivity() != null || (img.getTravel() != null && !img.getTravel().getId().equals(travel.getId()))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.image.already.associated"));
                }


                if (img.getStatus() == ImageStatus.TEMPORARY) {
                    img.setTravel(travel);
                    img.setStatus(ImageStatus.PERMANENT);
                }
            }
            travel.setImages(requestedImages);
        }

        Travel updatedTravel = travelRepository.save(travel);
        return travelMapper.toResponse(updatedTravel);
    }

    @Transactional(readOnly = true)
    public Page<TravelSummaryResponse> searchTravels(Continent continent,String country,String destination, BigDecimal minPrice, BigDecimal maxPrice, Integer minDurationDays, Integer maxDurationDays, Pageable pageable){
        Specification<Travel> travelSpecification = Specification.where(TravelSpecification.hasDestination(destination))
                .and(TravelSpecification.hasPriceRange(minPrice, maxPrice))
                .and(TravelSpecification.hasDurationRange(minDurationDays, maxDurationDays))
                .and(TravelSpecification.hasContinent(continent))
                .and(TravelSpecification.hasCountry(country));

        Page<Travel> travels = travelRepository.findAll(travelSpecification, pageable);
        return travels.map(travelMapper::toSummaryResponse);
    }

    @Transactional
    public List<TravelSummaryResponse> getRecommendedTravels(String userId) {
        if (userId == null) {
            return travelRepository.findTop10ByOrderByCreatedAtDesc().stream().map(travelMapper::toSummaryResponse).toList();
        }
        List<UUID> pastTravelsIds = bookingClient.getUserBookings();
        log.info("User past bookings: {}", pastTravelsIds);
        //Se non ha mai effettuato alcun viaggio, restituisco gli ultimi 10 viaggi creati
        if (pastTravelsIds.isEmpty()) return travelRepository.findTop10ByOrderByCreatedAtDesc().stream().map(travelMapper::toSummaryResponse).toList();

        List<Travel> userPastTravels = travelRepository.findAllById(pastTravelsIds);

        Map<UUID, Double> userAverageScores = userPastTravels.stream().flatMap(t -> t.getTagScores().stream()).collect(Collectors.groupingBy(tt -> tt.getTag().getId(), Collectors.averagingInt(TravelTag::getScore)));

        //Mi prendo massimo 500 viaggi per non sovraccaricare troppo
        List<Travel> travels = travelRepository.findCandidateTravels(pastTravelsIds, PageRequest.of(0, 500));

        //Calcolo lo scarto dei tag per ogni viaggio
        return travels.stream()
                .map(travel -> {
                    double totalPenalty = 0.0;

                    int totalTags = travel.getTagScores().size();

                    for (TravelTag tripTag : travel.getTagScores()) {
                        UUID tagId = tripTag.getTag().getId();
                        double tripScore = tripTag.getScore();
                        double userScore = userAverageScores.getOrDefault(tagId, 3.0);

                        totalPenalty += Math.abs(tripScore - userScore);
                    }

                    double maxPossiblePenalty = totalTags * 4.0;
                    double matchPercentage = (1.0 - (totalPenalty / maxPossiblePenalty)) * 100;

                    return Map.entry(travel, matchPercentage);
                })
                .sorted(Map.Entry.<Travel, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .map(travelMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizerTravelsActivityResponse getOrganizerTravelsActivity(String ownerId){
        List<Travel> travels = travelRepository.findAllByOwnerId(ownerId);
        List<TravelSummaryResponse> travelsSummary = travels.stream().map(travelMapper::toSummaryResponse).toList();

        List<Activity> activities = activityRepository.findAllByOwnerId(ownerId);
        List<ActivitySummaryResponse> activitiesSummary = activities.stream().filter(a -> a.getTravel() == null).map(activityMapper::toSummaryResponse).toList();

        OrganizerTravelsActivityResponse response = new OrganizerTravelsActivityResponse();
        response.setTravels(travelsSummary);
        response.setActivities(activitiesSummary);
        return response;
    }

    @Transactional
    public TravelDepartureResponse addDeparture(UUID travelId, TravelDepartureCreateRequest departureCreateRequest, String ownerId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        if (!travel.getOwnerId().equals(ownerId)) {throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.travel.not.owned"));}
        TravelDeparture departure = travelMapper.toDepartureEntity(departureCreateRequest);
        departure.setTravel(travel);

        travel.getDepartures().add(departure);
        validateTravelLogic(travel);

        TravelDeparture savedDeparture = travelDepartureRepository.save(departure);
        return travelMapper.toDepartureResponse(savedDeparture);
    }

    @Transactional
    public void deleteDeparture(UUID travelId, UUID departureId, String ownerId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        if (!travel.getOwnerId().equals(ownerId)) {throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.travel.not.owned"));}
        TravelDeparture departure = travelDepartureRepository.findById(departureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.departure.not.found")));
        if (!departure.getTravel().getId().equals(travelId)){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.not.found.in.this.travel"));}
        if (departure.getStatus() == Status.CONFIRMED){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.already.confirmed"));}
        travel.getDepartures().remove(departure);
        travelDepartureRepository.delete(departure);
    }

    @Transactional(readOnly = true)
    public List<TravelDepartureResponse> getTravelDepartures(UUID travelId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        List<TravelDeparture> departures = travel.getDepartures();
        return departures.stream().filter(d -> d.getStartDate().isAfter(LocalDate.now())).sorted(Comparator.comparing(TravelDeparture::getStartDate)).map(travelMapper::toDepartureResponse).toList();
    }

    @Transactional
    public TravelDepartureResponse updateDeparture(UUID travelId, UUID departureId, TravelDepartureUpdateRequest request, String ownerId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        if (!travel.getOwnerId().equals(ownerId)){throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.travel.not.owned"));}
        TravelDeparture departure = travelDepartureRepository.findById(departureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.departure.not.found")));
        if (!departure.getTravel().getId().equals(travelId)){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.not.found.in.this.travel"));}
        if (departure.getStatus().equals(Status.CONFIRMED)){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.already.confirmed"));}

        travelMapper.updateDepartureEntity(request, departure);
        validateTravelLogic(travel);

        travelDepartureRepository.save(departure);
        return travelMapper.toDepartureResponse(departure);
    }

    @Transactional
    public TravelDepartureResponse confirmDeparture(UUID travelId, UUID departureId, String ownerId){
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        if (!travel.getOwnerId().equals(ownerId)){throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.travel.not.owned"));}

        TravelDeparture departure = travelDepartureRepository.findById(departureId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.departure.not.found")));
        if (!departure.getTravel().getId().equals(travelId)){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.not.found.in.this.travel"));}
        if (departure.getStatus() == Status.CONFIRMED){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.departure.already.confirmed"));}
        departure.setStatus(Status.CONFIRMED);
        travelDepartureRepository.save(departure);
        return travelMapper.toDepartureResponse(departure);
    }

    @Override
    public List<String> getUniqueDestinations(Continent continent, String country) {
        Specification<Travel> travelSpecification = Specification.where(TravelSpecification.hasContinent(continent)).and(TravelSpecification.hasCountry(country));

        List<Travel> travels = travelRepository.findAll(travelSpecification);
        return travels.stream().map(Travel::getDestination).filter(Objects::nonNull).distinct().sorted().toList();
    }

    @Transactional
    public void updateTravelReviews(ReviewTravelUpdateEvent event) {
        Travel travel = travelRepository.findById(event.getTravelId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        travel.setAverageRating(event.getAverageRating());
        travel.setNumberOfRatings(event.getNumberOfRatings());
        travelRepository.save(travel);
    }

    @Transactional
    public TravelResponse addActivity(UUID travelId, ActivityCreateRequest request, String ownerId) {
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        if (!travel.getOwnerId().equals(ownerId)) {throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.travel.not.owned"));}

        boolean hasConfirmedDepartures = travel.getDepartures().stream().anyMatch(d -> d.getStatus() == Status.CONFIRMED);
        if (hasConfirmedDepartures) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.travel.has.confirmed.departures"));}

        if (request.getDepartures() != null && !request.getDepartures().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.activity.is.standalone.to.travel"));
        }
        Activity activity = activityMapper.toEntity(request);
        activity.setTravel(travel);
        activity.setOwnerId(ownerId);

        travel.getActivities().add(activity);
        validateTravelLogic(travel);
        Activity savedActivity = activityRepository.save(activity);
        
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            List<Image> images = imageRepository.findAllById(request.getImageIds());
            images.forEach(img -> {
                if (!img.getOwnerId().equals(ownerId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.image.not.owned"));
                }
                if (img.getTravel() != null || (img.getActivity() != null && !img.getActivity().getId().equals(savedActivity.getId()))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.image.already.associated"));
                }
                img.setActivity(savedActivity);
                img.setStatus(ImageStatus.PERMANENT);
            });
            imageRepository.saveAll(images);
            savedActivity.setImages(images);
        }
        
        return travelMapper.toResponse(travel);
    }

    @Transactional
    public void deleteTravelActivity(UUID travelId, UUID activityId, String ownerId) {
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        if (!travel.getOwnerId().equals(ownerId)) {throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.travel.not.owned"));}

        boolean hasConfirmedDepartures = travel.getDepartures().stream().anyMatch(d -> d.getStatus() == Status.CONFIRMED);
        if (hasConfirmedDepartures) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.travel.has.confirmed.departures"));}

        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        if (!activity.getTravel().getId().equals(travelId)){throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.activity.not.found.in.this.travel"));}

        travel.getActivities().remove(activity);
        activityRepository.delete(activity);
    }

    @Transactional
    public TravelResponse updateTravelActivity(UUID travelId, UUID activityId, ActivityUpdateRequest request, String ownerId) {
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.travel.not.found")));
        if (!travel.getOwnerId().equals(ownerId)) {throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.travel.not.owned"));}

        boolean hasConfirmedDepartures = travel.getDepartures().stream().anyMatch(d -> d.getStatus() == Status.CONFIRMED);
        if (hasConfirmedDepartures) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.travel.has.confirmed.departures"));}

        Activity activity = activityRepository.findById(activityId).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("error.activity.not.found")));
        if (!activity.getTravel().getId().equals(travelId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageLang.getMessage("error.activity.not.found.in.this.travel"));

        activityMapper.updateActivityFromDto(request, activity);
        validateTravelLogic(travel);

        if (request.getImageIds() != null) {
            List<Image> requestedImages = imageRepository.findAllById(request.getImageIds());
            List<Image> currentImages = activity.getImages();

            for (Image currentImage : currentImages) {
                if (!request.getImageIds().contains(currentImage.getId())) {
                    imageService.deleteImageFromMinio(currentImage.getPath());
                    imageRepository.delete(currentImage);
                }
            }

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
        return travelMapper.toResponse(travel);
    }

    //REVIEW AREA
    public boolean isValidTravel(UUID travelId){
        return travelRepository.existsById(travelId);
    }
}


