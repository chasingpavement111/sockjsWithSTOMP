package com.example.socket.demo.interceptor;

import java.security.Principal;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import sun.security.acl.PrincipalImpl;

/**
 * @author zhangjie
 */
public class WebSocketHandShakeInterceptor extends DefaultHandshakeHandler
{
	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes)
	{
		// 设置 sendToUser 的 user 值
		// CONNECTED 类型的消息参数（user-name: ??）
//		return super.determineUser(request, wsHandler, attributes);// 默认方法：NPE
		HttpServletRequest serverRequest = ((ServletServerHttpRequest) request).getServletRequest();
//		String sessionId = serverRequest.getRequestedSessionId();// NPE
		// 获取 simpSessionId 的方法：
		// 1、获取path路径：org.springframework.web.socket.sockjs.support.SockJsHttpRequestHandler.getSockJsPath
		// 2、截取路径的sessionId：org.springframework.web.socket.sockjs.support.AbstractSockJsService.handleRequest
		String attribute = HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;
		String sockJsPath = (String) serverRequest.getAttribute(attribute);
		String[] pathSegments = StringUtils.tokenizeToStringArray(sockJsPath.substring(1), "/");
//		String serverId = pathSegments[0];
		String simpleSessionId = pathSegments[1];//属于sockjs-session 的 id
//		String transport = pathSegments[2];
		return new PrincipalImpl(simpleSessionId);
	}
}
