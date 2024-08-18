package com.zolotarev.tms.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long taskId;
    private String text;
}
