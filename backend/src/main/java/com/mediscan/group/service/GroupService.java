package com.mediscan.group.service;

import com.mediscan.dto.v1.group.GroupResponseDTO;
import com.mediscan.group.entity.Group;
import com.mediscan.group.repository.GroupRepository;
import com.mediscan.user.entity.User;
import com.mediscan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    public GroupResponseDTO createGroup(String name) {
        User currentUser = getCurrentUser();

        if (groupRepository.existsByNameAndOwnerId(name, currentUser.getId())) {
            throw new com.mediscan.common.exception.ResourceConflictException(
                    "You already have a group with this name");
        }

        Group group = Group.builder()
                .name(name)
                .owner(currentUser)
                .build();

        return toResponse(groupRepository.save(group));
    }

    public List<GroupResponseDTO> getMyGroups() {
        return groupRepository.findByOwnerId(getCurrentUser().getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private GroupResponseDTO toResponse(Group group) {
        return GroupResponseDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(null) // Not yet in entity
                .ownerEmail(group.getOwner().getEmail())
                .memberCount(1) // MVP: just owner
                .createdAt(group.getCreatedAt())
                .build();
    }
}
