package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 用户管理类
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Destination smsDestination;
    @Autowired
    private UserDao userDao;

    @Override
    public void sendCode(String phone) {
        //随机生成6位验证码
        String s = RandomStringUtils.randomNumeric(6);
        //String  s="123456";
        //re
        redisTemplate.boundValueOps(phone).set(s);
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.DAYS);
        //发消息
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("phone",phone);
                mapMessage.setString("signName","品优购");
                mapMessage.setString("TemplateCode","SMS_152850232");
                mapMessage.setString("templateParam","{\"code\":\""+s+"\"}");
                return mapMessage;
            }
        });


    }

    @Override
    public void add(User user, String smscode) {
        //1.判段验证吗是否正确?
        String code= (String) redisTemplate.boundValueOps(user.getPhone()).get();
        if (null != smscode){
            if ( code.equals(code)){
                //验证码正确
                //添加到数据库
                user.setCreated(new Date());
                user.setUpdated(new Date());
                userDao.insertSelective(user);
            }else {
                //验证码不正确
                throw new RuntimeException("验证码错误");
            }
        }else {
            //验证码失效
            throw  new RuntimeException("验证码失效");
        }
    }
}