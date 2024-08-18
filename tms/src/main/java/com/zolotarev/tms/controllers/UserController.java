package com.zolotarev.tms.controllers;

import com.zolotarev.tms.dto.AuthRequest;
import com.zolotarev.tms.dto.AuthResponse;
import com.zolotarev.tms.dto.UserRequest;
import com.zolotarev.tms.dto.UserResponse;
import com.zolotarev.tms.entities.User;
import com.zolotarev.tms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name="Контроллер аутентификации", description="позволяет производить регистрацию и аутентификацию пользователей")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Позволяет создать нового пользователя"
    )
    ResponseEntity<?> register(@Valid @RequestBody UserRequest userRequest) {
        Long id = userService.register(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created with id: " + id);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Авторизация пользователя",
            description = "Метод генерирует аутентификационный JWT токен"
    )
    ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        AuthResponse response = userService.login(authRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "JWT Bearer")
    @Operation(
            summary = "Поиск пользователя по id"
    )
    ResponseEntity<?> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getById(id);
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @DeleteMapping("/delete/{id}")
    @SecurityRequirement(name = "JWT Bearer")
    @Operation(
            summary = "Удаление пользователя по id"
    )
    ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted with id: " + user.getId());
    }

    @GetMapping
    @SecurityRequirement(name = "JWT Bearer")
    @Operation(
            summary = "Получение списка пользователей5"
    )
    ResponseEntity<?> getAllUsers() {
        List<UserResponse> response = userService.getAll();
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @PutMapping("/update")
    @SecurityRequirement(name = "JWT Bearer")
    ResponseEntity<?> updateUser(@RequestBody UserRequest request, Authentication auth) {
        UserResponse response = userService.updateUser(request, auth);
        return ResponseEntity.status(HttpStatus.OK).body("User updated successfully: " + response.getId());
    }

}
