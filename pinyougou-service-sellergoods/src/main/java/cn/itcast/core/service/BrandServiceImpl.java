package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDao brandDao;

    public List<Brand> findAll() throws Exception {
        return brandDao.selectByExample(null);
    }

    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) throws Exception {
        //分页插件
        PageHelper.startPage(pageNum, pageSize);
        //执行查询
        Page<Brand> p = (Page<Brand>) brandDao.selectByExample(null);

        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public void add(Brand brand) throws Exception {
        brandDao.insertSelective(brand);
    }

    @Override
    public Brand findOne(Long id) throws Exception {
        return brandDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Brand brand) throws Exception {
        brandDao.updateByPrimaryKeySelective(brand);

    }

    @Override
    public void delete(Long[] ids) throws Exception {
        //BrandQUery.createCriter():条件对象
        BrandQuery brandQuery = new BrandQuery();
        brandQuery.createCriteria().andIdIn(Arrays.asList(ids));
        brandDao.deleteByExample(brandQuery);
    }

    /**
     * 分页查询有条件
     *
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {
        PageHelper.startPage(pageNum, pageSize);//分页控件
        BrandQuery brandQuery = new BrandQuery();
        //提取brandQuery.createCriteria()
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        //判断查询条件不为null而且查询条件去除两侧空格后不为空串
        if (brand.getName() != null && !brand.getName().trim().equals("")) {
            criteria.andNameLike("%" + brand.getName().trim() + "%");
        }
        if (brand.getFirstChar() != null && !brand.getFirstChar().trim().equals("")) {
            criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
        }
        Page<Brand> p = (Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 查询品牌.返回list<map>
     *
     * @return
     */
    @Override
    public List<Map> selectOptionList() {

        return brandDao.selectOptionList();
    }


}
