package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	/**
	 * This method is called before each method calling.
	 * This is to set common data.
	 * 
	 * @param model
	 * @param principal
	 */
	@ModelAttribute
	private void addCommonData(Model model,Principal principal ) {
		String userName=principal.getName();
		User user=userRepository.getUserByUserName(userName);
		model.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal ) {
		model.addAttribute("title","User Dashboard - Smart Contact Manager");
		return "normal/user_dashboard";
	}
	
	/**
	 * Endpoint to open JSP to enter details for saving a contact.
	 * 
	 * @param model
	 * @param principal
	 * @return JSP Page name for user to enter details.
	 */
	@RequestMapping("/addContact")
	public String addContact(Model model,Principal principal ) {
		model.addAttribute("title","Add Contact - Smart Contact Manager");
		model.addAttribute("contact",new Contact());
		return "normal/addcontact";
	}
	
	/**
	 * Endpoint to Save one Contact
	 * 
	 * @param contact
	 * @param image
	 * @param principal
	 * @param session
	 * @return JSP Page to save another contact.
	 */
	@PostMapping("/saveContact")
	public String saveContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile image,Principal principal,HttpSession session) {
		try {
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			contact.setUser(user);//This is required because of bi-directional mapping
			//processing and uploading image
			if(image.isEmpty()) {
				contact.setImage("contact.png");
			}else {
				contact.setImage(image.getOriginalFilename());
				File file=new ClassPathResource("/static/img").getFile();
				Path path=Paths.get(file.getAbsolutePath()+File.separator+image.getOriginalFilename());
				Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			
			
			user.getContacts().add(contact);
			userRepository.save(user);
			session.setAttribute("message", new Message("Contact Added Successfully !!", "success"));
		} catch (Exception e) {
			session.setAttribute("message", new Message("There is some error !! Try Again", "danger"));
			e.printStackTrace();
		}
		return "normal/addcontact";
	}
	
	/**
	 * Endpoint to List all Contacts
	 * 
	 * @param page
	 * @param model
	 * @param principal
	 * @return JSP Page name "normal/listcontacts"
	 */
	@GetMapping("/listcontacts/{page}")
	public String listAllContacts(@PathVariable("page") int page,Model model,Principal principal) {
		model.addAttribute("title","View Contacts - Smart Contact Manager");
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		Pageable pageable=PageRequest.of(page, 5);
		Page<Contact> contacts= contactRepository.findContactsByUser(user.getUserId(),pageable);
		//List<Contact> contacts=user.getContacts();
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/listcontacts";
	}
	
	/**
	 * Endpoint to View one Contact Details
	 * 
	 * @param contactId
	 * @param model
	 * @param principal
	 * @return JSP page name "normal/viewcontact"
	 */
	@GetMapping("/{contactId}/viewcontact")
	public String viewContact(@PathVariable("contactId") int contactId,Model model,Principal principal) {
		Optional<Contact> contactOptional=contactRepository.findById(contactId);
		if(contactOptional.isPresent()) {
			Contact contact=contactOptional.get();
			
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			
			if(user.getUserId()==contact.getUser().getUserId()) {
				model.addAttribute("contact",contact);
				model.addAttribute("title",contact.getName());
			}
		}
		return "normal/viewcontact";
	}
	
	/**
	 * Endpoint to delete a contact
	 * 
	 * @param contactId
	 * @param model
	 * @param principal
	 * @param session
	 * @return redirecting to list all contacts endpoint
	 */
	@GetMapping("/{contactId}/delete")
	public String deleteContact(@PathVariable("contactId") int contactId,Model model,Principal principal,HttpSession session) {
		Optional<Contact> contactOptional=contactRepository.findById(contactId);
		if(contactOptional.isPresent()) {
			Contact contact=contactOptional.get();
			
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			
			if(user.getUserId()==contact.getUser().getUserId()) {
				//contact.setUser(user); //This is to delink contact from user. Sometime we get error while deleting
				contactRepository.delete(contact);
				session.setAttribute("message", new Message("Contact Deleted Successfully", "success"));
			}
		}
		return "redirect:/user/listcontacts/0";
	}
	
	
	/**
	 * Endpoint to Open Update Form
	 * @param contactId
	 * @param model
	 * @return jsp page name "normal/updateContact"
	 */
	@PostMapping("/openUpdateForm/{contactId}")
	public String openUpdateContactPage(@PathVariable("contactId") int contactId,Model model) {
		model.addAttribute("title","View Contacts - Smart Contact Manager");
		Optional<Contact> contactOptional=contactRepository.findById(contactId);
		if(contactOptional.isPresent()) {
			model.addAttribute("contact",contactOptional.get());
		}
		return "normal/updatecontact";
	}
	
	@PostMapping("/updatecontact")
	public String updateContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile image,
								Principal principal,HttpSession session) {
		
		try {
			Contact oldContact=contactRepository.findById(contact.getContactId()).get();
			
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			contact.setUser(user);//This is required because of bi-directional mapping
			//processing and uploading image
			if(image.isEmpty()) {
				contact.setImage(oldContact.getImage());
			}else {
				contact.setImage(image.getOriginalFilename());
				File file=new ClassPathResource("/static/img").getFile();
				Path path=Paths.get(file.getAbsolutePath()+File.separator+image.getOriginalFilename());
				Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				if(oldContact.getImage()!="contact.png") {
				File deleteFile=new ClassPathResource("/static/img").getFile();
				File file1=new File(deleteFile,oldContact.getImage());
				file1.delete();
				}
			}
			contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact Updated Successfully !!", "success"));
		} catch (Exception e) {
			session.setAttribute("message", new Message("There is some error !! Try Again", "danger"));
			e.printStackTrace();
		}
		
		return "redirect:/user/listcontacts/0";
	}
	
	@GetMapping("/profile")
	public String showProfile(Model model) {
		model.addAttribute("title","Profile - Smart Contact Manager");
		return "normal/profile";
	}
	
	@GetMapping("/settings")
	public String openSettings(Model model) {
		model.addAttribute("title","Settings - Smart Contact Manager");
		return "normal/settings";
	}
	
	@PostMapping("/changepassword")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		User user = userRepository.getUserByUserName(principal.getName());
		
		if(passwordEncoder.matches(oldPassword, user.getPassword())) {
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);
			session.setAttribute("message", new Message("Password Successfully Changed !!", "success"));
		}else {
			session.setAttribute("message", new Message("Please Enter Correct Old Password", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}

}
