package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {
    @Autowired
    private TypeTemplateDao typeTemplateDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 分页查询,有条件
     *
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        //从mysql数据库中查询所有模板结果集
        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(null);
        //将上面的结果集保存在缓存库中
        for (TypeTemplate template : typeTemplates) {
            //品牌列表             {"id":10,"text":"VIVO"}
            redisTemplate.boundHashOps("brandList").put(template.getId(),JSON.parseArray(template.getBrandIds(), Map.class));
            //规格列表  {"id":32,"text":"机身内存"}] 这里面好友一个规格选项列表,通过外键查询规格选项列表
            List<Map> specList = findBySpecList(template.getId());
            redisTemplate.boundHashOps("specList").put(template.getId(),specList);
        }



        //分页插件
        PageHelper.startPage(page, rows);
        PageHelper.orderBy("id desc");
        //String name = typeTemplate.getName();
        TypeTemplateQuery query = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = query.createCriteria();
        //条件查询
        if (typeTemplate.getName() != null && !typeTemplate.getName().trim().equals("")) {
            criteria.andNameLike("%" + typeTemplate.getName().trim() + "%");
        }
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(query);
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    /**
     * 删除
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        typeTemplateQuery.createCriteria().andIdIn(Arrays.asList(ids));
        typeTemplateDao.deleteByExample(typeTemplateQuery);
    }

    @Override
    public List<Map> findBySpecList(Long id) {
        //通过id获取模板列表中的规格集
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        String specIds = typeTemplate.getSpecIds();
        //将json串转为map对象
        List<Map> listMap = JSON.parseArray(specIds, Map.class);
        //循环遍历list取出对应的map
        for (Map map : listMap) {
            //通过外键查询规格选项集合
            SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
            //通过
            specificationOptionQuery.createCriteria().andSpecIdEqualTo((long) (Integer) map.get("id"));
            List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(specificationOptionQuery);
            //给map添加规格选项
            map.put("options", specificationOptions);
        }
        return listMap;
    }


}
