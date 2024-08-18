package com.zolotarev.tms.repository;

import com.zolotarev.tms.entities.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByTaskId(Long taskId, Pageable pageable);
    List<Comment> findAllByAuthorId(Long authorId, Pageable pageable);
}
