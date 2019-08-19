package com.example.socket.demo.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 拦截器：用于传递从WebSocket客户端收到的消息
 * 对象：通过websocket连接发出的请求
 * 功能：身份认证（token校验）、维护用户与session关系
 * AbstractSecurityWebSocketMessageBrokerConfigurer.configureInbound ：Spring Security 对websocket的身份认证进行细粒度的划分
 *
 * @author zhangjie
 */
public class WebSocketInboundSecurityInterceptor extends ChannelInterceptorAdapter
{
	/**
	 * 只序列化非空值传递给前端
	 */
	private final ObjectMapper objectMapper;

	/**
	 * 发送信息给前端用户
	 */
	@Autowired
	@Qualifier("clientOutboundChannel")
	private MessageChannel clientOutboundChannel;

	public WebSocketInboundSecurityInterceptor()
	{
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel)
	{
		// 若preSend抛出异常，不会进入同一个拦截器的afterSendCompletion方法中
		StompHeaderAccessor headerAccessor =
				MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		SimpMessageType simpType = (SimpMessageType) headerAccessor.getMessageHeaders().get("simpMessageType");
		if (SimpMessageType.HEARTBEAT.compareTo(simpType) == 0)
		{
			return message;
		}
		switch (headerAccessor.getCommand())
		{
			case CONNECT:// 建立连接的消息
			case SEND:// 沟通信息
				// 对消息进行身份认证
				try
				{
					//todo ...
				}
				catch (Exception e)
				{
					e.printStackTrace();
					sendErrorMessage(headerAccessor, e);
				}
				break;
			default:
				break;
		}
		return message;
	}

	@Override
	public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex)
	{

		super.afterSendCompletion(message, channel, sent, ex);
	}

	/**
	 * 身份认证不通过的消息不允许接受
	 * 返回Error 信息，可以自动断开原本建立起来的websocket连接，原因：org.springframework.web.socket.messaging.StompSubProtocolHandler#sendToClient(org.springframework.web.socket.WebSocketSession, org.springframework.messaging.simp.stomp.StompHeaderAccessor, byte[])的 finally部分
	 *
	 * @param requestedMessage
	 * @param e
	 */
	private void sendErrorMessage(StompHeaderAccessor requestedMessage, Exception e)
	{
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
		// 添加错误原因
		String causeMessage = e.getMessage();
		headerAccessor.setMessage(causeMessage);
		// 添加标识信息给 Error信息，便于用户对应引起错误的frame（与请求信息中含有frame标识信息一致）
		headerAccessor.setUser(requestedMessage.getUser());
		headerAccessor.setSessionId(requestedMessage.getSessionId());
		headerAccessor.setMessageId(requestedMessage.getMessageId());
		headerAccessor.setReceiptId(requestedMessage.getReceiptId());
		headerAccessor.setSubscriptionId(requestedMessage.getSubscriptionId());
		// 序列化body：StompSubProtocolHandler.handleMessageToClient 要求payload必须为 byte数组，否则会忽略本次send消息
		byte[] bytePayLoad = new byte[0];
//		JsonResult jsonResult = new JsonResult();
//		jsonResult.setResult(ErrorCodeEnum.UNAUTHORIZED);
//		if (e instanceof BusinessException)
//		{
//			jsonResult.setResult_desc(causeMessage);
//		}
//		try
//		{
//			bytePayLoad = objectMapper.writeValueAsBytes(jsonResult);
//		}
//		catch (IOException ex)
//		{
//			ex.printStackTrace();
//			bytePayLoad = new byte[0];
//		}
		clientOutboundChannel.send(MessageBuilder.createMessage(bytePayLoad, headerAccessor.getMessageHeaders()));
	}
}
