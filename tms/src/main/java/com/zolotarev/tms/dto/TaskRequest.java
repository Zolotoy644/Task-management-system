package com.zolotarev.tms.dto;

import com.zolotarev.tms.entities.Task;
import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private Long performerId;
}
