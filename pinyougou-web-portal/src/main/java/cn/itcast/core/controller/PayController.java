package cn.itcast.core.controller;


import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付管理
 */
@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private PayService payService;
    @RequestMapping("/createNative")
    public Map<String, String> createNative() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return  payService.createNative(name);

    }
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        try {
            int x = 0;
            while (true){
                Map<String,String> map =payService.queryPayStatus(out_trade_no);
                //判断支付结果
                if ("SUCCESS".equals(map.get("trade_state"))){
                    return new Result(true,"交易成功");
                }if ("NOTPAY".equals(map.get("trade_state")) ||
                        "CLOSED".equals(map.get("trade_state"))
                        || "REVOKED".equals(map.get("trade_state"))
                        || "USERPAYING".equals(map.get("trade_state"))
                        || "PAYERROR".equals(map.get("trade_state"))) {

                    Thread.sleep(3000);
                    x++;
                    if (x > 100) {
                        //五分钟
                        //调用微信那边关闭订单Api 我不想写了哭哭
//                        String name = SecurityContextHolder.getContext().getAuthentication().getName();
//                        Map<String,String> closeMap=payService.closeOrder(name);
//                        if ("SUCCESS".equals(closeMap.get("return_code"))){
//                            if ("SUCCESS".equals(closeMap.get("result_code"))){
//                                System.out.println("关闭微信订单成功");
//                            }
//                        }

                        return new Result(false, "二维码超时");
                    }
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"支付失败");
        }
    }

}
