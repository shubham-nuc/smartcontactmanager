package com.smart.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

public class CustomUserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("Before User DAO Call username="+username);
		User user = userRepository.getUserByUserName(username);
		System.out.println("After User DAO Call");
		//System.out.println(user);
		if(user==null) {
			System.out.println("User not Exist !!");
			throw new UsernameNotFoundException("User not Exist !!");
		}
		System.out.println("After User Null Check");
		CustomUserDetails customUserDetails=new CustomUserDetails(user);
		return customUserDetails;
	}

}
