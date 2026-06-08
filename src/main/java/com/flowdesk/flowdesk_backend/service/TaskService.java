package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.config.KafkaProducer;
import com.flowdesk.flowdesk_backend.model.Project;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.repository.ProjectRepository;
import com.flowdesk.flowdesk_backend.repository.TaskRepository;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducer;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       KafkaProducer kafkaProducer) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public Task createTask(Long projectId, String title,
                           String description, String priority, String assigneeEmail) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(Task.Priority.valueOf(priority.toUpperCase()));
        task.setProject(project);

        if (assigneeEmail != null) {
            userRepository.findByEmail(assigneeEmail)
                    .ifPresent(task::setAssignee);
        }

        Task saved = taskRepository.save(task);

        // Send Kafka event
        kafkaProducer.sendTaskEvent("TASK_CREATED", saved.getId(), projectId.toString());

        return saved;
    }

    public List<Task> getTasksByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return taskRepository.findByProject(project);
    }

    public Task updateTaskStatus(Long taskId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(Task.Status.valueOf(status.toUpperCase()));
        task.setUpdatedAt(LocalDateTime.now());
        Task updated = taskRepository.save(task);

        // Send Kafka event
        kafkaProducer.sendTaskEvent("TASK_UPDATED", taskId, task.getProject().getId().toString());

        return updated;
    }

    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Send Kafka event before deleting
        kafkaProducer.sendTaskEvent("TASK_DELETED", taskId, task.getProject().getId().toString());

        taskRepository.deleteById(taskId);
    }
}