package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {
	
	@Value("${appid}")
	private String appid;
	
	@Value("${partner}")
	private String partner;
	
	@Value("${partnerkey}")
	private String partnerkey;
	
	@Value("${notifyurl}")
	private String notifyurl;
	

	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		Map param = new HashMap<>();
		param.put("appid", appid);//公众账号ID
		param.put("mch_id", partner);//商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		param.put("body", "品优购");//商品描述
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("total_fee", total_fee);//订单总金额
		param.put("spbill_create_ip", "127.0.0.1");//终端IP
		param.put("notify_url", notifyurl);
		param.put("trade_type", "NATIVE");

		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println("请求参数:"+xmlParam);
			util.HttpClient client=new util.HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			
			String content = client.getContent();
			System.out.println("返回参数:"+content);
			Map<String, String> paramMap = WXPayUtil.xmlToMap(content);
			Map returnMap = new HashMap<>();
			returnMap.put("code_url", paramMap.get("code_url"));
			returnMap.put("out_trade_no", out_trade_no);
			returnMap.put("total_fee", total_fee);
			
			return returnMap;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Map returnMap = new HashMap<>();
			return returnMap;
		}	
	}


	@Override
	public Map queryPayStatus(String out_trade_no) {
		Map param = new HashMap<>();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		
		try {
			String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println("查询订单支付状态请求参数:"+paramXml);
			util.HttpClient client = new util.HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			client.setHttps(true);
			client.setXmlParam(paramXml);
			client.post();
			
			String result = client.getContent();
			Map<String, String> paramMap = WXPayUtil.xmlToMap(result);
			System.out.println("查询订单状态返回参数:"+paramMap);
			return paramMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

}
