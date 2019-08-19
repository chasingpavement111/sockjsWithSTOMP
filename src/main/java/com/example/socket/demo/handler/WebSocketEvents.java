package com.example.socket.demo.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
public class WebSocketEvents {

    private final String localName = "localhost_1";

    @Autowired
    private RedissonClient redisson;

    @EventListener
    private void handleSessionDisconnect(BrokerAvailabilityEvent event) {
        if (event.isBrokerAvailable()) {
            System.out.println("broker available");
        } else {
            System.out.println("broker unavailable");
        }
    }

    @EventListener
    private void handleSessionConnected(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        // 连接到本机的用户账号id -> topic信息：Map{服务器，Map{topic，List<{sessionId,subId}>}}
        // Map{topic，List<{sessionId,subId}>}
//		RMap<String, List<Map<String,String>>> destinationMap = redisson.getMap(localName);
//		// Map{sessionId,subId}
//		String destination = headers.getDestination();
//		RLock lock = destinationMap.getLock(destination);
//		lock.lock();
//		destinationMap.putIfAbsent(destination,new ArrayList<>());
//		lock.unlock();
        System.out.println("session=" + headers.getSessionId());
        System.out.println("sub=" + headers.getSubscriptionId());
        System.out.println("connected");
    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        removeSession(headers.getSessionId());
        System.out.println("disconnect");
    }

    @EventListener
    private void handleSessionSubscribe(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        addSubscription(headers.getDestination(), headers.getSessionId(), headers.getSubscriptionId());
        System.out.println("subscribed");
    }

    @EventListener
    private void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        removeSubscriprion(headers.getDestination(), headers.getSessionId(), headers.getSubscriptionId());
        System.out.println("unsubscribed");
    }

    /**
     * 移除该session监听的所有topic信息
     *
     * @param disconnetSessionId
     */
    private void removeSession(String disconnetSessionId) {
        // redisson：Map{服务器，Map{topic，List<{sessionId,subId}>}}
        // Map{topic，List<{sessionId,subId}>}
        RMap<String, List<Map<String, String>>> destinationMap = redisson.getMap(localName);
        RLock lock = destinationMap.getLock(disconnetSessionId);
        lock.lock();
        // 遍历所有destination，删除断开连接的session信息
        Iterator<Map.Entry<String, List<Map<String, String>>>> destinationIterator = destinationMap.entrySet().iterator();
        while (destinationIterator.hasNext()) {
            Map.Entry<String, List<Map<String, String>>> destination = destinationIterator.next();
            Set<String> destinationsToRemove = new HashSet<>();
            // 遍历某个destination中的所有session
            Iterator<Map<String, String>> sessionIterator = destination.getValue().iterator();
            while (sessionIterator.hasNext()) {
                Map<String, String> session = sessionIterator.next();// map.size()==1
                Iterator<Map.Entry<String, String>> sessionToSub = session.entrySet().iterator();
                while (sessionToSub.hasNext()) {
                    Map.Entry<String, String> entry = sessionToSub.next();
                    // 删除断联 session
                    if (entry.getKey().compareTo(disconnetSessionId) == 0) {
                        sessionToSub.remove();
                    }
                }
                // 删除断联 session
                if (session.size() == 0) {
                    sessionIterator.remove();
                }
            }
            // 若本destination中已经不存在session连接，则删除destination
            if (destination.getValue().size() == 0) {
                destinationIterator.remove();
            }
        }
        lock.unlock();
    }

    /**
     * 存储订阅的topic信息
     * 一个地址可以同时监听多个topic，一个地址可以对应多个sessionId，一个session可以同时监听多个topic
     * 所以需要三个条件确定一个websocket client
     *
     * @param destination:topic     的路径地址
     * @param sessionId:监听          topic的SockJs.sessionId
     * @param subscriptionId：topic的 subscriptionId
     */
    private void addSubscription(String destination, String sessionId, String subscriptionId) {
        // Map{topic，List<{sessionId,subId}>}
        RMap<String, List<Map<String, String>>> destinationMap = redisson.getMap(localName);
        RLock lock = destinationMap.getLock(subscriptionId);
        lock.lock();
        destinationMap.computeIfAbsent(destination, (unused) -> new ArrayList<>()).add(Collections.singletonMap(sessionId, subscriptionId));// Map{sessionId,subId}
        lock.unlock();
    }

    /**
     * 删除订阅的topic信息
     *
     * @param destination
     * @param sessionId
     * @param unSubscriptionId
     */
    private void removeSubscriprion(String destination, String sessionId, String unSubscriptionId) {
        // Map{topic，List<{sessionId,subId}>}
        RMap<String, List<Map<String, String>>> destinationMap = redisson.getMap(localName);
        if (destinationMap.get(destination) == null) {
            return;
        }
        RLock lock = destinationMap.getLock(unSubscriptionId);
        lock.lock();
        Iterator<Map<String, String>> sessionIterator = destinationMap.get(destination).iterator();
        while (sessionIterator.hasNext()) {
            Map<String, String> session = sessionIterator.next();
            Iterator<Map.Entry<String, String>> sessionToSub = session.entrySet().iterator();
            while (sessionToSub.hasNext()) {
                Map.Entry<String, String> entry = sessionToSub.next();
                // 删除取消订阅的topic
                if (entry.getValue().compareTo(unSubscriptionId) == 0) {
                    sessionToSub.remove();
                }
            }
            // 删除取消订阅的topic
            if (session.size() == 0) {
                sessionIterator.remove();
            }
        }
		// 若本destination中已经不存在session连接，则删除destination
        if(destinationMap.get(destination).size()==0){
        	destinationMap.remove(destination);
		}
        lock.unlock();

    }
}
