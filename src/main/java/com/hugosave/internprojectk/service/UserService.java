package com.hugosave.internprojectk.service;

import com.hugosave.intern.project.proto.*;

public interface UserService {
    UserAssetAndBalanceDTO getUserBalance(String userId);

    UserAuthResponseDTO userLogin(UserOnboardDTO userOnboard);

    UserAuthResponseDTO userSignup(UserOnboardDTO userOnboard);
}
