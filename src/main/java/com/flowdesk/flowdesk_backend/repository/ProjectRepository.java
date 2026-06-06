package com.flowdesk.flowdesk_backend.repository;

import com.flowdesk.flowdesk_backend.model.Project;
import com.flowdesk.flowdesk_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwner(User owner);
}