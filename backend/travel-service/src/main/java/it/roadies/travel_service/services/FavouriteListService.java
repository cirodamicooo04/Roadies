package it.roadies.travel_service.services;

import it.roadies.travel_service.data.entity.FavouriteList;
import it.roadies.travel_service.data.entity.enumerations.Visibility;

import java.util.List;
import java.util.UUID;

public interface FavouriteListService {

    FavouriteList createList(String name, Visibility visibility, String ownerId);
    FavouriteList getListWithPermissions(UUID listId, String requesterId);
    List<FavouriteList> getMyLists(String ownerId); // Ritorna le liste dell'utente
    void deleteList(UUID listId, String ownerId);
    List<FavouriteList> getUserVisibleLists(String targetUserId, String requesterId);
    FavouriteList updateList(UUID listId, String name, Visibility visibility, String ownerId);



    void addFriendToList(UUID listId, String friendId, String ownerId);
    void removeFriendFromList(UUID listId, String friendId, String ownerId);

    void addTravelToList(UUID listId, UUID travelId, String ownerId);
    void removeTravelFromList(UUID listId, UUID travelId, String ownerId);

    void addActivityToList(UUID listId, UUID activityId, String ownerId);
    void removeActivityFromList(UUID listId, UUID activityId, String ownerId);
}