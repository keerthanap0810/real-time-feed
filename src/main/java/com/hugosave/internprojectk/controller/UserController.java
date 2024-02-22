package com.hugosave.internprojectk.controller;

import com.hugosave.intern.project.proto.*;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.service.UserService;
import com.hugosave.internprojectk.utilities.utils.Utils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public UserAuthResponseDTO login(@RequestBody UserOnboardDTO userOnboard) {
        return userService.userLogin(userOnboard);
    }

    @PostMapping("/signup")
    public UserAuthResponseDTO signup(@RequestBody UserOnboardDTO userOnboard) {
        return userService.userSignup(userOnboard);
    }

    @GetMapping("/balance")
    public UserAssetAndBalanceDTO getUserBalance(@RequestHeader(name = ConfigConstants.HEADER_AUTHORIZATION) String authorizationHeader) {
        String userId = Utils.getUserId(authorizationHeader);
        return userService.getUserBalance(userId);
    }

}
