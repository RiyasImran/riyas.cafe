package com.riyas.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.riyas.cafe.JWT.CustomerUserDetailService;
import com.riyas.cafe.JWT.JwtFilter;
import com.riyas.cafe.JWT.JwtUtil;
import com.riyas.cafe.constants.CafeConstants;
import com.riyas.cafe.dao.UserDao;
import com.riyas.cafe.models.User;
import com.riyas.cafe.service.UserService;
import com.riyas.cafe.utils.CafeUtils;
import com.riyas.cafe.utils.EmailSender;
import com.riyas.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    CustomerUserDetailService customerUserDetailService;
    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    EmailSender emailSender;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> signup(Map<String, String> requestMap) {
        log.info("inside signup {}",requestMap);
        try {
            if(validateSignUp(requestMap)){
                User user = userDao.findByEmail(requestMap.get("email"));
                if (Objects.isNull(user)){
                    userDao.save(getUserfromMap(requestMap));
                    return CafeUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);
                }
                else{
                    return CafeUtils.getResponseEntity("Email ALready Exists",HttpStatus.BAD_REQUEST);
                }
            }
            else{
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        }
        catch(Exception e){ e.printStackTrace();}
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("inside login");
        try{
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"),requestMap.get("password"))
            );
            if(auth.isAuthenticated()){
                if(customerUserDetailService.getUserDetails().getStatus().equalsIgnoreCase("true")){
                    return new ResponseEntity<>("{\"token\":\"" +
                            jwtUtil.generateToken(customerUserDetailService.getUserDetails().getEmail(),
                                    customerUserDetailService.getUserDetails().getRole())
                            + "\"}", HttpStatus.OK);
                }else {
                    return new ResponseEntity<>("{\"token\":\"" + "Wait For Admin Approval!" + "\"}", HttpStatus.BAD_REQUEST);
                }
            }
        }catch(Exception e){ log.error("{}",e);}
        return new ResponseEntity<>("{\"token\":\"" + "Bad Credentials" + "\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers() {
        try{
            if (jwtFilter.isAdmin()){
                return new ResponseEntity<>(userDao.getAllUsers(), HttpStatus.OK);
            }else {
             return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return CafeUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                Integer userId = Integer.parseInt(requestMap.get("id"));
                Optional <User> user = userDao.findById(userId);
                if(!user.isEmpty()){
                    String status = requestMap.get("status").toString();
                    userDao.updateStaus(status, userId);
                    /** we're facing some issues with gmail smtp configuration,
                     * so temporarily we stopped the mail activities */
                    sendMailToAllAdmins(status, user.get().getEmail(), userDao.getAllAdminsEmail());
                    return new ResponseEntity<>("User Status updated successfully", HttpStatus.OK);
                }else {
                    return new ResponseEntity<>("User Id doesn't exist", HttpStatus.OK);
                }
            }else {
                return new ResponseEntity<>(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgetPassword(Map<String, String> requestMap) {
        try{
            User user = userDao.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())){
                emailSender.forgetPasswordMail(user.getEmail(), "Credentials by Cafe Management", user.getPassword());
                return CafeUtils.getResponseEntity("Check your mail for credentials", HttpStatus.OK);
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try{
            User user = userDao.findByEmail(jwtFilter.getCurrentUser());
            if (!user.equals(null)){
                if (user.getPassword().equals(requestMap.get("oldPassword"))){
                    user.setPassword(requestMap.get("newPassword"));
                    userDao.save(user);
                    return CafeUtils.getResponseEntity(" Password Updated Successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity(" Incorrect Old Password", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmins(String status, String user, List<String> allAdminsEmail) {
        String admin = jwtFilter.getCurrentUser();
        allAdminsEmail.remove(admin);
        if(status != null && status.equalsIgnoreCase("true")){
            String enableMsg = "USER:- "+user+"\n is approved by \nADMIN:- "+admin;
            emailSender.sendSimpleMessage(admin, "Account Approved", enableMsg, allAdminsEmail);
        }else {
            String disableMsg = "USER:- "+user+"\n is disabled by \nADMIN:- "+admin;
            emailSender.sendSimpleMessage(admin, "Account Disabled", disableMsg, allAdminsEmail);

        }
    }

    private boolean validateSignUp(Map<String, String> requestMap){
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("password");
    }

    private User getUserfromMap(Map<String, String> requestMap){
        User user = new User();

        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setRole("user");
        user.setStatus("false");

        return user;
    }
}
