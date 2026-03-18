package com.smartCity.Web.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MyController {
	@RequestMapping("/index")
	public String welcomeUser(Model model) {
		String message = "hi sirs and madams";
		model.addAttribute("message", message);
		return "index";
	}

	@GetMapping("/register")
	public String registerPage() {
		return "register";
	}
	@RequestMapping("/dashboard")
	public String dashBoard() {
		return "dashboard";
	}
}
