package com.zolotarev.tms.service;

import com.zolotarev.tms.dto.TaskRequest;
import com.zolotarev.tms.dto.TaskResponse;
import com.zolotarev.tms.entities.Comment;
import com.zolotarev.tms.entities.Task;
import com.zolotarev.tms.entities.User;
import com.zolotarev.tms.exception.NoPermissionException;
import com.zolotarev.tms.repository.CommentRepository;
import com.zolotarev.tms.repository.TaskRepository;
import com.zolotarev.tms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;

    public Task create(TaskRequest task, Authentication auth) {
        Long userId = this.extractUserId(auth);
        User author = userRepository.getReferenceById(userId);
        User performer;
        Task newTask = this.toTask(task);
        Long performerId = task.getPerformerId();
        if(performerId != null){
            performer = userRepository.findById(performerId).orElseThrow(
                    () -> new NoSuchElementException("There is no User with Id: " + performerId));
            newTask.setPerformer(performer);
        }
        newTask.setAuthor(author);

        return taskRepository.save(newTask);
    }

    private Task toTask(TaskRequest taskRequest) {
        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setStatus(taskRequest.getStatus());
        task.setPriority(taskRequest.getPriority());

        return task;
    }

    private TaskResponse toResponse(Task task){
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());

        if (Hibernate.isInitialized(task.getAuthor())){
            User author = task.getAuthor();
            response.setAuthor("id: " + author.getId()
                    + ", name: " + author.getFirstName()
                    + ", surname: " + author.getLastName());
        } else {
            response.setAuthor("id: " + task.getAuthorId());
        }
        if (Hibernate.isInitialized(task.getPerformer())){
            User executor = task.getPerformer();
            if (executor != null) {
                response.setPerformer("id: " + executor.getId()
                        + ", name: " + executor.getFirstName()
                        + ", surname: " + executor.getLastName());
            } else {
                response.setPerformer("no executor assigned");
            }
        } else {
            response.setPerformer("id: " + task.getPerformerId());
        }
        if (Hibernate.isInitialized(task.getComments())) {
            response.setComments(task.getComments().size() + " comment(s)");
        } else {
            response.setComments("undefined");
        }
        return response;
    }

    private Long extractUserId(Authentication auth){
        User user = null;
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            user = (User) token.getPrincipal();
        }
        assert user != null;
        return user.getId();
    }

    public List<TaskResponse> findAll(Pageable pageable) {
        List<Task> tasks = taskRepository.findAll(pageable).getContent();
        return tasks.stream().map(this::toResponse).toList();
    }

    public List<TaskResponse> findByAuthorId(Long authorId, Pageable pageable) {
        List<Task> tasks = taskRepository.findByAuthorId(authorId, pageable);
        return tasks.stream().map(this::toResponse).toList();
    }

    public List<TaskResponse> findByPerformerId(Long performerId, Pageable pageable) {
        List<Task> tasks = taskRepository.findByPerformerId(performerId, pageable);
        return tasks.stream().map(this::toResponse).toList();
    }

    public List<TaskResponse> findByStatus(Task.TaskStatus status, Pageable pageable) {
        List<Task> tasks = taskRepository.findByStatus(status, pageable);
        return  tasks.stream().map(this::toResponse).toList();
    }

    public TaskResponse findTaskById(Long id) {
        Optional<Task> task = taskRepository.findTaskById(id);
        if (!task.isPresent()) {
            throw new NoSuchElementException("There is no task with Id: " + id);
        }
        return this.toResponse(task.get());
    }

    public Long update(Long id, TaskRequest newTask, Authentication auth) throws NoPermissionException {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("There is no task with id: " + id));
        Long authorId = task.getAuthorId();
        Long userId = this.extractUserId(auth);
        boolean isAuthor = userId.equals(authorId);
        if (!isAuthor) {
            throw new NoPermissionException("You have no permission to update this task: " + id);
        }
        task.setPerformerId(newTask.getPerformerId());
        task.setTitle(newTask.getTitle());
        task.setDescription(newTask.getDescription());
        task.setStatus(newTask.getStatus());
        task.setPriority(newTask.getPriority());
        return id;
    }

    public Task delete(Long id, Authentication auth) throws NoPermissionException {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("There is no task with id: " + id));
        Long authorId = task.getAuthorId();
        Long userId = this.extractUserId(auth);
        boolean isAuthor = userId.equals(authorId);
        if (!isAuthor) {
            throw new NoPermissionException("You have no permission to delete this task: " + id);
        }
        taskRepository.deleteById(id);
        return task;
    }
}
