package com.example.socket.demo.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.socket.demo.domain.CustomSocketMessage;

/**
 * @author zhangjie
 */
public interface WebSocketMessageService
{
	Map<CustomSocketMessage, Set<String>> getMsgForAdminOfAlarmCount(List<Map<String, Object>> alarmCountList);

	CustomSocketMessage getAlarmCount(Long userId, Long businessId);
}
