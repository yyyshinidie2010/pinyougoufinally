package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;

import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    private ItemCatService itemCatService;

    /**通过父id查询商品分类结果集
     * @param parentId
     * @return
     */
    @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(Long parentId){
        List<ItemCat> itemCatList = itemCatService.findByParentId(parentId);
        return itemCatList ;
    }

    /**新建商品分类
     * @param itemCat
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody ItemCat itemCat){
        try {
            itemCatService.add(itemCat);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"添加失败");
        }

    }

    /**分类回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public ItemCat findOne(Long id){
        return itemCatService.findOne(id);

    }
    /**回显所有分类优化
     * @return
     */
    @RequestMapping("/findAll")
    public List<ItemCat> findAll(){
        return itemCatService.findAll();

    }

    /**修改分类
     * @param itemCat
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ItemCat itemCat){
        try {
            itemCatService.update(itemCat);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"添加失败");
        }

    }

    /**删除分类
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            itemCatService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"删除失败");
        }

    }
}
