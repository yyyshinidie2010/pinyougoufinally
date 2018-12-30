package cn.itcast.core.service;

import cn.itcast.common.utils.IdWorker;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.user.Cart;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RedisTemplate redisTemplate;//提高效率将
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private PayLogDao payLogDao;

    /**
     * 保存订单主表和订单详情表
     *
     * @param order
     */
    @Override
    public void add(Order order) {
        //支付的金额(支付日志的金额)
        double TP = 0;
        //设置订单集合
        List<String> ids = new ArrayList<>();
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(order.getUserId());
        if (null != cartList && cartList.size() > 0){
            for (Cart cart : cartList) {
                //设置订单ID,使用IK分词器tiwter公司的
                long id = idWorker.nextId();
                ids.add(String.valueOf(id));
                order.setOrderId(id);
                //实付金额
                double totalPrice = 0;
                //状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
                order.setStatus("1");
                //订单创建时间
                order.setCreateTime(new Date());
                //订单更新时间
                order.setUpdateTime(new Date());
                //设置来源2:PC端
                order.setSourceType("2");
                //设置商家ID
                order.setSellerId(cart.getSellerId());

                //保存订单详情
                List<OrderItem> orderItemList = cart.getOrderItemList();
                for (OrderItem orderItem : orderItemList) {
                    //库存Id,和购买的数量,我们要补全库存对象去数据库查询
                    Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                    //设置订单详情ID
                    orderItem.setId(idWorker.nextId());
                    //设置商品Id
                    orderItem.setGoodsId(item.getGoodsId());
                    //订单ID
                    orderItem.setOrderId(id);
                    //商品标题
                    orderItem.setTitle(item.getTitle());
                    //商品单价
                    orderItem.setPrice(item.getPrice());
                    //图片
                    orderItem.setPicPath(item.getImage());
                    //商家id
                    orderItem.setSellerId(item.getSellerId());
                    //小计
                    orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum()));

                    totalPrice += orderItem.getTotalFee().doubleValue();
                    //保存订单详情表:
                    orderItemDao.insertSelective(orderItem);
                }
                    //实付金额
                order.setPayment(new BigDecimal(totalPrice));
                TP += order.getPayment().doubleValue();
                //保存订单
                orderDao.insertSelective(order);
            }
            //保存完订单时候我么那要清空购物车
            redisTemplate.boundHashOps("CART").delete(order.getUserId());
            //几张订单,二张
             //付款 一次 合并
            //银行流水
            //支付日志表
            //订单的ID == 支付ID +总金额 + 银行流水
            PayLog payLog = new PayLog();
            //支付日志的ID必须唯一
            long id = idWorker.nextId();
            payLog.setOutTradeNo(String.valueOf(id));
            //生成时间
            payLog.setCreateTime(new Date());
            //用户ID
            payLog.setUserId(order.getUserId());
            //支付状态0:未支付
            payLog.setTradeState("0");
            //总金额  以分为单位(微信接口要的就是分)
            payLog.setTotalFee((long)(TP*100));
            //订单集合 订单号的集合 986096985763217408, 986096985784188928
            payLog.setOrderList(ids.toString().replace("[","").replace("]",""));
            //设置交易类型 在线支付:1
            payLog.setPayType("1");
            //保存日志表
            payLogDao.insertSelective(payLog);
            //优化,将日志表放入缓存中
            redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
        }
    }
}
