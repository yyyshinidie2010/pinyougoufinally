package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.user.Cart;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车管理
 */
@Service
public class CartServiceImpl implements  CartService {
    @Autowired
    private ItemDao itemDao;
    /**根据库存id查询库存对象
     * @param itemId
     * @return
     */
    @Override
    public Item findItemById(Long itemId) {
        return  itemDao.selectByPrimaryKey(itemId);

    }

    /**将购物车装满
     * @param cartList
     * @return
     */
    @Override
    public List<Cart> findCartList(List<Cart> cartList) {
        for (Cart cart : cartList) {
            Item item = null;
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                //通过获取库存对象来补全
                 item = findItemById(orderItem.getItemId());
                //设置图片
                orderItem.setPicPath(item.getImage());
                //设置标题
                orderItem.setTitle(item.getTitle());
                //设置单价
                orderItem.setPrice(item.getPrice());
                //设置总价,从orderItrm中获取的是对象,我么你要转为double然后进行运算,最后在返回一个对象
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));

            }
            //设置商家名称
            cart.setSellerName(item.getSeller()) ;

        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**合并新购物车到老购物车中(缓存中)
     * @param newCartList
     * @param name
     */
    @Override
    public void merge(List<Cart> newCartList, String name) {
         //1.获取缓存中的购物车结果集
        List<Cart> olderCartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
        //2.合并了新购物车到老购物车中(合并两个集合)
        olderCartList = mergeNewAndOld(newCartList,olderCartList);
        //3.将老购物车结果集保存到缓存中
        redisTemplate.boundHashOps("CART").put(name,olderCartList);


    }

    /**从缓存中取出购物车
     * @param name
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        return  (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
    }

    //提取一个合并新老购物车的方法
    public List<Cart> mergeNewAndOld(List<Cart> newCartList,List<Cart> oldCartList){
        //判断新的购物车是否有之
        if (null != newCartList && newCartList.size() > 0){
            //新车有值
         if (null != oldCartList && oldCartList.size() > 0){
             //老车有值,新车也有之合并
             for (Cart newCart : newCartList) {
                 //老购物车结果集中商家是否含有新购物车的商家
                 int newIndexOf = oldCartList.indexOf(new Cart());
                 if (newIndexOf != -1){
                     //-存在,找到在老购物车结果集中和新购物车相同的商家
                     Cart oldCart = oldCartList.get(newIndexOf);
                     //判断新购物车的新商品在老购物车中是否存在
                     List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                     //新购物车的商品集合
                     List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                     for (OrderItem newOrderItem : newOrderItemList) {
                         //判端新购物车中的新商品是存在老购物车的老购物商品结合中
                         int indexOf = oldOrderItemList.indexOf(newOrderItem);
                         if (indexOf != -1){
                             //--存在,老商品结果集中那个商品和新商品相同,追加数量
                             OrderItem oldOrderItem = oldOrderItemList.get(indexOf);
                             oldOrderItem.setNum(oldOrderItem.getNum()+newOrderItem.getNum());
                         }else {
                             //--不存在,添加新商品
                             oldOrderItemList.add(newOrderItem);
                         }
                     }

                 }else{
                     //-不存在添加新的购车
                     oldCartList.add(newCart);
                 }
             }

         }else {
             //老车没值
             return newCartList;
         }
        }
        return oldCartList;
    }
}
