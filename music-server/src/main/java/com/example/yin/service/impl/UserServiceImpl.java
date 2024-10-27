package com.example.yin.service.impl;

import com.example.yin.dao.ConsumerMapper;
import com.example.yin.dao.UserMapper;
import com.example.yin.domain.User;
import com.example.yin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsumerMapper consumerMapper;

    @Override
    public void reg(User user) {
        Integer rows = userMapper.insert(user);
        // 判断受影响的行数是否不为1
        if (rows == 1) {
            System.out.println("插入成功");
        }else{
            System.out.println("插入失败");
        }
    }

    @Override
    public boolean veritypasswd(String username, String password) {
        return consumerMapper.verifyPassword(username, password) > 0;
    }
}
