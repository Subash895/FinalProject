package com.smartCity.Web.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.user.UserDtos;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService service;
    private final ApiDtoMapper apiDtoMapper;

    public UserController(UserService service, ApiDtoMapper apiDtoMapper) {
        this.service = service;
        this.apiDtoMapper = apiDtoMapper;
    }


    @PostMapping
    public UserDtos.UserResponse create(@RequestBody UserDtos.UserRequest user) {
        return apiDtoMapper.toUserResponse(service.register(apiDtoMapper.toUser(user)));
    }


    @GetMapping
    public List<UserDtos.UserResponse> getAll() {
        return service.getAll().stream().map(apiDtoMapper::toUserResponse).collect(Collectors.toList());
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserDtos.UserResponse> getById(@PathVariable Long id) {
        return service.getById(id).map(apiDtoMapper::toUserResponse).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public UserDtos.UserResponse update(@PathVariable Long id, @RequestBody UserDtos.UserRequest user) {
        return apiDtoMapper.toUserResponse(service.update(id, apiDtoMapper.toUser(user)));
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "User deleted successfully";
    }
}
