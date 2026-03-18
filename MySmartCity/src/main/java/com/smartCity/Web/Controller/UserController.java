package com.smartCity.Web.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.Model.User;
import com.smartCity.Web.Service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService service;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            Model model) {

        boolean valid = service.validataUser(email, password);

        if (valid) {
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Invalid credentials");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @PostMapping("/user/save")
    public String saveUser(@ModelAttribute User user) {
        service.saveUser(user);
        return "redirect:/";
    }
}