package com.example.yin.service;

import com.example.yin.domain.User;

public interface UserService {
    void reg(User user);
    boolean veritypasswd(String username, String password);
}


