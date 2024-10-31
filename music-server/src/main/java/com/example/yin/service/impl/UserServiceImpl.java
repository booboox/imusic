package com.example.yin.service.impl;

import com.example.yin.dao.ConsumerMapper;
import com.example.yin.dao.UserMapper;
import com.example.yin.domain.User;
import com.example.yin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsumerMapper consumerMapper;

    /**
     * 注册
     * @param user 注册有关提交的用户信息
     */
    @Override
    public void reg(User user) {

        String oldPassword=user.getPassword();
        //获取盐值（随机生成一个盐值）
        String salt = UUID.randomUUID().toString().toUpperCase();//Universally Unique Identifier 通用唯一标识符
        //补全数据：盐值的记录
        user.setSalt(salt);
        //将密码和盐值作为一个整体进行加密处理,忽略原有密码强度提升了数据的安全
        String md5Password=getMD5Password(oldPassword,salt);
        //将加密之后的密码重新补全设置到User对象中
        user.setPassword(md5Password);

        Integer rows = userMapper.insert(user);
        // 判断受影响的行数是否不为1
        if (rows == 1) {
            System.out.println("插入成功");
        }else{
            System.out.println("插入失败");
        }
    }

    /**
     * 登录验证
     * @param username 用户名
     * @param password 密码
     * @return Boolean
     */
    @Override
    public boolean veritypasswd(String username, String password) {
        return consumerMapper.verifyPassword(username, password) > 0;
    }

    /**
     * 定义一个md5算法的加密处理
     * @param password 用户输入的密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    private String getMD5Password(String password,String salt){

        for (int i = 0; i < 3; i++) {
            //md5加密算法方法的调用(进行三次加密)
            password = DigestUtils.md5DigestAsHex((salt + password + salt).getBytes()).toUpperCase();
            //getBytes() 方法将拼接后的字符串转换为字节数组，md5哈希算法参数要求任意长度的字节数组
        }
        return password;
    }
}
