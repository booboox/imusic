package com.example.yin;

import com.example.yin.dao.UserMapper;
import com.example.yin.domain.User;
import com.example.yin.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class YinMusicApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService iUserService;

    @Test
    public void insert() {
        User user = new User();
        user.setUsername("user01");
        user.setPassword("123456");
        Integer rows = userMapper.insert(user);
        System.out.println("rows=" + rows);
    }

    @Test
    public void reg() {
            User user = new User();
            user.setUsername("user11");
            user.setPassword("123456");
            iUserService.reg(user);
            System.out.println("注册成功！");
    }
}
