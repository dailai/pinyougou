package com.pinyougou.itempage.service;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyMessageListener implements MessageListener {

	@Autowired
	private MyJob myJob;
	
	
	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage)message;
		try {
			long goodsId = Long.parseLong(textMessage.getText());
			System.out.println("接收到的商品ID:"+goodsId);
			myJob.genItem(goodsId);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
