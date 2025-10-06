package com.smart.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepositry;
import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepositry userRepositry;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model m, Principal principle) {
		String usernme = principle.getName();
		
		User user = userRepositry.getUserByUserName(usernme);
		System.out.println(user);
		
		m.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model m, Principal principle) {
		
		
		return "normal/user_dashboard";
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m) {
		m.addAttribute("title" , "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, Principal principal, HttpSession session) {

		try {
			
		    contact.setImage("default.png");

		    String username = principal.getName();
		    User user = userRepositry.getUserByUserName(username);

		    // Set owning side
		    contact.setUser(user);

		    // Update inverse side (optional but good practice)
		    user.getContacts().add(contact);

		    // Save user (cascade will save contact as well)
		    this.userRepositry.save(user);

		    System.out.println("data added: " + user);
		    session.setAttribute("message", new Message("Successfully Contact Details Saved!", "alert-success"));

		    return "normal/add_contact_form";
			
		    
	
		}catch(Exception e) {
			
			   e.printStackTrace();
		        session.setAttribute("message", new Message("Something went wrong: " + e.getMessage(), "alert-danger"));
		       
		        return "normal/add_contact_form";
			
			
		}
		

	}
	
	@GetMapping("/view-contact/{page}")
	public String viewContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		
		m.addAttribute("title", "view Contact Page");
		
		String userName = principal.getName();
		System.out.println("Logged-in user: " + userName);

		User user =this.userRepositry.getUserByUserName(userName);
		
		 Pageable pageable =PageRequest.of(page, 5);
		
		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		
		System.out.println("sachin"+contacts);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		
		
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		
		return "normal/view_contacts";
		
	}
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model m, Principal principal) {
	    
		 Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		  Contact contact= contactOptional.get();
		  
		  
		  String userName=principal.getName();
		  User user =this.userRepositry.getUserByUserName(userName);
		  
		  
		  if(user.getId() == contact.getUser().getId()) {
			  m.addAttribute("contact", contact);
			  m.addAttribute("title", contact.getName());
		  }
			 
		  
		  
		
		 System.out.println("cid"+cId);
		return "normal/contact_detail";
	}

	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Principal principal, HttpSession session) {

	    Optional<Contact> contactOptional = this.contactRepository.findById(cid);

	    if (!contactOptional.isPresent()) {
	        session.setAttribute("message", new Message("Contact not found!", "alert-danger"));
	        return "redirect:/user/view-contact/0";
	    }

	    Contact contact = contactOptional.get();

	    // ✅ Use ID for authorization check
	    User loggedInUser = this.userRepositry.getUserByUserName(principal.getName());
	    if (contact.getUser().getId() != loggedInUser.getId()) {
	        session.setAttribute("message", new Message("You are not authorized to delete this contact!", "alert-danger"));
	        return "redirect:/user/view-contact/0";
	    }


	    // Delete contact explicitly
	    this.contactRepository.delete(contact);

	    session.setAttribute("message", new Message("Contact Deleted Successfully", "alert-success"));
	    return "redirect:/user/view-contact/0";
	}

	
	// Open update form
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cId,Model m) {
		
		m.addAttribute("title","Update Contact");
		Contact contact= this.contactRepository.findById(cId).get();
		
		m.addAttribute("contact",contact);
		return "normal/update_contact_form";
	}
	
	@PostMapping("/process-update")
	public String UpdateFormData(@ModelAttribute Contact contact , Model m , Principal principle, HttpSession session) {
		
		User user = this.userRepositry.getUserByUserName(principle.getName());
		contact.setUser(user);
		
		this.contactRepository.save(contact);

		 return "redirect:/user/view-contact/0";
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title","Profile Page");
		return "normal/profile";
		
	}


}
