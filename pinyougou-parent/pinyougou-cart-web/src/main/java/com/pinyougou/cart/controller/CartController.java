package com.pinyougou.cart.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Reference(timeout=6000)
	private CartService cartService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;

	/**
	 * 购物车列表
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登陆用户:"+userName);
		String cookieValue = CookieUtil.getCookieValue(request, "cartList", true);
		if(cookieValue==null||cookieValue==""){
			cookieValue="[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cookieValue, Cart.class);
		
		if(userName.equals("anonymousUser")){

			System.out.println("从cookie中获取购物车列表");
			return cartList_cookie;
		}else{
			List<Cart> cartList_redis = cartService.findCartListFromRedis(userName);
			System.out.println("从redis中获取购物车列表");
			if(cartList_cookie.size()>0){
				//1.合并购物车
				cartList_redis = cartService.mergeCartList(cartList_cookie, cartList_redis);
				//2.删除cookie
				CookieUtil.deleteCookie(request, response, "cartList");
				//3.存入redis
				cartService.saveCartListToRedis(userName, cartList_redis);
			}
			return cartList_redis;
		}

	}
	
	@CrossOrigin(origins="http://localhost:9109",allowCredentials="true")
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num){
		
//		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9109");
//		response.setHeader("Access-Control-Allow-Credentials", "true");

		
		
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		
			try {
				List<Cart> cartList = findCartList();		
				cartList = cartService.addGoodsToCartList(cartList, itemId, num);
				if (userName.equals("anonymousUser")) {
					CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), true);	
					System.out.println("购物车数据存入cookie");
				}else{
					cartService.saveCartListToRedis(userName, cartList);
					System.out.println("购物车数据存入redis");
				}
					
				return new Result(true,"添加到购物车成功");
			}catch (RuntimeException e){
				e.printStackTrace();
				return new Result(false,e.getMessage());
			} 
			catch (Exception e) {
				e.printStackTrace();
				return new Result(false,"添加到购物车失败");
			}
	} 
	
	
	
}
	


