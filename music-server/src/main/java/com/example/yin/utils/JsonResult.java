package com.example.yin.utils;

//创建响应类，包括状态码、状态描述信息、数据，这部分功能封装在一个类中，将这个类作为方法的返回值，返回给前端浏览器

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * Json格式的数据进行响应
 */
// @JsonInclude(JsonInclude.Include.ALWAYS)//始终返回所有属性，即使为空
//在application.properties中设置了服务器不会向客户端响应值为NULL的属性
public class JsonResult<E> implements Serializable {//一个类中有泛型的数据类型，在类声明时也要声明成泛型

    /** 状态码 */
    private Integer state;
    /** 描述信息*/
    private String message;
    /**对应响应的数据*/
    //注册一般没有要响应给前端的数据，查询自然有，所以数据类型不确定，用泛型来表示
    private E data;

    public JsonResult() {
    }

    public JsonResult(Integer state) {
        this.state = state;
    }

    public JsonResult(Throwable e) {
        this.message = e.getMessage();
    }

    public JsonResult(Integer state, E data) {
        this.state = state;
        this.data = data;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }
}

