package it.roadies.travel_service.data.dao;

import it.roadies.travel_service.data.entity.UserFriendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserFriendshipRepository extends JpaRepository<UserFriendship, UUID> {

    boolean existsByUserIdAndFriendId(String userId, String friendId);

    void deleteByUserIdAndFriendId(String userId, String friendId);
}