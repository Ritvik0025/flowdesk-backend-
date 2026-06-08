package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:3001")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/{projectId}")
    public ResponseEntity<Task> createTask(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> body) {
        Task task = taskService.createTask(
                projectId,
                body.get("title"),
                body.get("description"),
                body.get("priority"),
                body.get("assigneeEmail")
        );
        return ResponseEntity.ok(task);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<List<Task>> getTasks(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<Task> updateStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body) {
        Task task = taskService.updateTaskStatus(taskId, body.get("status"));
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok().build();
    }
}