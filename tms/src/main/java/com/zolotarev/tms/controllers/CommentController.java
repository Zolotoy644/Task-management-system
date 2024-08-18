package com.zolotarev.tms.controllers;

import com.zolotarev.tms.dto.CommentRequest;
import com.zolotarev.tms.dto.CommentResponse;
import com.zolotarev.tms.entities.Comment;
import com.zolotarev.tms.exception.NoPermissionException;
import com.zolotarev.tms.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Data
@RequiredArgsConstructor
@RequestMapping("/comments")
@SecurityRequirement(name = "JWT Bearer")
@Tag(name="Контроллер комментариев", description="Предназначен для добавления и получения комментариев к задачам")
public class CommentController {
    @Autowired
    private final CommentService commentService;

    @PostMapping("/create")
    @Operation(
            summary = "Создание комментария к задаче",
            description = "Позволяет создать комментарий"
    )
    public ResponseEntity<?> createComment(@RequestBody CommentRequest commentRequest,
                                        Authentication auth) {
        Comment comment = commentService.create(commentRequest, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body("Comment created with id: " + comment.getId());
    }

    @GetMapping("/all")
    @Operation(
            summary = "Получение всех комментариев"
    )
    public ResponseEntity<?> findAll(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "5") int limit) {
        List<CommentResponse> comments = commentService.findAll(PageRequest.of(page, limit));
        return ResponseEntity.status(HttpStatus.FOUND).body(comments);
    }

    @GetMapping("/by-task/{taskId}")
    @Operation(
            summary = "Поиск комментария по id задачи"
    )
    public ResponseEntity<?> findByTaskId(@PathVariable Long taskId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "5") int limit) {
        List<CommentResponse> comments = commentService.findAllByTaskId(taskId, PageRequest.of(page, limit));
        return ResponseEntity.status(HttpStatus.FOUND).body(comments);
    }

    @GetMapping("/by-author/{authorId}")
    @Operation(
            summary = "Поиск комментария к задаче по автору"
    )
    public ResponseEntity<?> findByAuthorId(@PathVariable Long authorId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "5") int limit) {
        List<CommentResponse> comments = commentService.findAllByAuthorId(authorId, PageRequest.of(page, limit));
        return ResponseEntity.status(HttpStatus.FOUND).body(comments);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Удаление комментария к задаче",
            description = "Позволяет удалить комментарий"
    )
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) throws NoPermissionException {
        Comment comment = commentService.delete(id, auth);
        return ResponseEntity.status(HttpStatus.OK).body("Comment deleted with id: " + comment.getId());
    }

    @PutMapping("/update/{id}")
    @Operation(
            summary = "Изменение комментария к задаче",
            description = "Позволяет изменить комментарий"
    )
    public ResponseEntity<?> update(@RequestParam Long id,
                                    @RequestBody CommentRequest newComment,
                                    Authentication auth) throws NoPermissionException {
        Long commentId = commentService.update(id, newComment, auth);
        return ResponseEntity.status(HttpStatus.OK).body(commentId);
    }
}
