package com.example.socket.demo.service.imp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.socket.demo.common.Constant;
import com.example.socket.demo.common.ErrorCodeEnum;
import com.example.socket.demo.domain.CustomSocketMessage;
import com.example.socket.demo.domain.JsonResult;
import com.example.socket.demo.service.WebSocketMessageService;

/**
 * @author zhangjie
 */
@Service
@Transactional
public class WebSocketMessageServiceImpl implements WebSocketMessageService, ApplicationListener<BrokerAvailabilityEvent>
{

	@Autowired
	@Qualifier("brokerMessagingTemplate")
	private SimpMessagingTemplate brokerMessagingTemplate;

	@KafkaListener(topics = { "TEST", "VMS-PU-SS" })
	public void onMessage(ConsumerRecord<String, String> record)
	{
		System.out.println("收到topic:" + record.topic());
		System.out.println("value:" + record.value());
//		consumer(record.topic(), record.value());
	}

	/**
	 * 接受处理消息：“system”与broker断开或重连的消息
	 *
	 * @param brokerAvailabilityEvent
	 */
	@Override
	public void onApplicationEvent(BrokerAvailabilityEvent brokerAvailabilityEvent)
	{
		System.out.println("连接处理");//todo 作用？
	}

	@Override
	public Map<CustomSocketMessage, Set<String>> getMsgForAdminOfAlarmCount(List<Map<String, Object>> alarmCountList)
	{
		// 构造发送给教育局用户的msg消息体：包含所有学校的alarmCount数据
		CustomSocketMessage msgForAdmin = new CustomSocketMessage();
		msgForAdmin.setList(alarmCountList);
		msgForAdmin.setMsgType("alarmCount");
		msgForAdmin.setIssuedTime(new Date());
		// 获取管理员用户id
		List<Long> adminIdList = Arrays.asList(1L,2L);
		// 组装消息
		Set<String> topicSet = new HashSet<>();
		JsonResult jsonResult = new JsonResult(msgForAdmin);
		adminIdList.forEach(id -> {
//			topicSet.add(Constant.WEBSOCKET_TOPIC_NAME_FOR_UPDATES + "/" + id);
			brokerMessagingTemplate.convertAndSend(Constant.WEBSOCKET_TOPIC_NAME_FOR_UPDATES + "/" + id, jsonResult);
		});
		System.out.println("service send msg");
		return Collections.singletonMap(msgForAdmin, topicSet);
	}

	@Override
	public CustomSocketMessage getAlarmCount(Long userId, Long businessId)
	{
		if (businessId == null && userId == null)
		{
			throw new RuntimeException(ErrorCodeEnum.WRONG_PARAMETER.getInfo());
		}
		// 构造返回消息
		CustomSocketMessage msg = new CustomSocketMessage();
		msg.setMsgType("alarmCount");
		msg.setIssuedTime(new Date());
		// 获取告警总数信息：todo 待修改为 MQ 方式
		List<Map<String, Object>> listOfIdAndAlarmCount = new ArrayList<>();
		listOfIdAndAlarmCount.add(Collections.singletonMap("key","value"));
		msg.setList(listOfIdAndAlarmCount);
		return msg;
	}
}
