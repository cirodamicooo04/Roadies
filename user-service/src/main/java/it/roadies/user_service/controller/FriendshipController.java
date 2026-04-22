package it.roadies.user_service.controller;

import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.entities.enumeration.Status;
import it.roadies.user_service.services.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;

    @PostMapping("/request/{receiverUsername}")
    public ResponseEntity<Void> send(@RequestParam String myId, @PathVariable String receiverUsername) {
        friendshipService.sendRequest(myId, receiverUsername);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/respond/{friendshipId}")
    public ResponseEntity<Void> respond(
            @PathVariable UUID friendshipId,
            @RequestParam Status status,
            @RequestParam String myId) {
        friendshipService.respondToRequest(friendshipId, status, myId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list/{myId}")
    public ResponseEntity<List<UserProfileResponseDTO>> getFriends(@PathVariable String myId) {
        List<UserProfileResponseDTO> friends = friendshipService.getFriendsList(myId);
        return ResponseEntity.ok(friends);
    }
}