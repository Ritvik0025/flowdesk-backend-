package com.flowdesk.flowdesk_backend.repository;

import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
}