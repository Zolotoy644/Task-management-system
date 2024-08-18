package com.zolotarev.tms.dto;

import lombok.Data;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private String author;
    private String task;
}
