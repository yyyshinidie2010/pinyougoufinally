package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.GoodsVo;


@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;

    /**添加商品
     * @param goodsVo
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsVo goodsVo){
        try {
            //获取商家ID,
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            goodsVo.getGoods().setSellerId(name);
            goodsService.add(goodsVo);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    /**条件查询
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page,Integer rows,@RequestBody Goods goods){
        //分页查询不需要查询本商家的的商品
      return   goodsService.search(page,rows,goods);

    }

    /**商品回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public GoodsVo findOne(Long id){
       return goodsService.findOne(id);
    }
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsVo goodsVo){

        try {
            goodsService.update(goodsVo);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }

    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){

        try {
            goodsService.delete(ids);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }

    }
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            goodsService.updateStatus(ids,status);
            return new Result(true,"审核成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核失败");
        }
    }

}

