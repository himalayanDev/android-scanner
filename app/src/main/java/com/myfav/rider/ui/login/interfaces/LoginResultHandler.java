package com.myfav.rider.ui.login.interfaces;

import com.myfav.rider.ui.login.data.LoginError;

import java.util.List;

public interface LoginResultHandler {
    void onSuccess();

    void onValidationError(List<LoginError> errorList);

    void onServerError(String message);
}
