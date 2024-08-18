package com.zolotarev.tms.dto;

import com.zolotarev.tms.entities.User;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private User.Role role;
    private List<String> asAuthor;
    private List<String> asExecutor;
    private List<String> comments;
}
