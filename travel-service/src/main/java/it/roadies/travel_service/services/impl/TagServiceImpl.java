package it.roadies.travel_service.services.impl;

import it.roadies.travel_service.data.dao.TagRepository;
import it.roadies.travel_service.data.dto.response.TagResponse;
import it.roadies.travel_service.data.entity.Tag;
import it.roadies.travel_service.data.mapper.TagMapper;
import it.roadies.travel_service.services.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public List<TagResponse> getTags() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream().map(tagMapper::toResponse).toList();
    }

    @Transactional
    public TagResponse addTag(String name) {
        String normalizedName = name.toUpperCase().trim();

        Optional<Tag> existingTag = tagRepository.findByNameIgnoreCase(normalizedName);
        if (existingTag.isPresent()) return tagMapper.toResponse(existingTag.get());

        Tag tag = new Tag();
        tag.setName(normalizedName);
        Tag newTag = tagRepository.save(tag);

        //Aggiungo il nuovo tag a tutti i viaggi esistenti con score di default
        tagRepository.addDefaultTagToAllTravels(newTag.getId(), 2);

        return tagMapper.toResponse(tag);
    }
}
