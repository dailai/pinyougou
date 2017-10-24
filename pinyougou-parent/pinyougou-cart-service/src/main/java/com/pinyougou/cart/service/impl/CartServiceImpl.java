package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		
		//1.根据SKU ID查询SKU商品信息
		TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
		if(tbItem==null){
			throw new RuntimeException("该商品不存在");
		}
		if(!tbItem.getStatus().equals("1")){
			throw new RuntimeException("该商品状态无效");
		}
		//2.查询商家ID
		String sellerId = tbItem.getSellerId();
		
		//3.根据商家ID查询购物车列表中是否存在该商家的购物车对象
		Cart cart=searchCart(cartList, sellerId);
		
		if(cart!=null){//4.如果列表中存在该商家的购物车对象
			//4.1 查询购物车明细列表中是否存在该商品
			TbOrderItem tbOrderItem = searchOrderItem(cart.getOrderItemList(),itemId);
			if (tbOrderItem!=null) {//4.2 如果存在，则更改数量和金额
				tbOrderItem.setNum(tbOrderItem.getNum()+num);
				tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getPrice().doubleValue()*tbOrderItem.getNum()));
				if(tbOrderItem.getNum()<=0)
					cart.getOrderItemList().remove(tbOrderItem);
				if(cart.getOrderItemList().size()<=0)
					cartList.remove(cart);
			} else {//4.3 如果不存在，则增加商品
				tbOrderItem=createOrderItem(tbItem,num);
				cart.getOrderItemList().add(tbOrderItem);
			}
					
		}else{//5.如果列表中不存在该商家的购物车对象
			//5.1 创建购物车对象
			cart=new Cart();
			cart.setSellerName(tbItem.getSeller());
			cart.setSellerId(sellerId);
			TbOrderItem orderItem = createOrderItem(tbItem, num);
			List orderList = new ArrayList<>();
			orderList.add(orderItem);
			cart.setOrderItemList(orderList);
			//5.2 将新建的购物车对象加入购物车列表
			cartList.add(cart);
		}
		
		return cartList;
	}

	/**
	 * 查询购物车列表中是否有该商家的购物车对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCart(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if(cart.getSellerId().equals(sellerId)){
				return cart;
			}
		}
		return null;
	}
	
	/**
	 * 查询购物车明细列表中是否存在该商品
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItem(List<TbOrderItem> orderItemList, Long itemId) {
		for (TbOrderItem tbOrderItem : orderItemList) {
			if(tbOrderItem.getItemId().equals(itemId)){
				return tbOrderItem;
			}
		}
		return null;
	}
	
	/**
	 * 在该商家的购物车明细列表中创建新商品
	 * @param tbItem
	 * @param num
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem tbItem, Integer num) {
		
		if(num<0){
			throw new RuntimeException("非法数量");
		}
		
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(tbItem.getGoodsId());
		orderItem.setItemId(tbItem.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(tbItem.getImage());
		orderItem.setPrice(tbItem.getPrice());
		orderItem.setSellerId(tbItem.getSellerId());
		orderItem.setTitle(tbItem.getTitle());
		orderItem.setTotalFee(new BigDecimal(tbItem.getPrice().doubleValue()*num));
		return orderItem;
	}

	@Override
	public List<Cart> findCartListFromRedis(String username) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if(cartList==null){
			cartList= new ArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		System.out.println("合并购物车");
		List<Cart> cartList = new ArrayList<>();
		for (Cart cart : cartList1) {
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				cartList=addGoodsToCartList(cartList, orderItem.getItemId(), orderItem.getNum());
			}
		}
		for (Cart cart : cartList2) {
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				cartList=addGoodsToCartList(cartList, orderItem.getItemId(), orderItem.getNum());
			}
		}
		
		return cartList;
	}

}
