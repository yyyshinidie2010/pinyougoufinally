package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义实认证现类
 */
public class UserDetailServiceImpl implements UserDetailsService {
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //入参用户名
        //返回商家对象
        Seller seller = sellerService.findOne(username);
        if (null != seller){
            //用户名存在
            if ("1".equals(seller.getStatus())){
                //通过审核
                //权限集合
                Set<GrantedAuthority> authorities= new HashSet<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
                return new User(username,seller.getPassword(),authorities);
            }
        }
        return null;
    }
}
