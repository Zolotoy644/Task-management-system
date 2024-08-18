package com.zolotarev.tms.dto;

import com.zolotarev.tms.entities.Task;
import lombok.Data;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private String author;
    private String performer;
    private String comments;
}
