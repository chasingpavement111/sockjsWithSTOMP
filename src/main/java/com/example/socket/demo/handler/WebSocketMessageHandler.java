package com.example.socket.demo.handler;

import java.security.Principal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.socket.demo.common.Constant;
import com.example.socket.demo.common.ErrorCodeEnum;
import com.example.socket.demo.domain.CustomSocketMessage;
import com.example.socket.demo.domain.JsonResult;
import com.example.socket.demo.service.WebSocketMessageService;


/**
 * @author zhangjie
 */
@Controller
public class WebSocketMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(WebSocketMessageHandler.class);

    /**
     * 利用socket连接，向监听指定topic的客户端发生消息
     * 转发过程：SimpleBrokerMessageHandler#handleMessageInternal
     */
    @Autowired
    @Qualifier("brokerMessagingTemplate")
    private SimpMessagingTemplate brokerMessagingTemplate;

    @Autowired
    private WebSocketMessageService webSocketMessageService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(cron = "0 0/5 * * * *")
    private void updateAndSendMessage() {
        // 测试广播
        List<Long> list = Arrays.asList(6L, 8L);
        list.forEach(id -> {
            CustomSocketMessage message = new CustomSocketMessage();
            message.setIssuedTime(new Date());
            message.setBusinessId(id);
			/*
			可通过debug模式，查看成功向多少个客户端发生了消息（若0个，不会打印）。
			日志利用如下方法打印：Broadcasting to [count] sessions.
			org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler.sendMessageToSubscribers
			 */
            brokerMessagingTemplate.convertAndSend(Constant.WEBSOCKET_TOPIC_NAME_FOR_UPDATES + "/" + id, new JsonResult(message));
        });
    }

    /**
     * 1、MessageMapping value值 = send Frame 的目的路径除去项目前缀（ApplicationDestinationPrefixes）的剩余部分
     * 保证value值在所有MessageMapping值中唯一
     * 2、SendToUser(broadcast = false) : 实现websocket 一对一对话，不会广播给所有使用该账号登陆的用户
     * broadcast=true 时，会向所有用户(所有建立了连接的用户，与登陆账号无关)发送消息
     *
     * @param message
     * @param userId
     * @return
     */
    @MessageMapping(Constant.WEBSOCKET_DESTINATION_FOR_UPDATES + "/{userId}")
    @SendToUser(value = Constant.WEBSOCKET_QUEUE_NAME_FOR_UPDATES, broadcast = false)
    public JsonResult sendAlarmCount(CustomSocketMessage message, @DestinationVariable String userId) {
        Long id;
        try {
            id = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("路径错误：用户唯一标识格式错误");
        }
        CustomSocketMessage rtnMessage = webSocketMessageService.getAlarmCount(id, null);
        rtnMessage.setMsgType(message.getMsgType());
        return new JsonResult(rtnMessage);
    }

    @MessageMapping(Constant.WEBSOCKET_DESTINATION_FOR_UPDATES)
    @SendToUser(value = Constant.WEBSOCKET_QUEUE_NAME_FOR_UPDATES, broadcast = false)
    public JsonResult sendUserAlarmCount(@Payload CustomSocketMessage message, Principal user, @Header("simpSessionId") String sessionId) {
        if (message.getMsgType().matches("[0-9]+")) {// 测试报错消息
            throw new RuntimeException("msgType 包含数字，抛异常");
        }
        logger.info("user identifier = " + user.getName() + ", sessionId = " + sessionId);
        return new JsonResult(message);
    }

    /**
     * 统一处理：来自@MessageMapping方法的异常。修饰@Controller的方法
     * 也可以使用@ControllerAdvice 修饰的异常统一处理类，进行处理
     * 不会捕获拦截器中的报错
     *
     * @param e
     * @return
     */
    @MessageExceptionHandler
    @SendToUser(destinations = Constant.WEBSOCKET_QUEUE_NAME_FOR_ERRORS, broadcast = false)
// 捕获的报错消息不广播，只发送给对应用户：broadcast = false
    public JsonResult handleException(Exception e) {
        e.printStackTrace();
        JsonResult jsonResult = new JsonResult();
        jsonResult.setResult(ErrorCodeEnum.FAIL);
        jsonResult.setResult_desc(e.getMessage());
        return jsonResult;
    }

    @RequestMapping("/test/broadcast")
    @ResponseBody
    public JsonResult broadCastTest(HttpServletRequest request) {
        updateAndSendMessage();
        return new JsonResult(null);
    }

    @RequestMapping("/test/broadcast/admin")
    @ResponseBody
    public JsonResult broadCastAdminTest(@RequestBody String requestBody, HttpServletRequest request) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("TEST", requestBody);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {

                System.out.println("成功");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.out.println("失败");
            }
        });
        webSocketMessageService.getMsgForAdminOfAlarmCount(null);
        return new JsonResult(null);
    }
}
