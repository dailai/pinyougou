package com.pinyougou.itempage.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.pinyougou.pojogroup.Goods;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

@Component
public class MyJob {
	
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	@Autowired
	private GoodsService goodsService;
	@Value("${target_dir}")
	private String targetDir;
	
	public void genItem(Long goodsId) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException{
		//1.得到配置对象
		Configuration configuration = freeMarkerConfig.getConfiguration();
		//2.获取模板
		Template template = configuration.getTemplate("item.ftl");
		//3.构建数据对象
		Goods goods = goodsService.findOne(goodsId);
		//4.创建输出流对象
		Writer out = new FileWriter(targetDir+goodsId+".html");
		//5.生成网页
		template.process(goods, out);
		//6.关闭流
		out.close();
	}
	
}
