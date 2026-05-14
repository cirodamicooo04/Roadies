package it.roadies.travel_service.controller;

import it.roadies.travel_service.data.dto.response.TagResponse;
import it.roadies.travel_service.data.entity.enumerations.Continent;
import it.roadies.travel_service.services.ActivityService;
import it.roadies.travel_service.services.TagService;
import it.roadies.travel_service.services.TravelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

//    @GetMapping("/public/destinations")
//    public ResponseEntity<Set<String>> getDestinations(@RequestParam(required = false) Continent continent, @RequestParam(required = false) String country){
//        Set<String> destinations = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
//        destinations.addAll(travelService.getUniqueDestinations(continent,country));
//        destinations.addAll(activityService.getUniqueDestinations(continent,country));
//        return ResponseEntity.ok(destinations);
//    }

    @GetMapping("/public/continents")
    public ResponseEntity<List<Continent>> getContinents(){
        return ResponseEntity.ok(List.of(Continent.values()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tags")
    public ResponseEntity<TagResponse> addTag(@RequestBody @Size(min = 3, max = 25) String name){
        TagResponse response = tagService.addTag(name);
        return ResponseEntity.ok(response);
    }
}
