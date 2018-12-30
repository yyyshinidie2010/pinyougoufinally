package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.user.Cart;

import cn.itcast.core.service.CartService;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = {"http://localhost:9003"})//解决跨域请求的注解
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {

        try {
            List<Cart> cartList = null;
            //判断Cookie中是否含有购物车
            boolean k =false;
            //判断是否登录
            //从安全框架中获取用户名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            /*1.获取Cookie 数组*/
            Cookie[] cookies = request.getCookies();
            if (null != cookies && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    /*2.获取cookie中的购物车(集合,一个cookie中有多个个购物车)*/
                    if ("CART".equals(cookie.getName())) {
                        cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                        k=true;
                        break;//找到之后他就跳出,节省性能
                    }
                }
            }
            /*3.没有就创建购物车*/
            if (null == cartList) {
                cartList = new ArrayList<>();
            }
            /*4.追加当前款项*/
            //入参.Long item , Integer num,
            //创建一个新的购物车
            Cart newCart = new Cart();
            //根据库存id查询库存信息
            Item item= cartService.findItemById(itemId);
            //商家id
            newCart.setSellerId(item.getSellerId());
            //库存
            OrderItem newOrderItem = new OrderItem();
            //设置库存id
            newOrderItem.setItemId(itemId);
            //库存数量
            newOrderItem.setNum(num);
            //商品结果集
            List<OrderItem> newOrderItemList = new ArrayList<>();
            newOrderItemList.add(newOrderItem);

            newCart.setOrderItemList(newOrderItemList);
            //添加新购物车
            //判断新购物车的商家是谁,是否在老购物车结果集中存在
            int newIndexOf = cartList.indexOf(newCart);//不存在:-1,存在:<=0 返回角标
            if (newIndexOf != -1){
                //-存在,从老购物车的结果集中找到和新购物车的商家id一样的购物车
                Cart oldCart = cartList.get(newIndexOf);
                List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                int indexOf = oldOrderItemList.indexOf(newOrderItem);
                if (indexOf != -1){
                    //判断新购物车中的 新商品在老购物车中 是否存在
                    //--存在.老购物车商品的结果集中有和新商品一样的,追加数量
                    OrderItem ordOrderItem = oldOrderItemList.get(indexOf);
                    ordOrderItem.setNum(ordOrderItem.getNum()+newOrderItem.getNum());

                }else {
                    //--不存在.添加新商品
                    oldOrderItemList.add(newOrderItem);
                }

            }else{
                //-不存在,添加购物车
                cartList.add(newCart);

            }




            if (!"anonymousUser".equals(name)) {
                //已登录
                //5.将当前的购物车合并到原来的购物车中
                cartService.merge(cartList,name);//合并购物车时要判断是哪个用户的购物车所以入参要加上用户名
                //6.清空cookie;
                if (k){
                    Cookie cookie = new Cookie("CART",null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }

            } else {
                /*未登陆*/


                /*5,将购物车 保存到cookie中*/
                Cookie cookie = new Cookie("CART", JSON.toJSONString(cartList));
                //设置存活时间
                cookie.setMaxAge(60*60*24*5);
                //设置路径
                cookie.setPath("/");//设置path可以在同一个tomcat上
                /*6会写到浏览器中*/
                response.addCookie(cookie);

            }

            return new Result(true, "添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加购物车失败");
        }
    }

    //查询购物车
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) {
        List<Cart> cartList=null;

        //1.获取cookie数组
        Cookie[] cookies = request.getCookies();
        if (null != cookies && cookies.length > 0){
            for (Cookie cookie : cookies) {
                //2.获取cookie中的购物车结果集
                if ("CART".equals(cookie.getName())){
                    cartList =JSON.parseArray(cookie.getValue(),Cart.class);
                    break;
                }
            }
        }
        //判断是否登陆
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(name)){
            //已登录
            //3.有,将购物车合并到账户中的原购物车中 清空cookie
            if (null != cartList ){
                cartService.merge(cartList,name);
                Cookie cookie = new Cookie("CART",null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            //4,将从账户中取出购物车(从缓存中取出来)
            cartList=cartService.findCartListFromRedis(name);
        }
        //5.有,将购物车结果集装满
        if (cartList != null ){
            cartList=cartService.findCartList(cartList);
        }
        //6.回显

        return cartList;
    }
}

