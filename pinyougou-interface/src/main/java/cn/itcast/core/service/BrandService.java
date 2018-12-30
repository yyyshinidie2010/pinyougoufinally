package cn.itcast.core.service;

import entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    PageResult search(Integer pageNum, Integer pageSize, Brand brand);

    public List<Brand> findAll() throws Exception;

    PageResult findPage(Integer pageNum, Integer pageSize)throws Exception;


    void add(Brand brand)throws Exception;

    Brand findOne(Long id)throws Exception;

    void update(Brand brand)throws Exception;

    void delete(Long[] ids)throws Exception;

    List<Map> selectOptionList();
}
