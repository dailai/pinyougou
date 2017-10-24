package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;

/**
 * 支付控制层
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private WeixinPayService weixinPayService;

	@Reference
	private SeckillOrderService seckillOrderService;
	
	@Autowired
	private HttpServletResponse response;
	
	/**
	 * 生成二维码
	 * 
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative() {
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		// 获取当前用户
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		// 到redis查询秒杀订单
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
		// 判断秒杀订单存在
		if (seckillOrder != null) {
			return weixinPayService.createNative(seckillOrder.getId() + "", seckillOrder.getMoney().longValue() + "");
		} else {
			return new HashMap();
		}
	}

	/**
	 * 查询支付状态
	 * 
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		// 获取当前用户
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		Result result = null;
		int x = 0;
		while (true) {
			// 调用查询接口
			Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
			if (map == null) {// 出错
				result = new Result(false, "支付出错");
				break;
			}
			if (map.get("trade_state").equals("SUCCESS")) {// 如果成功
				result = new Result(true, "支付成功");
				// 修改订单状态
				// orderService.updateOrderStatus(out_trade_no,
				// map.get("transaction_id"));
				seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no),
						map.get("transaction_id"));
				break;
			}
			try {
				Thread.sleep(3000);// 间隔三秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 为了不让循环无休止地运行，我们定义一个循环变量，如果这个变量超过了这个值则退出循环，设置时间为5分钟
			x++;
			if (x > 100) {
				result = new Result(false, "二维码超时");
				seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
				System.out.println("超时，取消订单");
				// 调用微信的关闭订单接口（学员实现）

				break;
			}
		}
		return result;
	}

}
