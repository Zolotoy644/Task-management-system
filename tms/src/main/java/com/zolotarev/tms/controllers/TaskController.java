package com.zolotarev.tms.controllers;

import com.zolotarev.tms.dto.TaskRequest;
import com.zolotarev.tms.dto.TaskResponse;
import com.zolotarev.tms.entities.Task;
import com.zolotarev.tms.exception.NoPermissionException;
import com.zolotarev.tms.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
@SecurityRequirement(name = "JWT Bearer")
@Tag(name="Контроллер задач", description="Предназначен для управления пользовательскими задачамми")
public class TaskController {
    @Autowired
    private final TaskService taskService;

    @PostMapping("/create")
    @Operation(
            summary = "Создание задачи",
            description = "Позволяет создать задачу"
    )
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest taskRequest,
                                        Authentication auth) {
        Task task = taskService.create(taskRequest, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body("Task created with id: " + task.getId());
    }

    @GetMapping("/all")
    @Operation(
            summary = "Поиск всех задач"
    )
    public ResponseEntity<?> findAll(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "5") int limit) {
        List<TaskResponse> tasks = taskService.findAll(PageRequest.of(page, limit));
        return ResponseEntity.status(HttpStatus.FOUND).body(tasks);
    }

    @GetMapping("/author/{id}")
    @Operation(
            summary = "Поиск всех задач по автору"
    )
    public ResponseEntity<?> findByAuthorId(@PathVariable Long id,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "5") int limit) {
        List<TaskResponse> tasks = taskService.findByAuthorId(id, PageRequest.of(page, limit));
        return ResponseEntity.status(HttpStatus.FOUND).body(tasks);
    }

    @GetMapping("/performer/{id}")
    @Operation(
            summary = "Поиск задач по исполнителю"
    )
    public ResponseEntity<?> findByPerformerId(@PathVariable Long id,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "5") int limit) {
        List<TaskResponse> tasks = taskService.findByPerformerId(id, PageRequest.of(page, limit));
        return ResponseEntity.status(HttpStatus.FOUND).body(tasks);
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Поиск задач по статусу"
    )
    public ResponseEntity<?> findByStatus(@PathVariable Task.TaskStatus status,
                                          @PageableDefault(page = 0, size = 5) Pageable pageable) {
        List<TaskResponse> tasks = taskService.findByStatus(status, pageable);
        return ResponseEntity.status(HttpStatus.FOUND).body(tasks);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Поиск конкретной задачи по id"
    )
    public ResponseEntity<?> findTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.findTaskById(id);
        return ResponseEntity.status(HttpStatus.FOUND).body(task);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Удаление задачи",
            description = "Позволяет удалить задачу"
    )
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication auth) throws NoPermissionException {
        Task task = taskService.delete(id, auth);
        return ResponseEntity.status(HttpStatus.OK).body("Task deleted with id: " + task.getId());
    }

    @PutMapping("/update/{id}")
    @Operation(
            summary = "Изменение задачи",
            description = "Позволяет изменить задачу"
    )
    public ResponseEntity<?> updateTask(@RequestParam Long id,
                                        @RequestBody TaskRequest newTask,
                                        Authentication auth) throws NoPermissionException {
        Long taskId = taskService.update(id, newTask, auth);
        return ResponseEntity.status(HttpStatus.OK).body(taskId);
    }
}
