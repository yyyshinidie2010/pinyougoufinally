package cn.itcast.core.service;

import entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import entity.Result;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    PageResult search(Integer page, Integer rows,Specification specification)throws Exception;

    SpecificationVo findOne(Long id)throws Exception;


    void add(SpecificationVo specificationVo)throws Exception;

    void update(SpecificationVo specificationVo);


    void delete(Long[] ids);

    List<Map> selectOptionList();
}
