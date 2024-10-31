package com.example.yin.dao;

import com.example.yin.domain.User;

public interface UserMapper {
    Integer insert(User user);
    int verifyPassword(String username, String password);
}
