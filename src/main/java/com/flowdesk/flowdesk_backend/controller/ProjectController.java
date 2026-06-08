package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.model.Project;
import com.flowdesk.flowdesk_backend.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:3001")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestBody java.util.Map<String, String> body,
            Authentication authentication) {
        String email = authentication.getName();
        Project project = projectService.createProject(
                body.get("name"),
                body.get("description"),
                email
        );
        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<List<Project>> getProjects(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(projectService.getProjects(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }
}