package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepositry;
import com.smart.entity.User;

public class UserDetailsServiceImpl1 implements UserDetailsService {

	@Autowired
	private UserRepositry userRepositry;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user =userRepositry.getUserByUserName(username);
		
		if(user == null) {
			throw new UsernameNotFoundException("Could not found user");
		}
		
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		
		return customUserDetails;
	}

}
