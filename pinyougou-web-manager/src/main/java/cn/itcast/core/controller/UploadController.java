package cn.itcast.core.controller;

import cn.itcast.common.utils.FastDFSClient;
import entity.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    //获取配置文件的属性
    @Value("${FILE_SERVER_URL}")
    private String url;

    /**商品图片上传
     * @return
     */
    //入参是form表单的图片
    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file){
        try {
            //file.getOriginalFilename(); 获取原始名,可以得到文件的后缀
            //上传文件到分布式文件系统FastDFS:
            //参数我们要添加一个配置信息:
            FastDFSClient fastDFSClient=new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            //获取扩展名:org.apache.commons.io.FilenameUtils;shiyogn apache 的获取流的方法
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            //参数:二进制文件,后缀名 和 图片描述
            //上传图片,返回一个路径
            String path = fastDFSClient.uploadFile(file.getBytes(), ext);
            //返回的参数路径需要加上IP地址,我们哦通过配置文件获取
            return new Result(true,url+path);
        } catch (Exception e) {
            //e.printStackTrace();
            return new Result(false,"文件上传失败");
        }
    }

}

