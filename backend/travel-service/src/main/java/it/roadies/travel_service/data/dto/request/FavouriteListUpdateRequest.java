package it.roadies.travel_service.data.dto.request;

import it.roadies.travel_service.data.entity.enumerations.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavouriteListUpdateRequest {
    @NotBlank
    private String name;

    @NotNull
    private Visibility visibility;
}
