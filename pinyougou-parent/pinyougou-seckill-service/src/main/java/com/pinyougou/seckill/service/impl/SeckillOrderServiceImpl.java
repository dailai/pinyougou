package com.pinyougou.seckill.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id) {
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			seckillOrderMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillOrderExample example = new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();

		if (seckillOrder != null) {
			if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
			}
			if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
			}
			if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
				criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
			}
			if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
				criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
			}
			if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
			}
			if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
			}
			if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
				criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
			}

		}

		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void submitOrder(Long seckillId, String userId) {
		// 1.从缓存中提取秒杀商品
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		// 判断商品是否存在
		if (seckillGoods == null) {
			throw new RuntimeException("商品不存在");
		}
		// 判断是否有库存
		if (seckillGoods.getStockCount() <= 0) {
			throw new RuntimeException("商品已卖完");
		}
		// 判断是否开始
		if (seckillGoods.getStartTime().getTime() > new Date().getTime()) {
			throw new RuntimeException("活动未开始");
		}
		// 判断是否超过活动期
		if (seckillGoods.getEndTime().getTime() < new Date().getTime()) {
			throw new RuntimeException("活动已结束");
		}
		System.out.println("减少前库存：" + seckillGoods.getStockCount());
		// 2.减少库存

		seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);// 减少库存
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);// 保存到缓存
		// 3.在缓存保存订单
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(idWorker.nextId());
		seckillOrder.setMoney(seckillGoods.getCostPrice());
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setUserId(userId);
		seckillOrder.setStatus("0");
		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);// 存入缓存

	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		System.out.println("saveOrderFromRedisToDb:" + userId);
		// 根据用户ID查询日志
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder == null) {
			throw new RuntimeException("订单不存在");
		}
		// 如果与传递过来的订单号不符
		if (seckillOrder.getId().longValue() != orderId.longValue()) {
			throw new RuntimeException("订单不相符");
		}
		seckillOrder.setTransactionId(transactionId);// 交易流水号
		seckillOrder.setPayTime(new Date());// 支付时间
		seckillOrder.setStatus("1");// 状态
		seckillOrderMapper.insert(seckillOrder);// 保存到数据库
		redisTemplate.boundHashOps("seckillOrder").delete(userId);// 从redis中清除
	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		// 根据用户ID查询日志
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder != null && seckillOrder.getUserId().equals(userId)) {
			redisTemplate.boundHashOps("seckillOrder").delete(userId);// 删除缓存中的订单
			// 恢复库存
			// 1.从缓存中提取秒杀商品
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods")
					.get(seckillOrder.getSeckillId());
			if (seckillGoods != null) {
				seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
				redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);// 存入缓存
			}
		}
	}

}
