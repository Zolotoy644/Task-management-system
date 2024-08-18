package com.zolotarev.tms.service;

import com.zolotarev.tms.dto.CommentRequest;
import com.zolotarev.tms.dto.CommentResponse;
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

@Service
@RequiredArgsConstructor
public class CommentService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final CommentRepository commentRepository;
    @Autowired
    private final TaskRepository taskRepository;

    public Comment create(CommentRequest comment, Authentication auth) {
        Long userId = this.extractUserId(auth);
        User author = userRepository.getReferenceById(userId);
        Task task = taskRepository.findTaskById(comment.getTaskId()).orElseThrow(
                () -> new NoSuchElementException("There is no Task with taskId: " + comment.getTaskId())
        );
        Comment newComment = new Comment();
        newComment.setAuthor(author);
        newComment.setTask(task);
        newComment.setText(comment.getText());
        newComment.setAuthorId(userId);
        return commentRepository.save(newComment);
    }

    private Long extractUserId(Authentication auth) {
        User user = null;
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            user = (User) token.getPrincipal();
        }
        assert user != null;
        return user.getId();
    }

    public List<CommentResponse> findAllByTaskId(Long taskId, Pageable pageable) {
        List<Comment> commentList = commentRepository.findAllByTaskId(taskId, pageable);
        return commentList.stream().map(this::toResponse).toList();
    }

    public List<CommentResponse> findAllByAuthorId(Long authorId, Pageable pageable) {
        List<Comment> commentList = commentRepository.findAllByAuthorId(authorId, pageable);
        return commentList.stream().map(this::toResponse).toList();
    }

    public List<CommentResponse> findAll(Pageable pageable) {
        List<Comment> commentList = commentRepository.findAll(pageable).getContent();
        return commentList.stream().map(this::toResponse).toList();
    }

    private CommentResponse toResponse(Comment comment){
        CommentResponse resp = new CommentResponse();
        resp.setId(comment.getId());
        resp.setContent(comment.getText());
        if (Hibernate.isInitialized(comment.getAuthor())){
            User author = comment.getAuthor();
            resp.setAuthor("id: " + author.getId()
                    + ", name: " + author.getFirstName()
                    + ", surname: " + author.getLastName());
        } else {
            resp.setAuthor("id: " + comment.getAuthorId());
        }
        if ( Hibernate.isInitialized( comment.getTask() )){
            Task task = comment.getTask();
            resp.setTask("id: " + task.getId()
                    + ", title: " + task.getTitle());
        } else {
            resp.setTask("id: " + comment.getTaskId());
        }

        return resp;
    }

    public Comment delete(Long id, Authentication auth) throws NoPermissionException {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("There is no comment with such id"));
        Long authorId = comment.getAuthorId();

        Long userId = this.extractUserId(auth);
        boolean isAuthor = userId.equals(authorId);
        if( !isAuthor ){
            throw new NoPermissionException("You have no permission to delete this comment: " + id);
        }
        commentRepository.deleteById(id);

        return comment;
    }

    public Long update(Long id, CommentRequest newComment, Authentication auth) throws NoPermissionException {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("There is no comment with such id"));
        Long authorId = comment.getAuthorId();
        Long userId = this.extractUserId(auth);
        boolean isAuthor = userId.equals(authorId);
        if(!isAuthor) {
            throw new NoPermissionException("You have no permission to update this comment: " + id);
        }
        comment.setText(newComment.getText());

        return commentRepository.save(comment).getId();
    }
}
