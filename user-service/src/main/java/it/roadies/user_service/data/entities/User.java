package it.roadies.user_service.data.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name= "USER_PROFILE")
@Data

public class User {

    @Id
    private String keycloakId;

    @Column(length = 255,unique = true,nullable = false)
    private String email;

    @Column(length = 255,unique = true, nullable = false)
    private String username;

    @Column(length = 255,nullable = false)
    private String firstName;

    @Column(length = 255,nullable = false)
    private String lastName;

    @Column(length = 255)
    private String avatarUrl;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "requesterId")
    private List<Friendship> requesterFriendships; //Amicizie in arrivo

    @OneToMany(mappedBy = "receiverId")
    private List<Friendship> receiverFriendships; //Amicizie in uscita

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Gamification gamification;

    @OneToMany(mappedBy = "user_id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDocument> documents;

}
