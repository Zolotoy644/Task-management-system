package com.zolotarev.tms.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zolotarev.tms.dto.AuthRequest;
import com.zolotarev.tms.dto.AuthResponse;
import com.zolotarev.tms.dto.UserRequest;
import com.zolotarev.tms.entities.User;
import com.zolotarev.tms.repository.UserRepository;
import com.zolotarev.tms.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:test-application.yml")
@Transactional
public class ControllersTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    void registerUser() throws Exception {
        UserRequest userRequest = new UserRequest("Ivan", "Ivanov", "iv@ya.ru", "111", User.Role.USER);

        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());
        Optional<User> user = userRepository.findByEmail(userRequest.getEmail());
        assertTrue(user.isPresent());
    }

    @Test
    void authUser() throws Exception {
        UserRequest userRequest = new UserRequest("Ivan", "Ivanov", "iv@ya.ru", "111", User.Role.USER);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());


        AuthRequest authRequest = new AuthRequest("iv@ya.ru", "111");
        String responseString = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse response = objectMapper.readValue(responseString, AuthResponse.class);

        assertEquals(authRequest.getEmail(), jwtService.extractUserEmail(response.getToken()));
    }

    @Test
    void getUserById() throws Exception {
        UserRequest userRequest = new UserRequest("Ivan", "Ivanov", "iv@ya.ru", "111", User.Role.USER);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        AuthRequest authRequest = new AuthRequest("iv@ya.ru", "111");
        String responseString = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        AuthResponse response = objectMapper.readValue(responseString, AuthResponse.class);


        mockMvc.perform(get("/users/{id}", response.getUserId())
                .header("Authorization", "Bearer " + response.getToken()))
        // Assert
        .andExpect(status().isFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
