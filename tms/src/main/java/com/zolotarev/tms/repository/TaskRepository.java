package com.zolotarev.tms.repository;

import com.zolotarev.tms.entities.Task;
import com.zolotarev.tms.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findAll(Pageable pageable);
    List<Task> findByAuthorId(Long authorId, Pageable pageable);
    List<Task> findByPerformerId(Long performerId, Pageable pageable);
    List<Task> findByStatus(Task.TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.comments JOIN FETCH t.author LEFT JOIN FETCH t.performer WHERE t.id = ?1")
    Optional<Task> findTaskById(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.performerId = NULL WHERE t.id IN ?1")
    void clearPerformers(List<Long> idList);
}
