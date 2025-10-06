package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.UserRepositry;
import com.smart.entity.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepositry userRepositry;
	
@RequestMapping("/")	
public String home(Model m) {
	m.addAttribute("title","Home - Smart Contect Manager");
	return "home";
}

@RequestMapping("/about")	
public String about(Model m) {
	m.addAttribute("title","About - Smart Contect Manager");
	return "about";
}

@RequestMapping("/signup")	
public String signup(Model m) {
	m.addAttribute("title","Register - Smart Contect Manager");
	m.addAttribute("user",new User());
	return "signup";
}

@PostMapping("/do_register")
public String registerUser(
        @Valid @ModelAttribute("user") User user,
        BindingResult result,
        Model m,
        HttpSession session,
        @RequestParam(value = "aggrement", defaultValue = "false") boolean aggrement) {

    // Check if terms & conditions are accepted
    if (!aggrement) {
        session.setAttribute("message", new Message("You must accept terms and conditions", "alert-danger"));
        m.addAttribute("user", user);
        return "signup";
    }

    // Validation errors
    if (result.hasErrors()) {
        m.addAttribute("user", user);
        return "signup";
    }

    try {
        // Set default values
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setImageUrl("default.png");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepositry.save(user);

        // Success message
        session.setAttribute("message", new Message("Successfully Registered!", "alert-success"));
        m.addAttribute("user", new User()); // reset form
        return "signup";

    } catch (Exception e) {
        e.printStackTrace();
        session.setAttribute("message", new Message("Something went wrong: " + e.getMessage(), "alert-danger"));
        m.addAttribute("user", user);
        return "signup";
    }
}

@RequestMapping("/signin")
public String customLogin(Model m) {
	m.addAttribute("title", "Login Page");
	return "login";
}

@RequestMapping("/loginfail")
public String customLoginFail(Model m) {
	m.addAttribute("title", "Login Page Page");
	return "loginfail";
}






}
