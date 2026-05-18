package com.example.dropby.models;

import com.google.gson.annotations.SerializedName;

public class OtpRequest {
    @SerializedName("user_id")  public int userId;
    @SerializedName("otp_code") public String otpCode;

    public OtpRequest(int userId, String otpCode) {
        this.userId  = userId;
        this.otpCode = otpCode;
    }
}