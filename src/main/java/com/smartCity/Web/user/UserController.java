package com.smartCity.Web.user;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserService service;


    @PostMapping
    public User create(@RequestBody User user) {
        return service.register(user);
    }


    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    }


    @GetMapping("/{id}")
    public Optional<User> getById(@PathVariable Long id) {
        return service.getById(id);
    }


    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return service.update(id, user);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "User deleted successfully";
    }
}
