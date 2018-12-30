package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Seller seller){
        return sellerService.search(page,rows,seller);
    }
    @RequestMapping("/findOne")
    public Seller findOne(String sellerId){
       return sellerService.findOne(sellerId);
    }
    @RequestMapping("/updateStatus")
    public Result update(String sellerId, String status){
        try {
            sellerService.update(sellerId,status);
            return new Result(true,"审核成功");
        } catch (Exception e) {
            //e.printStackTrace();
            return new Result(false,"审核失败");
        }

    }
}

