package com.example.socket.demo.interceptor;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * 日志记录和异常处理
 *
 * @author zhangjie
 */
public class WebSocketCustomHandlerDecorator extends WebSocketHandlerDecorator
{
	@Autowired
	private RedissonClient redisson;

	public WebSocketCustomHandlerDecorator(WebSocketHandler wsHandler)
	{
		super(wsHandler);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception
	{
		String userName = session.getPrincipal().getName();
		RMap<Object, Object> map = redisson.getMap(userName);
		System.out.println("establish："+map);
		super.afterConnectionEstablished(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception
	{
		System.out.println("close");
		super.afterConnectionClosed(session, closeStatus);
	}
}
