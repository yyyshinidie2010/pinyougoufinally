package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pojogroup.SpecificationVo;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecificationDao specificationDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    /**
     * 分页查询,有条件
     * 分页查询查询的是pageresult,里面封装了当前页和当前页的结果集
     * @param page
     * @param rows
     * @param
     * @return
     * @throws Exception
     */
    @Override
    public PageResult search(Integer page, Integer rows,Specification specification) throws Exception {
        //分页插件
        PageHelper.startPage(page,rows);
        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        if (specification.getSpecName() != null && !specification.getSpecName().trim().equals("")){
            criteria.andSpecNameLike("%"+specification.getSpecName().trim()+"%");
        }
        //有条件查询
        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(specificationQuery);
        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public SpecificationVo findOne(Long id) {
        SpecificationVo specificationVo = new SpecificationVo();
        //查询规格
        Specification specification = specificationDao.selectByPrimaryKey(id);
        specificationVo.setSpecification(specification);
        //查询规格选项结果集
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(id);

        //排序
        query.setOrderByClause(" orders desc");
        List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(query);
        specificationVo.setSpecificationOptionList(specificationOptions);
        return specificationVo;
    }

    @Override
    public void add(SpecificationVo specificationVo) throws Exception {
        //规格表,要返回id,要当做现象结果表的外键
        specificationDao.insertSelective(specificationVo.getSpecification());

        //规格下选项结果表
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        //结果集循环遍历添加选项结果
        for (SpecificationOption specificationOption : specificationOptionList) {
            //将id作为外键添加到选想结果表中
            specificationOption.setSpecId( specificationVo.getSpecification().getId());
            //保存结果表
            specificationOptionDao.insertSelective(specificationOption);
        }

    }

    /**修改规格
     * @param specificationVo
     */
    @Override
    public void update(SpecificationVo specificationVo) {
       //修改规格
        specificationDao.updateByPrimaryKeySelective(specificationVo.getSpecification());
        //删除规格选项,根据外键删除
        Long id = specificationVo.getSpecification().getId();
        SpecificationOptionQuery specificationOptionQuery= new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria().andSpecIdEqualTo(id);
        specificationOptionDao.deleteByExample(specificationOptionQuery);
        //添加规格现象
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        for (SpecificationOption specificationOption : specificationOptionList) {
            //添加外键
            specificationOption.setSpecId(id);
            //添加选项
            specificationOptionDao.insertSelective(specificationOption);
        }
    }

    /**删除规格
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        //根据ids删除规格选项
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        specificationOptionQuery.createCriteria().andSpecIdIn(Arrays.asList(ids));
        specificationOptionDao.deleteByExample(specificationOptionQuery);
        //删除规格
        SpecificationQuery specificationQuery = new SpecificationQuery();
        specificationQuery .createCriteria().andIdIn(Arrays.asList(ids));
        specificationDao.deleteByExample(specificationQuery);
    }

    /**查询规格
     * @return
     */
    @Override
    public List<Map> selectOptionList() {

        return specificationDao.selectOptionList();
    }


}
