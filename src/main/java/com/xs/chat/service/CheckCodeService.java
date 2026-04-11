package com.xs.chat.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public interface CheckCodeService {
    String sendCheckCode(HttpServletResponse response, HttpSession session) throws IOException;

    String sendEmailCheckCode(HttpServletResponse response, HttpSession session) throws IOException;

    void sendQQEmailCode(String email, HttpSession session, String type);
}
