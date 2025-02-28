package com.controller;


import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.StringUtil;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.YingpianOrderEntity;

import com.service.YingpianOrderService;
import com.entity.view.YingpianOrderView;
import com.service.YingpianService;
import com.entity.YingpianEntity;
import com.service.YonghuService;
import com.entity.YonghuEntity;
import com.utils.PageUtils;
import com.utils.R;

/**
 * 影片订单
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/yingpianOrder")
public class YingpianOrderController {
    private static final Logger logger = LoggerFactory.getLogger(YingpianOrderController.class);

    @Autowired
    private YingpianOrderService yingpianOrderService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;



    //级联表service
    @Autowired
    private YingpianService yingpianService;
    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        params.put("orderBy","id");
        PageUtils page = yingpianOrderService.queryPage(params);

        //字典表数据转换
        List<YingpianOrderView> list =(List<YingpianOrderView>)page.getList();
        for(YingpianOrderView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        YingpianOrderEntity yingpianOrder = yingpianOrderService.selectById(id);
        if(yingpianOrder !=null){
            //entity转view
            YingpianOrderView view = new YingpianOrderView();
            BeanUtils.copyProperties( yingpianOrder , view );//把实体数据重构到view中

            //级联表
            YingpianEntity yingpian = yingpianService.selectById(yingpianOrder.getYingpianId());
            if(yingpian != null){
                BeanUtils.copyProperties( yingpian , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYingpianId(yingpian.getId());
            }
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(yingpianOrder.getYonghuId());
            if(yonghu != null){
                BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody YingpianOrderEntity yingpianOrder, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,yingpianOrder:{}",this.getClass().getName(),yingpianOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            yingpianOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        yingpianOrder.setInsertTime(new Date());
        yingpianOrder.setCreateTime(new Date());
        yingpianOrderService.insert(yingpianOrder);
        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody YingpianOrderEntity yingpianOrder, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,yingpianOrder:{}",this.getClass().getName(),yingpianOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            yingpianOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        yingpianOrderService.updateById(yingpianOrder);//根据id更新
        return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        yingpianOrderService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }



    /**
    * 前端列表
    */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = yingpianOrderService.queryPage(params);

        //字典表数据转换
        List<YingpianOrderView> list =(List<YingpianOrderView>)page.getList();
        for(YingpianOrderView c:list)
            dictionaryService.dictionaryConvert(c); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        YingpianOrderEntity yingpianOrder = yingpianOrderService.selectById(id);
            if(yingpianOrder !=null){
                //entity转view
                YingpianOrderView view = new YingpianOrderView();
                BeanUtils.copyProperties( yingpianOrder , view );//把实体数据重构到view中

                //级联表
                    YingpianEntity yingpian = yingpianService.selectById(yingpianOrder.getYingpianId());
                if(yingpian != null){
                    BeanUtils.copyProperties( yingpian , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYingpianId(yingpian.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(yingpianOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody YingpianOrderEntity yingpianOrder, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,yingpianOrder:{}",this.getClass().getName(),yingpianOrder.toString());
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if("用户".equals(role)){
            YingpianEntity yingpianEntity = yingpianService.selectById(yingpianOrder.getYingpianId());
            if(yingpianEntity == null){
                return R.error(511,"查不到该影片");
            }
            Double yingpianNewMoney = yingpianEntity.getYingpianNewMoney();
            if(yingpianNewMoney == null){
                return R.error(511,"影片票价不能为空");
            }

            Integer userId = (Integer) request.getSession().getAttribute("userId");
            YonghuEntity yonghuEntity = yonghuService.selectById(userId);
            if(yonghuEntity == null)
                return R.error(511,"用户不能为空");
            if(yonghuEntity.getNewMoney() == null)
                return R.error(511,"用户金额不能为空");
            double balance = yonghuEntity.getNewMoney() - yingpianEntity.getYingpianNewMoney();//余额
            if(balance<0)
                return R.error(511,"余额不够支付");
            yingpianOrder.setCreateTime(new Date());
            yingpianOrder.setInsertTime(new Date());
            yingpianOrderService.insert(yingpianOrder);//根据id更新
            yonghuEntity.setNewMoney(balance);
            yonghuService.updateById(yonghuEntity);
            return R.ok();
        }else{
            return R.error(511,"您没有权限支付订单");
        }
    }





}

