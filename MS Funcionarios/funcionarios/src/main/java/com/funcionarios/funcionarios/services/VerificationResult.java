package com.funcionarios.funcionarios.services;

import com.funcionarios.funcionarios.models.dto.LoginDTO;

public class VerificationResult {
    private LoginDTO loginDTO;
    private String error;

    public VerificationResult(LoginDTO loginDTO) {
        this.loginDTO = loginDTO;
        this.error = null;
    }

    public VerificationResult(String error) {
        this.error = error;
        this.loginDTO = null;
    }

    public boolean isSuccess() {
        return loginDTO != null;
    }

    public LoginDTO getLoginDTO() {
        return loginDTO;
    }

    public String getError() {
        return error;
    }
}
