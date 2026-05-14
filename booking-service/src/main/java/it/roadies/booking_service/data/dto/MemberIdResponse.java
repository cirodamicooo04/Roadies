package it.roadies.booking_service.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberIdResponse {
    private UUID memberId;
    private String firstName;
    private String lastName;
    private List<UUID> documentIds;
}
