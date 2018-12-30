package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.*;


@Service
public class ItemsearchServiceImpl implements  ItemsearchService
{
    //索引库
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    //搜索
    public Map<String,Object> search(Map<String,String> searchMap){
        //搜索关键词去空格replace,是将替换全部
      searchMap.put("keywords",searchMap.get("keywords").replaceAll(" ",""));
        //结果集
        //总条数
        Map<String, Object> map = search1(searchMap);
        //商品分类
        List<String> categoryList = searchCategoryByKeywords(searchMap);
        map.put("categoryList",categoryList);
        //商品品牌
        //商品规格
        if (null != categoryList && categoryList.size() > 0){
            //企业需求查询第一个分类的数据,根据分类查询,规格id
            Object typeId = redisTemplate.boundHashOps("itemCat").get(categoryList.get(0));
            //根据typeId,查询品牌列表,和 规格和列表
            //商品品牌
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            //商品规格
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("brandList",brandList);
            map.put("specList",specList);

        }
        return map;
    }
    //查询商品分类结果集
    public List<String> searchCategoryByKeywords(Map<String,String> searchMap){
        //分组查询(因为分类是重复的所以要分组查询)
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query query = new SimpleQuery(criteria);
        //分组设置分组域
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        List<String> categoryList = new ArrayList<>();
        //执行分组查询
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);
        //分组结果指定对应的域名
        GroupResult<Item> itemCategory = groupPage.getGroupResult("item_category");
        List<GroupEntry<Item>> content = itemCategory.getGroupEntries().getContent();
        for (GroupEntry<Item> itemGroupEntry : content) {
          categoryList.add(itemGroupEntry.getGroupValue());
        }

        return categoryList;

    }

    //结果集
    //总条数
    //定义搜索对象的结构  category:商品分类
//    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'',
// 'pageNo':1,'pageSize':40,'sort':'','sortField':''};
    public Map<String,Object> search1(Map<String,String> searchMap){
        //关键词查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        HighlightQuery highlightQuery = new SimpleHighlightQuery(criteria);
        //过滤 //    $scope.searchMap={''category':'','brand':'','spec':{},'price':'',)
        //分类
        if (null != searchMap.get("category") && !"".equals(searchMap.get("category").trim())){
            FilterQuery filterQuery = new SimpleQuery(new Criteria("item_category").is(searchMap.get("category").trim()));
            highlightQuery.addFilterQuery(filterQuery);
        }

        //品牌
        if (null != searchMap.get("brand") && !"".equals(searchMap.get("brand").trim())){
            FilterQuery filterQuery = new SimpleQuery(new Criteria("item_brand").is(searchMap.get("brand").trim()));
            highlightQuery.addFilterQuery(filterQuery);
        }

        //价格 'price','0-500'  'price','3000-*'
        if (null != searchMap.get("price") && !"" .equals(searchMap.get("price").trim())){
            String[] prices = searchMap.get("price").trim().split("-");
            FilterQuery filterQuery = null;
            //判断字段是否含有"*"
           if (searchMap.get("price").trim().contains("*")){
               //含有
                 filterQuery = new SimpleQuery(new Criteria("item_price").greaterThanEqual(prices[0]));
           }else {
               //不含
               filterQuery = new SimpleQuery(new Criteria("item_price").between(prices[0],prices[1],true,false));
           }
            highlightQuery.addFilterQuery(filterQuery);
        }
        //规格 "item_spec_网络": "联通3G",
        //     "item_spec_机身内存": "16G",
        if (null != searchMap.get("spec") && !"".equals(searchMap.get("spec"))){
            Map<String,String> itemSpec = JSON.parseObject(searchMap.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entries = itemSpec.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                FilterQuery filterQuery = new SimpleQuery(new Criteria("item_spec_"+entry.getKey()).is(entry.getValue()));
                highlightQuery.addFilterQuery(filterQuery);
            }
        }
        //排序
        //'sort':'','sortField':''};
        //判断sortField,不为空(也不为''),他们成对出现判断一个就相当于域判断两个
        if (null != searchMap.get("sort") && !"".equals(searchMap.get("sort"))){
            //判断排序方式升?降
            if ("DESC".equals(searchMap.get("sort"))){
                highlightQuery.addSort(new Sort(Sort.Direction.DESC,"item_"+searchMap.get("sortField")));
            }else {
                highlightQuery.addSort(new Sort(Sort.DEFAULT_DIRECTION,"item_"+searchMap.get("sortField")));
            }
        }

        // 分页
        //设置偏移量  偏移量=(当前页-1)*每页的条数
        highlightQuery.setOffset((Integer.parseInt(searchMap.get("pageNo"))-1)*Integer.parseInt(searchMap.get("pageSize")));
        //每页数
        highlightQuery.setRows(Integer.parseInt(searchMap.get("pageSize")));
        //开启高亮
        HighlightOptions highlightOptions = new HighlightOptions();
            //高亮域设置为标题title
        highlightOptions.addField("item_title");
            //前缀
        highlightOptions.setSimplePrefix("<span style='color:red'>");
            //后缀
        highlightOptions.setSimplePostfix("</span >");
            //参数是一个对象,因为高亮需要设置 高亮域,前后缀
        highlightQuery.setHighlightOptions(highlightOptions);

        Map<String,Object> map = new HashMap<>();
        //执行查询
        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(highlightQuery, Item.class);
        //获取高亮的字段,他在高亮域highlighted里
        List<HighlightEntry<Item>> highlighted = page.getHighlighted();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            //从高亮域中获取高亮的字段,放入显示的title中
            Item item = itemHighlightEntry.getEntity();
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
            if (null != highlights && highlights.size() > 0){
                //高亮的名称
                item.setTitle( highlights.get(0).getSnipplets().get(0));
            }

        }
        //结果集
        map.put("rows",page.getContent());
        //总条数
        map.put("total",page.getTotalElements());
        //总页数
        map.put("totalPages",page.getTotalPages());
        return map;
    }
}
