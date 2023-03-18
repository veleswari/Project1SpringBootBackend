package com.cafe.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.cafe.JWT.CustomerUsersDetailsService;
import com.cafe.JWT.JwtFilter;
import com.cafe.JWT.JwtUtil;
import com.cafe.constents.CafeConstants;
import com.cafe.dao.UserDao;
import com.cafe.model.User;
import com.cafe.service.UserService;
import com.cafe.util.CafeUtils;
import com.cafe.util.EmailUtils;
import com.cafe.wrapper.UserWrapper;
import com.google.common.base.Strings;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService{

	@Autowired
	UserDao userDao; 
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	CustomerUsersDetailsService customerUsersDetailsService;
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	JwtFilter jwtFilter;
	
	@Autowired
	EmailUtils emailUtils;
	
	@Override
	public ResponseEntity<String> signUp(Map<String, String> requestMap) {
		log.info("Inside signup {}",requestMap);
		try {
			if(validateSignUpMap(requestMap)) {
				User user=userDao.findByEmailId(requestMap.get("email"));
				if(Objects.isNull(user)) { 
					userDao.save(getUserFromMap(requestMap));
					return CafeUtils.getResponseEntity("Successfully Registered", HttpStatus.OK); 
					
				}else {
					return CafeUtils.getResponseEntity("Email already exists", HttpStatus.BAD_REQUEST);
				}
				
			}else {
				return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	private boolean validateSignUpMap(Map<String, String> requestMap) {
		if(requestMap.containsKey("name") && requestMap.containsKey("contactNumber") &&
		requestMap.containsKey("email") && requestMap.containsKey("password")) {
			return true;
		}else {
		return false;
		}
	}
	
	private User getUserFromMap(Map<String, String> requestMap) {
		User user= new User();
		user.setName(requestMap.get("name"));
		user.setEmail(requestMap.get("email"));
		user.setContactNumber(requestMap.get("contactNumber"));
		user.setPassword(requestMap.get("password"));
		user.setStatus("false");
		user.setRole("user");
		return user;  
		
	}

	@Override
	public ResponseEntity<String> login(Map<String, String> requestMap) {
		log.info("Inside login");
		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));
			if(auth.isAuthenticated()) {
				if(customerUsersDetailsService.getUserDetails().getStatus().equalsIgnoreCase("true")) {
					return new ResponseEntity<String>( "{\"token\":\""+
				jwtUtil.generateToken(customerUsersDetailsService.getUserDetails().getEmail(),
						customerUsersDetailsService.getUserDetails().getRole()) + "\"}",
				HttpStatus.OK ); 
				} 
				else {
					return new ResponseEntity<String>("{\"message\":\""+"wait for admin approval."+"\"}",HttpStatus.BAD_REQUEST);
				}
			}			
		} catch (Exception e) {
			log.error("{}",e);
		}
		return new ResponseEntity<String>("{\"message\":\""+"Bad credentials."+"\"}",HttpStatus.BAD_REQUEST);
		
	}

	@Override
	public ResponseEntity<List<UserWrapper>> getAllUser() {
		try {
			if(jwtFilter.isAdmin()) {
				return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK); 
			}else {
				return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
			}
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> update(Map<String, String> requestMap) {
		try {
			if (jwtFilter.isAdmin()) {
				Optional<User> optional= userDao.findById(Integer.parseInt(requestMap.get("id")));
				if(!optional.isEmpty()) {
					userDao.updateStatus(requestMap.get("status"),Integer.parseInt(requestMap.get("id")));
					//sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(),userDao.getAllAdmin());
					return CafeUtils.getResponseEntity("User status update successfully", HttpStatus.OK);
				}else {
					return CafeUtils.getResponseEntity("User id doesn't exists", HttpStatus.OK); 
				}
			} else {
				return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED); 
			}
			
		} catch (Exception e) {
			
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> checkToken() {
		return CafeUtils.getResponseEntity("true",HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
		try {
			User userObj= userDao.findByEmailId(jwtFilter.getCurrentUser());
			if(!userObj.equals(null)) {
				if(userObj.getPassword().equals(requestMap.get("oldPassword"))) {
					userObj.setPassword(requestMap.get("newPassword"));
					userDao.save(userObj);
					return CafeUtils.getResponseEntity("Password update successfully", HttpStatus.OK);
				}
				return CafeUtils.getResponseEntity("Incorrect old password", HttpStatus.BAD_REQUEST); 
			}
			return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);  
	}

	@Override
	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
		try {
			User user=userDao.findByEmailId(requestMap.get("email"));
			if(!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
				emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", user.getPassword());
			return CafeUtils.getResponseEntity("Check your mail for Credentials", HttpStatus.OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);  
	}

//	private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
//		allAdmin.remove(jwtFilter.getCurrentUser());
//		if(status != null && status.equalsIgnoreCase("true")) {
//			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Aproved","USER:- "+user+"\n is approved by \n ADMIN:- "+jwtFilter.getCurrentUser(),allAdmin);
//		}else {
//			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled","USER:- "+user+"\n is disabled by \n ADMIN:- "+jwtFilter.getCurrentUser(),allAdmin);
//		}
//	}
}
