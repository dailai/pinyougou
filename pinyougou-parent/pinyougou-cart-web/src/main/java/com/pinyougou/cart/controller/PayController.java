package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private OrderService orderService;

	@Reference
	private WeixinPayService winXinPayService;

	@Autowired
	private HttpServletResponse response;

	@RequestMapping("/createNative")
	public Map createNative() {
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);

		// 获取当前用户
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		// 到redis查询支付日志
		TbPayLog payLog = orderService.searchPayLogFromRedis(userId);

		if (payLog != null) {
			//return winXinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
			return winXinPayService.createNative(payLog.getOutTradeNo(), "1");
		} else {
			return new HashMap();
		}
	}

	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		int x = 0;
		Result result = null;
		while (true) {

			Map map = winXinPayService.queryPayStatus(out_trade_no);
			if (map == null) {
				result = new Result(false, "支付失败");
				break;
			}
			if ("SUCCESS".equals(map.get("trade_state"))) {
				result = new Result(true, "支付成功");
				//修改订单状态
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id")+"");
				break;
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 为了不让循环无休止地运行，我们定义一个循环变量，如果这个变量超过了这个值则退出循环，设置时间为5分钟
			x++;
			if (x >= 50) {
				result = new Result(false, "二维码超时");
				break;
			}
			System.out.println("查询订单状态");
		}

		return result;
	}

}
