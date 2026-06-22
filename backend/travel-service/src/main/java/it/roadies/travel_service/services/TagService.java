package it.roadies.travel_service.services;

import it.roadies.travel_service.data.dto.response.TagResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public interface TagService {
    List<TagResponse> getTags();
    TagResponse addTag(String name);
}
