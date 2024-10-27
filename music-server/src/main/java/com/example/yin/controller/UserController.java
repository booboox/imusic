package com.example.yin.controller;


import com.example.yin.common.ErrorMessage;
import com.example.yin.common.SuccessMessage;
import com.example.yin.domain.Consumer;
import com.example.yin.domain.User;
import com.example.yin.service.UserService;
import com.example.yin.service.impl.ConsumerServiceImpl;
import com.example.yin.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private ConsumerServiceImpl consumerService;

    /**
     * 用户注册
     */
    @RequestMapping(value = "/register/cellphone", method = RequestMethod.POST)
    public JsonResult<Void> reg(User user){
        //创建响应结果对象
        JsonResult<Void> result = new JsonResult<>();
        userService.reg(user);
        result.setState(200);
        result.setMessage("用户注册成功");
        return result;
    }

    /**
     * 登录判断
     */
    @ResponseBody
    @RequestMapping(value = "/login/cellphone", method = RequestMethod.POST)
    public Object loginStatus(HttpServletRequest req, HttpSession session) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        boolean res = consumerService.veritypasswd(username, password);
        if (res) {
            session.setAttribute("username", username);
            return new SuccessMessage<List<Consumer>>("登录成功", consumerService.loginStatus(username)).getMessage();
        } else {
            return new ErrorMessage("用户名或密码错误").getMessage();
        }
    }
}
