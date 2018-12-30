package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import pojogroup.GoodsVo;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.*;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private SellerDao sellerDao;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination topicPageAndSolrDestination;
    @Autowired
    private Destination queueSolrDeleteDestination;

    /**
     * 添加商品数据
     *
     * @param goodsVo 商品对象private Goods goods;
     *                商品详情对象private GoodsDesc goodsDesc;
     *                库存对象结果集private List<Item> itemList;
     */
    @Override
    public void add(GoodsVo goodsVo) {
        //商品表:
        //审核状态,设置为0,未审核
        goodsVo.getGoods().setAuditStatus("0");
        //添加商品表
        goodsDao.insertSelective(goodsVo.getGoods());
        //商品详情表
        //获取商品表的id作为详情的id
        goodsVo.getGoodsDesc().setGoodsId(goodsVo.getGoods().getId());
        //添加商品详情表
        goodsDescDao.insertSelective(goodsVo.getGoodsDesc());
        //判断是否启用规格
        if ("1".equals(goodsVo.getGoods().getIsEnableSpec())) {
            //启用,开启sku 库存表,多个
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {
                //标题title=商品名称+""+规格1+""+规格2
                //先获取商品名称:
                String title = goodsVo.getGoods().getGoodsName();
                //规格
                String spec = item.getSpec();
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = specMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += entry.getValue();
                }
                item.setTitle(title);
                //商品的第一张图片
                String itemImages = goodsVo.getGoodsDesc().getItemImages();
                List<Map> images = JSON.parseArray(itemImages, Map.class);
                if (null != images && images.size() > 0) {
                    item.setImage((String) images.get(0).get("url"));
                }
                //商品的3级分类ID和商品的分类名称
                item.setCategoryid(goodsVo.getGoods().getCategory3Id());
                String itemCatName = itemCatDao.selectByPrimaryKey(goodsVo.getGoods().getCategory3Id()).getName();
                item.setCategory(itemCatName);
                //添加时间
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());
                //商品表id 是本表的外键
                item.setGoodsId(goodsVo.getGoods().getId());
                //商家id
                item.setSellerId(goodsVo.getGoods().getSellerId());
                //商家公司名
                item.setSeller(sellerDao.selectByPrimaryKey(goodsVo.getGoods().getSellerId()).getName());
                //品牌名
                item.setBrand(brandDao.selectByPrimaryKey(goodsVo.getGoods().getBrandId()).getName());
                //保存商品详情列
                itemDao.insertSelective(item);
            }


        }


    }

    /**
     * 分页条件查询
     *
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        //分页助手
        PageHelper.startPage(page, rows);
        //排序
        PageHelper.orderBy("id desc");
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        //判断商品状态
        if (null != goods.getAuditStatus() && !"".equals(goods.getAuditStatus())) {
            criteria.andAuditStatusEqualTo(goods.getAuditStatus());
        }
        if (null != goods.getGoodsName() && !"".equals(goods.getGoodsName())) {
            criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
        }//判断商家id不为null.如果为null就是运营商,就查询所有商品数据
        if (null != goods.getSellerId()) {
            //查询当前商家的商品
            criteria.andSellerIdEqualTo(goods.getSellerId());
        }
        //只查询不删除的
        criteria.andIsDeleteIsNull();
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 回显
     *
     * @param id
     * @return
     */
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo = new GoodsVo();
        //商品表
        goodsVo.setGoods(goodsDao.selectByPrimaryKey(id));
        //商品详情表
        goodsVo.setGoodsDesc(goodsDescDao.selectByPrimaryKey(id));
        //库存结果表
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        goodsVo.setItemList(itemDao.selectByExample(itemQuery));

        return goodsVo;
    }

    @Override
    public void update(GoodsVo goodsVo) {
        //商品表
        goodsDao.updateByPrimaryKeySelective(goodsVo.getGoods());
        //商品详情表
        goodsDescDao.updateByPrimaryKeySelective(goodsVo.getGoodsDesc());
        //库存表
        //先删除,再添加
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goodsVo.getGoods().getId());
        itemDao.deleteByExample(itemQuery);
        //添加
        //判断是否启用规格
        if ("1".equals(goodsVo.getGoods().getIsEnableSpec())) {
            //启用,开启sku 库存表,多个
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {
                //标题title=商品名称+""+规格1+""+规格2
                //先获取商品名称:
                String title = goodsVo.getGoods().getGoodsName();
                //规格
                String spec = item.getSpec();
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = specMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += entry.getValue();
                }
                item.setTitle(title);
                //商品的第一张图片
                String itemImages = goodsVo.getGoodsDesc().getItemImages();
                List<Map> images = JSON.parseArray(itemImages, Map.class);
                if (null != images && images.size() > 0) {
                    item.setImage((String) images.get(0).get("url"));
                }
                //商品的3级分类ID和商品的分类名称
                item.setCategoryid(goodsVo.getGoods().getCategory3Id());
                String itemCatName = itemCatDao.selectByPrimaryKey(goodsVo.getGoods().getCategory3Id()).getName();
                item.setCategory(itemCatName);
                //添加时间
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());
                //商品表id 是本表的外键
                item.setGoodsId(goodsVo.getGoods().getId());
                //商家id
                item.setSellerId(goodsVo.getGoods().getSellerId());
                //商家公司名
                item.setSeller(sellerDao.selectByPrimaryKey(goodsVo.getGoods().getSellerId()).getName());
                //品牌名
                item.setBrand(brandDao.selectByPrimaryKey(goodsVo.getGoods().getBrandId()).getName());
                //保存商品详情列
                itemDao.insertSelective(item);
            }
        }

    }

    /**
     * 删除商品就是给商品的dele赋值为1
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        Goods goods = new Goods();
        goods.setIsDelete("1");
        for (Long id : ids) {
            goods.setId(id);
            goodsDao.updateByPrimaryKeySelective(goods);
            //发消息
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(String.valueOf(id));
                }
            });


        }


    }

    /**商品审核
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        Goods goods = new Goods();
        goods.setAuditStatus(status);
        for (Long id : ids) {
            goods.setId(id);
            //商品状态
            goodsDao.updateByPrimaryKeySelective(goods);
            //判断是否通过
            if ("1".equals(status)){
                //发消息
                jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(String.valueOf(id));

                    }
                });
            }






















        }


    }

}
