package it.roadies.travel_service.controller;

import it.roadies.travel_service.data.dto.response.TagResponse;
import it.roadies.travel_service.services.ActivityService;
import it.roadies.travel_service.services.TagService;
import it.roadies.travel_service.services.TravelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@RestController
@RequestMapping("api/v1/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final TagService tagService;
    private final TravelService travelService;
    private final ActivityService activityService;

    @GetMapping("/public/tags")
    public ResponseEntity<List<TagResponse>> getTags(){
        List<TagResponse> responses = tagService.getTags();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/public/destinations")
    public ResponseEntity<Set<String>> getDestinations(){
        Set<String> destinations = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        destinations.addAll(travelService.getUniqueDestinations());
        destinations.addAll(activityService.getUniqueDestinations());
        return ResponseEntity.ok(destinations);
    }
}
