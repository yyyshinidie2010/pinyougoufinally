package cn.itcast.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义授权实现类
 */
@Component
public class UserDetailServiceImpl implements UserDetailsService{
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //创建权限对象
        Set<GrantedAuthority> authorities= new HashSet<>();
        //授权
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));//登录了
        //查询权限表授权
        //暂时没有
        return new User(username,"",authorities);//用户的在springsecurity中是不存储用户密码的,
                                                        // 因为我们在中央认证总系内存储了,不能存储在个人中心中
                                                        //而且password不能赋值为null否则报错,所以要配置"";
    }
}
