package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {
    @Autowired
    private ContentDao contentDao;
    @Override
    public PageResult search(Integer page, Integer rows, Content content) {
        PageHelper.startPage(page,rows);
        Page<Content> p = (Page<Content>) contentDao.selectByExample(null);
        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public void add(Content content) {
        //更新数据库
        contentDao.insertSelective(content);
        //根据分类id清空缓存
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
    }

    @Override
    public Content findOne(Long id) {
        return contentDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Content content) {
        //根据主键查询未更改前的数据的categoryId
        Long categoryId = contentDao.selectByPrimaryKey(content.getId()).getCategoryId();
        //更新数据库
        contentDao.updateByPrimaryKeySelective(content);
        //判断两个id是否相等
        if (!content.getCategoryId().equals(categoryId)){
        //不相同清除原来的广告缓存分类
            redisTemplate.boundHashOps("content").delete(categoryId);
        }
        //清除现在的广告分类
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());

    }

    @Override
    public void delete(Long[] ids) {
        //更新数据库
        Set<Long> longs = new HashSet<>();
        if (ids != null) {
            for (Long id : ids) {
                //获取选中的categryId
                Long categoryId = contentDao.selectByPrimaryKey(id).getCategoryId();
                //去重
                longs.add(categoryId);
                contentDao.deleteByPrimaryKey(id);
            }
            //根据categoryId删除缓存
            for (Long aLong : longs) {
                redisTemplate.boundHashOps("content").delete(aLong);
            }
        }

    }
    @Autowired
    private RedisTemplate redisTemplate;
    /**根据广告分类id查询广告结果集
     * @param categoryId
     * @return
     */
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        //1,先查询缓
        List<Content> contentList = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
        if (null == contentList){
            //2.在查询数据库
            ContentQuery contentQuery = new ContentQuery();
            contentQuery.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
            contentQuery.setOrderByClause("sort_order desc");
            contentList = contentDao.selectByExample(contentQuery);
            //3.将数据库值存储到redis
            redisTemplate.boundHashOps("content").put(categoryId,contentList);
            ///设置时间
            redisTemplate.boundHashOps("content").expire(24, TimeUnit.HOURS);


        }
        return contentList;


    }
}
