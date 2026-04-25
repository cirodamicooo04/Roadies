package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dto.response.TagResponse;

import java.util.List;

public interface TagService {
    List<TagResponse> getTags();
}
