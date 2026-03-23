package com.smartCity.Web.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.Model.User;
import com.smartCity.Web.Service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserService service;

    // ================= CREATE USER =================
    // 🔥 Uses register logic (IMPORTANT)
    @PostMapping
    public User create(@RequestBody User user) {
        return service.register(user);
    }

    // ================= GET ALL USERS =================
    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    }

    // ================= GET USER BY ID =================
    @GetMapping("/{id}")
    public Optional<User> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // ================= UPDATE USER =================
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return service.update(id, user);
    }

    // ================= DELETE USER =================
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "User deleted successfully";
    }
}