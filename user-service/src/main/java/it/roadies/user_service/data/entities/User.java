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
    private String keycloak_id;

    @Column(length = 255,unique = true,nullable = false)
    private String email;

    @Column(length = 255,unique = true, nullable = false)
    private String username;

    @Column(length = 255,nullable = false)
    private String first_name;

    @Column(length = 255,nullable = false)
    private String last_name;

    @Column(length = 255)
    private String avatar_url;

    @Column(nullable = false)
    private LocalDate birth_date;

    @Column(nullable = false)
    private LocalDateTime created_at;

    @Column(nullable = false)
    private LocalDateTime last_login;

    @OneToMany(mappedBy = "requester_id")
    private List<Friendship> requester_friendships; //Amicizie in arrivo

    @OneToMany(mappedBy = "receiver_id")
    private List<Friendship> receiver_friendships; //Amicizie in uscita

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Gamification gamification;

}
