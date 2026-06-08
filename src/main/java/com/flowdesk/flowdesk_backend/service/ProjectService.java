package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.model.Project;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.repository.ProjectRepository;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Project createProject(String name, String description, String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setOwner(owner);

        return projectRepository.save(project);
    }

    public List<Project> getProjects(String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return projectRepository.findByOwner(owner);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}