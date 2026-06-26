package it.roadies.travel_service.data.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class TagResponse {
    private UUID id;
    private String name;
}
