package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.enums.UserRole;
import com.smart.helper.Message;
import com.smart.service.IUserService;

@Controller
public class HomeController {
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@RequestMapping(value = "/registeruser",method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult bindingResult,
								@RequestParam(value = "agreement",defaultValue = "false") boolean agreement, 
								Model model,HttpSession session ) {
		System.out.println("Agreement" +agreement);
		try {
			System.out.println("User" + user);
			if (!agreement) {
					throw new Exception("You have not agreed terms and conditions.");
			}
			
			if(bindingResult.hasErrors()) {
				model.addAttribute("user", user);
				return "signup";
			}
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setRole(UserRole.USER.getRole());
			user.setEnabled(true);
			userService.saveUser(user);
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
		} catch (Exception e) {
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong !!"+e.getMessage(), "alert-danger"));
		}
		return "signup";
	}
	
	@RequestMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title", "Login - Smart Contact Manager");
		return "login";
	}

}
