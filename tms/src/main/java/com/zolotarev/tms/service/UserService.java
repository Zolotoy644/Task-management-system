package com.zolotarev.tms.service;

import com.zolotarev.tms.dto.AuthRequest;
import com.zolotarev.tms.dto.AuthResponse;
import com.zolotarev.tms.dto.UserRequest;
import com.zolotarev.tms.dto.UserResponse;
import com.zolotarev.tms.entities.Comment;
import com.zolotarev.tms.entities.Task;
import com.zolotarev.tms.entities.User;
import com.zolotarev.tms.repository.TaskRepository;
import com.zolotarev.tms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder encoder;
    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final TaskRepository taskRepository;
    @Autowired
    private final AuthenticationManager authenticationManager;

    public Long register(UserRequest userRequest){

        User user = this.mapDtoToUser(userRequest);

        Optional<User> userFromDb = userRepository.findByEmail(userRequest.getEmail());

        if (userFromDb.isPresent()) {
            throw new NoSuchElementException("Email already registered: " + userRequest.getEmail());
        }

        User savedUser = userRepository.saveAndFlush(user);
        return savedUser.getId();
    }

    public AuthResponse login(AuthRequest authRequest){

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                ));



        User user = (User) auth.getPrincipal();
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(user.getId(), token);
    }

    public List<UserResponse> getAll() {
        List<User> userList = userRepository.findAll();
        return userList.stream().map(this::toResponse).toList();
    }

    // we need 3 calls here to avoid MultipleBagFetchException and Cartesian product
    @Cacheable(value = "user_resp", key = "#id")
    @Transactional(readOnly = true)
    public UserResponse getById(Long id){
        // all 3 users merged in one persistence context
        User user = userRepository.findByIdWithTasks(id).orElseThrow(
                () -> new NoSuchElementException("There is no User with id: " + id)
        );
        User user1 = userRepository.findOneById(id).orElseThrow();
        User user2 = userRepository.findOneByIdWithTasks(id).orElseThrow();
        return this.toResponse(user2);
    }

    public UserResponse toResponse(User user){
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setEmail(user.getEmail());
        resp.setRole(user.getRole());
        if (Hibernate.isInitialized( user.getAuthorTasks() )) {
            List<Task> tasks = user.getAuthorTasks();
            List<String> taskList = new ArrayList<>();
            for (Task t : tasks) {
                taskList.add("id: " + t.getId() + ", title: " + t.getTitle());
            }
            resp.setAsAuthor(taskList);
        } else {
            resp.setAsAuthor(List.of("undefined: loaded lazily"));
        }
        if (Hibernate.isInitialized( user.getPerformerTasks() )) {
            List<Task> tasks = user.getPerformerTasks();
            List<String> taskList = new ArrayList<>();
            for (Task t : tasks) {
                taskList.add("id: " + t.getId() + ", title: " + t.getTitle());
            }
            resp.setAsExecutor(taskList);
        } else {
            resp.setAsExecutor(List.of("undefined: loaded lazily"));
        }
        if (Hibernate.isInitialized( user.getComments() )) {
            List<Comment> comments = user.getComments();
            List<String> commentList = new ArrayList<>();
            for (Comment c : comments) {
                commentList.add("id: " + c.getId() + ", to task: " + c.getTaskId());
            }
            resp.setComments(commentList);
        } else {
            resp.setComments(List.of("undefined: loaded lazily"));
        }


        return resp;
    }

    public User mapDtoToUser(UserRequest userRequest){
        return User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .password(encoder.encode(userRequest.getPassword()))
                .role(userRequest.getRole())  // or set constant role like "USER"
                .build();
    }

    // Only ADMIN  user can do this
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#result.email"),
            @CacheEvict(value = "user_resp", key = "#userId"),
            @CacheEvict(value = "tasks", allEntries = true)
    })
    public User deleteUser(Long userId) {

        User user = userRepository.findByIdWithTasks(userId).orElseThrow(
                () -> new NoSuchElementException("There is no User with id: " + userId));

        // There is no orphan removal here!!!
        List<Long> performerTaskList = user.getPerformerTasks()
                .stream()
                .map(Task::getId)
                .toList();

        // clear executor field in affected tasks
        taskRepository.clearPerformers(performerTaskList);

        // update User DB
        userRepository.delete(user);

        return user;    // for caching purpose only
    }

    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "user_resp", key = "#result.id")
    })
    public UserResponse updateUser(UserRequest newUser, Authentication auth) {
        User current = (User) auth.getPrincipal();
        User fromDb = userRepository.findById( current.getId()).orElseThrow();
        fromDb.setFirstName(newUser.getFirstName());
        fromDb.setLastName(newUser.getLastName());
        fromDb.setEmail(newUser.getEmail());
        fromDb.setPassword( encoder.encode( newUser.getPassword() ));
        fromDb.setRole(newUser.getRole());
        User saved = userRepository.save(fromDb);
        return this.toResponse(saved);
    }
}
