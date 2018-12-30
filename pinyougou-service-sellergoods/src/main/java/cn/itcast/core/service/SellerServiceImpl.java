package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {
    @Autowired
    private SellerDao sellerDao;


    /**分页查询之条件查询
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        //分页插件
        PageHelper.startPage(page,rows);
        SellerQuery sellerQuery =new SellerQuery();
        SellerQuery.Criteria criteria = sellerQuery.createCriteria();
        //条件查询
        if (seller.getName() != null && !seller.getName().trim().equals("")){
            criteria.andNameLike("%"+seller.getName().trim()+"%");
        }
        if (seller.getNickName() != null && !seller.getNickName().trim().equals("")){
            criteria.andNickNameLike("%"+seller.getNickName().trim()+"%");
        }if (seller.getStatus() != null && !"9".equals(seller.getStatus())){
            criteria.andStatusEqualTo(seller.getStatus());
        }

        Page<Seller> p = (Page<Seller>) sellerDao.selectByExample(sellerQuery);

        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public Seller findOne(String sellerId) {
        return sellerDao.selectByPrimaryKey(sellerId);
    }

    @Override
    public void update(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }

    /**商家注册登录:
     * @param seller
     */
    @Override
    public void add(Seller seller) {
        String password = seller.getPassword();
        //使用bcry加密算法(包含MD5等不可逆的加密,和加盐随机添加字符串方式)
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(password);
        seller.setPassword(encode);
        seller.setStatus("0");
        sellerDao.insertSelective(seller);
    }
}
