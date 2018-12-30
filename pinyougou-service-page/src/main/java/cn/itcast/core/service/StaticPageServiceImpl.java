package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;


import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 静态化处理实现类
 */
@Service
public class StaticPageServiceImpl implements StaticPageService , ServletContextAware {
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private GoodsDao goodsDao;
    //静态化处理方法
    public void index(Long id)  {
        //输出路径(绝对路径 )
        String allPath=getPath("/"+id+".html");

        //创建Freemarker(创建freemarker的时候,他在配置问价就已经配置了相对路径了)
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        //数据
        Map<String,Object> root = new HashMap<>();
        //商品详情表
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        root.put("goodsDesc",goodsDesc);
        //库存结果集
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        root.put("itemList",itemList);
        //商品表
        Goods goods = goodsDao.selectByPrimaryKey(id);
        root.put("goods",goods);
        //库存分类表
        //一级分类
        root.put("itemCat1",itemCatDao.selectByPrimaryKey( goods.getCategory1Id()).getName());
        //二级分类
        root.put("itemCat2",itemCatDao.selectByPrimaryKey( goods.getCategory2Id()).getName());
        //三级分类
        root.put("itemCat3",itemCatDao.selectByPrimaryKey( goods.getCategory3Id()).getName());




        //输出流
        Writer out = null;
        try {
            //读取模板文件
            Template template = configuration.getTemplate("item.ftl");
            out = new OutputStreamWriter(new FileOutputStream(allPath),"UTf-8");
            template.process(root,out);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                    if (null != out) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    //获取全路径
    public String getPath(String path){
        return servletContext.getRealPath(path);
    }

    private ServletContext servletContext;
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
