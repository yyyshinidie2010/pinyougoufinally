package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import entity.PageResult;

public interface SellerService {

    PageResult search(Integer page, Integer rows, Seller seller);

    Seller findOne(String sellerId);

    void update(String sellerId, String status);

    void add(Seller seller);

}
