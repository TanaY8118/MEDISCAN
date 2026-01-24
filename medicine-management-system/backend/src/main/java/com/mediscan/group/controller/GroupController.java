package com.mediscan.group.controller;

import com.mediscan.dto.v1.group.GroupRequestDTO;
import com.mediscan.dto.v1.group.GroupResponseDTO;
import com.mediscan.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponseDTO> createGroup(@RequestBody @Valid GroupRequestDTO request) {
        return ResponseEntity.ok(groupService.createGroup(request.getName()));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponseDTO>> getMyGroups() {
        return ResponseEntity.ok(groupService.getMyGroups());
    }

}
