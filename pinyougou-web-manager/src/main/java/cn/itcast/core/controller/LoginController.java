package cn.itcast.core.controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {


    /**获取当前登录人信息
     * @return
     */
    @RequestMapping("/showName")
    public Map<String,Object> showName(){
        //使用security从session中回去登录名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> map = new HashMap<>();
        map.put("username",name);
        map.put("curTime",new Date());
        return map;
    }
}
