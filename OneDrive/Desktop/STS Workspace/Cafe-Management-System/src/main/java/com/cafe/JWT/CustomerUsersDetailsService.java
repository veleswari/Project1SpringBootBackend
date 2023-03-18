package com.cafe.JWT;

import java.util.ArrayList;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cafe.dao.UserDao;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerUsersDetailsService implements UserDetailsService{

	@Autowired
	UserDao userDao;
	
	private com.cafe.model.User userDetails;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("Inside loadUserByUsername {}",username);
		userDetails = userDao.findByEmailId(username);
		if(!Objects.isNull(userDetails)) {
			return new User(userDetails.getEmail(),userDetails.getPassword(),new ArrayList<>());
		}else {
			throw new UsernameNotFoundException("user not found");
		}
	}
	
	public com.cafe.model.User getUserDetails(){
		return userDetails;
	}
}
