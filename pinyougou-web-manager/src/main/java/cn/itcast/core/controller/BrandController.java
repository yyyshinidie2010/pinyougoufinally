package cn.itcast.core.controller;

import entity.PageResult;
import entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;


import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 品牌管理
 */
@RestController
@RequestMapping("/brand")
public class BrandController {


    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<Brand> findAll()throws Exception{
        return brandService.findAll();
    }
    @RequestMapping("/findPage")
    public PageResult findPage(Integer pageNum,Integer pageSize)throws Exception{
        return brandService.findPage(pageNum,pageSize);
    }
    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand){
        try {
            brandService.add(brand);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            return new Result(false,"保存失败");
        }
        }
    @RequestMapping("/findOne")
    public Brand findOne(Long id)throws Exception{
        return brandService.findOne(id);
    }
    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            return new Result(false,"修改失败");
        }
    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"成功");
        } catch (Exception e) {
            return new Result(false,"失败");
        }

    }

    /**查询当前页的结果集
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     * @throws Exception
     */
    @RequestMapping("/search")
    public PageResult search(Integer pageNum,Integer pageSize,@RequestBody Brand brand )throws Exception{
        return brandService.search(pageNum,pageSize,brand);
    }

    /** 查询所有品牌结果集,返回值是list<map>
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
       return brandService.selectOptionList();
    }

}
