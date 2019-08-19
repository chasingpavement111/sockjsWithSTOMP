package com.arges.web.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.example.socket.demo.interceptor.WebSocketHandShakeInterceptor;
import com.example.socket.demo.interceptor.WebSocketInboundSecurityInterceptor;

/**
 * 注意当使用web.xml+_servlet.xml 配置项目时，需要保证配置类不会被扫描两次（实例化多次），
 * 否则会出现org.springframework.messaging.simp.SimpMessagingTemplate 实例有多个，导致与@SendTo、@SendToUser注解的session缓存不在同一个实例中无法等效替换的问题。
 * @author zhangjie
 */
@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private TaskScheduler taskScheduler;

    /**
     * 使用规范路径：（使用simple broker 没有实际意义，但是使用Stomp Broker时需要遵守规范）
     * app : 代表所有需要进入 MessageMapping方法的消息
     * topic : 代表用于一对多广播的消息
     * queue : 代表用于一对一会话的消息
     * secured : 需要身份认证的消息，需要带Authorization请求头信息
     * v1 : 版本v1使用的会话消息类型
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // StompBroker，适合分布式系统：支持更多的STOMP命令和特性（如acks, receipts...）
        registry.enableSimpleBroker("/topic", "/queue")
                /*
                项目启动时开启：org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler.start 中的方法（org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler.startInternal 实现方法）开启"发送心跳"的定时任务（定义定时任务的执行机制）
                simpleBroker 设置心跳机制：org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler.startInternal
                    若需要发送心跳（sx>0 && sy>0），则必须设置TaskScheduler；若设置了TaskScheduler，则只需{sx,sy}至少有一个值大于0即可开启心跳定时发送任务
                1、org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler.initHeartbeatTaskDelay：定义定时任务的间隔时间 = min(sx,sy)
                    {sx, sy}:当两者都大于0时，服务端会向客户端每 min(sx,sy)毫秒发送一次心跳包。希望每 sy毫秒收到客户端发送的心跳包。两者都为 0时，服务端不会发送心跳，默认不发送。
                2、org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler.HeartbeatTask.run：定时任务的具体执行内容，message=\n
                    若sx=0，则服务端不会向socket client发送心跳消息 —— 不可以
                    若sy=0，则客户端即使断联，服务端也会不断向他发送心跳消息 —— 不推荐（设为0，可以使用EndPoint进行统一的心跳检测。但是增加了负担，不推荐）
                    info.getLastWriteTime：服务端发送一次 MESSAGE消息后成功后设置，发送 HEARTBEAT心跳消息不更新
                    info.getLastReadTime：每次接受到客户端消息，就会更新一次
                 */
                .setHeartbeatValue(new long[] { 5_000L, 5_000L })  // 服务端每5s向建立连接的客户端发送一次心跳，客户端发生心跳的间隔不可大于10min=10*60*1000
                .setTaskScheduler(taskScheduler);//todo 计算用户量，避免资源吃不消。每次用户都发一遍（在一个定时任务中）
        registry.setApplicationDestinationPrefixes("/app/secured");
//		registry.setUserDestinationPrefix("/user/");// @SendToUser的默认前缀
//		registry.setPreservePublishOrder(true);// 保证消息顺序：与发布顺序一致（顺序性会有一定的性能开销。因此默认使用多线程发生消息，不保证消息接受顺序）
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry)
    {
        // 不需要写入endpoint的路径 ：contextPath, web.xml 的servlet-mapping 中要求的 url-pattern(本项目为api)
//		registry.addEndpoint("/socket");
        registry.addEndpoint("/socket")
                .setHandshakeHandler(handshakeInterceptor())
                .withSockJS()
                // 缓存容量限制：1M
//				.setStreamBytesLimit(1024)
                /*
                org.springframework.web.socket.messaging.StompSubProtocolHandler.afterStompSessionConnected：定义客户端建立socket连接后的行为（补充返回header, 设置心跳方式）
                    只有当SimpleBroker的sy==0时，才使用本心跳设置进行心跳检测。
                定时发送 HEARTBEAT消息，每HeartBeat时间向客户端发送心跳消息，message=h
                org.springframework.web.socket.sockjs.transport.session.AbstractSockJsSession.HeartbeatTask.run：定时心跳行为（每次发送一条心跳消息，并将下一次心跳任务加入到定时器中）
                 */
//				.setHeartbeatTime(5_000L)
//				.setTaskScheduler(taskScheduler)
                //设置sockjs 下载路径，最好与客户端使用的资源一致
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
    }

    /**
     * 用于传递从WebSocket客户端收到的消息
     *
     * @param registry
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registry)
    {
        registry.interceptors(inboundChannelInterceptor());
    }

    /**
     * 用于将服务器消息发送到WebSocket客户端
     *
     * @return
     */
//	@Override
//	public void configureClientOutboundChannel(ChannelRegistration registry)
//	{
//		registry.setInterceptors(outboundChannelInterceptor());
//	}

    /**
     * 日志记录和异常处理
     *
     * @return
     */
    //	@Override
//	public void configureWebSocketTransport(WebSocketTransportRegistration registration)
//	{
//		registration.addDecoratorFactory(CustomWebSocketHandlerDecorator::new);
//	}
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 99)// 需要确保身份验证拦截器在Spring Security之前配置。
    public ChannelInterceptor inboundChannelInterceptor()
    {
        return new WebSocketInboundSecurityInterceptor();
    }

    @Bean
    public DefaultHandshakeHandler handshakeInterceptor()
    {
        return new WebSocketHandShakeInterceptor();
    }
}