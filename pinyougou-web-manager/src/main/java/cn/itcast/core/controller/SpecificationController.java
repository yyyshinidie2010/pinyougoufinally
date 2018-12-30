package cn.itcast.core.controller;

import cn.itcast.core.pojo.template.TypeTemplate;
import entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {
    @Reference
    private SpecificationService specificationService;
    //查询分页有条件
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows,@RequestBody Specification specification) throws Exception {

       return specificationService.search(page, rows,specification);
    }

    /**添加规格
     * @param specificationVo
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody SpecificationVo specificationVo){

        try {
             specificationService.add(specificationVo);
            return new Result(true,"成功");
        } catch (Exception e) {
            return new Result(false,"失败");
        }
    }
    @RequestMapping("/findOne")
    public SpecificationVo findOne(Long id) throws Exception {
        return specificationService.findOne(id);
    }
    @RequestMapping("/update")
    public Result update(@RequestBody SpecificationVo specificationVo){

        try {
            specificationService.update(specificationVo);
            return new Result(true,"成功");
        } catch (Exception e) {
            return new Result(false,"失败");
        }
    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){

        try {
            specificationService.delete(ids);
            return new Result(true,"成功");
        } catch (Exception e) {
            return new Result(false,"失败");
        }
    }
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
       return specificationService.selectOptionList();
    }


}
