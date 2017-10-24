package com.pinyougou.manager.controller;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;

import freemarker.template.Configuration;
import freemarker.template.Template;

@RestController
public class HtmlController {
	
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	
	@Reference
	private GoodsService goodsService;
	
	@Reference
	private ItemCatService itemCatService;
	
	@RequestMapping("/gen_item")
	public void gen_item(Long goods_id) throws Exception{
		
		/**
		 *1.通过freeMarkerConfig得到配置对象
		 *2.获取模板
		 *3.构建数据对象
		 *4.构建输出对象
		 *5.输出
		 *6.关闭流
		 */
		Configuration configuration = freeMarkerConfig.getConfiguration();
		Template template = configuration.getTemplate("item.ftl");
//		Map map = new HashMap<>();
//		map.put("goodsName", "苹果测试商品");
		
		Goods goods = goodsService.findOne(goods_id);
		
		String itemCat1 = itemCatService.findOne(goods.getGoods().getCategory1Id()).getName();
		String itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id()).getName();
		String itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id()).getName();
		
		Map map = new HashMap<>();
		map.put("itemCat1", itemCat1);
		map.put("itemCat2", itemCat2);
		map.put("itemCat3", itemCat3);
		
		goods.setMap(map);
		
		FileWriter fileWriter = new FileWriter("C:\\item\\"+goods_id+".html");
		template.process(goods, fileWriter);
		fileWriter.close();
		
	}

}
