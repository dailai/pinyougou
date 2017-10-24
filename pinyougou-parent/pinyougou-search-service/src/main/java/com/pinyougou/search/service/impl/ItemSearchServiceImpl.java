package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public Map<String, Object> search(Map searchMap) {

		Map map = new HashMap<>();
		if(!"".equals(searchMap.get("keywords").toString())&&searchMap.get("keywords").toString()!=null){
			// 1.关键字查询
			Query query = new SimpleQuery();
			Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
				// 1.1添加关键字条件
				query.addCriteria(criteria);
				// 1.2商品分类筛选
				String category0 = (String) searchMap.get("category");
				if (category0 != null && !"".equals(category0)) {// 当前端传递过来条件
					Criteria filterCriteria = new Criteria("item_category").is(category0);
					FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
					query.addFilterQuery(filterQuery);
				}
		
				// 1.3品牌筛选
				String brand = (String) searchMap.get("brand");
				if (brand != null && !"".equals(brand)) {// 当前端传递过来条件
					Criteria filterCriteria = new Criteria("item_brand").is(brand);
					FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
					query.addFilterQuery(filterQuery);
				}
		
				// 1.4规格筛选
				Map<String, String> specMap = (Map) searchMap.get("spec");
				if (specMap != null) {
					for (String key : specMap.keySet()) {
						Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
						FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
						query.addFilterQuery(filterQuery);
					}
				}
				
				//1.5按价格过滤			
				String price_string= (String) searchMap.get("price");
				if(price_string!=null &&  !"".equals(price_string)  ){
					String[] price = price_string.split("-");
					//区间开始价格
					if(!"0".equals(price[0])){
						Criteria filterCriteria=new Criteria("item_price" ).greaterThanEqual(price[0]);
						FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
						query.addFilterQuery(filterQuery);				
					}
					//区间截止价格
					if(!"0".equals(price[1])){
						Criteria filterCriteria=new Criteria("item_price" ).lessThanEqual(price[1]);
						FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
						query.addFilterQuery(filterQuery);				
					}		
				}
				
				//1.6 分页
				Integer pageNo = (Integer)searchMap.get("pageNo");//提取页码
				Integer pageSize = (Integer)searchMap.get("pageSize");//每页记录数
				if(pageNo==null){
					pageNo=1;//默认第一页
				}
				if(pageSize==null){
					pageSize=10;
				}
				query.setOffset((pageNo-1)*pageSize);
				query.setRows(pageSize);
				
				
				//1.7 排序
				String sortValue = (String) searchMap.get("sort");
				if(sortValue!=null){
					Sort sort = null;
					if ("ASC".equals(sortValue)) {
						sort = new Sort(Sort.Direction.ASC,"item_price");
					} else {
						sort = new Sort(Sort.Direction.DESC,"item_price");
					}
					if(sort!=null)
						query.addSort(sort);
				}
				
				ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
				map.put("rows", page.getContent());
				map.put("currentPage", pageNo);
				map.put("totalPages", page.getTotalPages());//返回总页数
				map.put("total", page.getTotalElements());//返回总记录数
		
		// 2.根据商品分类分组查询
		Query query2=new SimpleQuery();	
		Criteria criteria2=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query2.addCriteria(criteria2);
		GroupOptions options = new GroupOptions().addGroupByField("item_category");
		query2.setGroupOptions(options);
		GroupPage<TbItem> queryForGroupPage = solrTemplate.queryForGroupPage(query2, TbItem.class);
		GroupResult<TbItem> groupResult = queryForGroupPage.getGroupResult("item_category");
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		List<String> categoryList = new ArrayList<>();
		for (GroupEntry<TbItem> entry : content) {
			categoryList.add(entry.getGroupValue());
		}
		
		map.put("categoryList", categoryList);

		// 3.在缓存中根据商品的分类名称查询模板id
		if (categoryList.size() > 0) {
			String category = categoryList.get(0);
			Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
			if (typeId != null) {
				List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
				map.put("brandList", brandList);
				List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
				map.put("specList", specList);
			}
		}
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByIds(List goodsIdList) {
		System.out.println("删除商品ID" + goodsIdList);
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

}
