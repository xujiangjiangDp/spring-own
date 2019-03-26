package com.gupaoedu;

import com.gupaoedu.annotation.GPAutowired;
import com.gupaoedu.annotation.GPController;
import com.gupaoedu.annotation.GPRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@GPController
@GPRequestMapping("/index")
public class IndexController {
    @GPAutowired
    private Service service;

    @GPRequestMapping("/msg")
    public void msg(HttpServletRequest req, HttpServletResponse resp){
        try {
            resp.getWriter().write(service.msg());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
