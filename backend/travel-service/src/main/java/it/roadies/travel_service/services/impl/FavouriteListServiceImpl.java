package it.roadies.travel_service.services.impl;

import it.roadies.shared.i18n.MessageLang;
import it.roadies.travel_service.data.dao.*;
import it.roadies.travel_service.data.entity.*;
import it.roadies.travel_service.data.entity.enumerations.Visibility;
import it.roadies.travel_service.services.FavouriteListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavouriteListServiceImpl implements FavouriteListService {

    private final FavouriteListRepository listRepository;
    private final FavouriteListSharedRepository sharedRepository;
    private final UserFriendshipRepository friendshipRepository;
    private final TravelRepository travelRepository;
    private final FavouriteListItemRepository itemRepository;
    private final ActivityRepository activityRepository;
    private final MessageLang messageLang;

    @Override
    @Transactional
    public FavouriteList createList(String name, Visibility visibility, String ownerId) {
        FavouriteList newList = new FavouriteList();
        newList.setName(name);
        newList.setVisibility(visibility);
        newList.setOwnerId(ownerId);
        return listRepository.save(newList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavouriteList> getMyLists(String ownerId) {
        return listRepository.findAllByOwnerId(ownerId);
    }

    @Override
    @Transactional
    public void deleteList(UUID listId, String ownerId) {
        FavouriteList list = listRepository.findById(listId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("list.not.found")));

        if (!list.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("error.deleting.other.list"));
        }
        listRepository.delete(list);
    }


    //Controllo della lista con i vari casi
    //CASO 1: SE NON SEI IL PROPRIETARIO E LA LISTA E' PRIVATA TI BUTTO FUORI
    //CASO 2: SE NON SEI IL PROPRIETARIO E SIAMO NEL CASO DELLA SHARED_SPECIFIC, CONTROLLIAMO ALL'INTERNO DELLO SHARED REPO
    //SE E' PRESENTE L'ID DELL'UTENTE LOGGATO
    //CASO 3: SE NON SEI IL PROPRIETARIO E SIAMO NEL CASO DEL PUBLIC, CONTROLLIAMO SE ABBIAMO NEL NOSTRO "TACCUINO" SALVATA
    //L'AMICIZIA TRA L'UTENTE CORRENTE E L'UTENTE DI CUI STIAMO RICHIEDENDO LE LISTE

    @Override
    public FavouriteList getListWithPermissions(UUID listId, String requesterId) {

        FavouriteList list = listRepository.findById(listId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageLang.getMessage("list.not.found")));

        if (requesterId != null && requesterId.equals(list.getOwnerId())) {
            return list;
        }

        switch (list.getVisibility()) {

            case PRIVATE:
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("unauthorized.private"));

            case SHARED_SPECIFIC:
                if (requesterId == null || !sharedRepository.existsByListIdAndUserId(listId, requesterId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("unauthorized.list"));
                }
                break;

            case PUBLIC:
                if (requesterId == null || !friendshipRepository.existsByUserIdAndFriendId(list.getOwnerId(), requesterId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("unauthorized.require.friendship"));
                }
                break;

            default:
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messageLang.getMessage("error.unknown.visibility"));
        }

        return list;
    }

    @Override
    @Transactional
    public void addFriendToList(UUID listId, String friendId, String ownerId) {
        FavouriteList list = listRepository.findById(listId).orElseThrow();
        if (!list.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lista non tua");

        if (!sharedRepository.existsByListIdAndUserId(listId, friendId)) {
            FavouriteListShared shared = new FavouriteListShared();
            shared.setList(list);
            shared.setUserId(friendId);
            sharedRepository.save(shared);
        }
    }

    @Override
    @Transactional
    public void removeFriendFromList(UUID listId, String friendId, String ownerId) {
        FavouriteList list = listRepository.findById(listId).orElseThrow();
        if (!list.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lista non tua");

        sharedRepository.deleteByListIdAndUserId(listId, friendId);
    }

    @Override
    @Transactional
    public void addTravelToList(UUID listId, UUID travelId, String ownerId) {
        FavouriteList list = listRepository.findById(listId).orElseThrow();
        if (!list.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lista non tua");

        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaggio non trovato"));

        FavouriteListItem item = new FavouriteListItem();
        item.setList(list);
        item.setTravel(travel);
        item.setActivity(null);
        itemRepository.save(item);
    }

    @Override
    @Transactional
    public void addActivityToList(UUID listId, UUID activityId, String ownerId) {
        FavouriteList list = listRepository.findById(listId).orElseThrow();
        if (!list.getOwnerId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lista non tua");

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attività non trovata"));

        FavouriteListItem item = new FavouriteListItem();

        item.setList(list);
        item.setTravel(null);
        item.setActivity(activity);
        itemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeTravelFromList(UUID listId, UUID travelId, String ownerId) {
        FavouriteList list = listRepository.findById(listId).orElseThrow();
        if (!list.getOwnerId().equals(ownerId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageLang.getMessage("unauthorized.access"));

        itemRepository.deleteByListIdAndTravelId(listId, travelId);
    }

    @Override
    @Transactional
    public void removeActivityFromList(UUID listId, UUID activityId, String ownerId) {
        FavouriteList list = listRepository.findById(listId).orElseThrow();
        if (!list.getOwnerId().equals(ownerId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lista non tua");

        itemRepository.deleteByListIdAndActivityId(listId, activityId);
    }


}