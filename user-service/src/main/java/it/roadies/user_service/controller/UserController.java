package it.roadies.user_service.controller;

import it.roadies.user_service.data.dto.request.UserSyncRequestDTO;
import it.roadies.user_service.data.dto.response.UserProfileResponseDTO;
import it.roadies.user_service.data.dto.result.UserSyncResult;
import it.roadies.user_service.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @PostMapping("/sync")
    public ResponseEntity<UserProfileResponseDTO> syncUser(@RequestBody UserSyncRequestDTO requestDto) {

        UserSyncResult result = userService.syncUser(requestDto);

        //Gestiamo due stati con 200 se l'utente esisteva e con 201 se l'utente non esisteva
        if (result.isNewUser()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.profile());
        }

        return ResponseEntity.ok(result.profile());
    }

    @GetMapping("/me/{keycloakId}")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(@PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.getProfile(keycloakId));
    }

    @GetMapping("/search/{username}")
    public ResponseEntity<UserProfileResponseDTO> searchUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.getProfileByUsername(username));
    }

    @PutMapping("/update/{keycloakId}")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @PathVariable String keycloakId,
            @RequestBody UserSyncRequestDTO updateDto) {
        return ResponseEntity.ok(userService.updateProfile(keycloakId, updateDto));
    }

    @DeleteMapping("/delete/{keycloakId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String keycloakId) {
        userService.deleteProfile(keycloakId);
        return ResponseEntity.noContent().build();
    }

}