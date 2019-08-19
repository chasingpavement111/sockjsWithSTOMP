package com.example.socket.demo.common;

public class Constant
{
	/**
	 * 在session中存放登录用户信息的key
	 */
	public static final String MANAGER_SESSION_NAME = "manager";
	/**
	 * 前端类型标识字段
	 */
	public static final String FRONT_END_CODE_NAME = "frontEndCode";
	/**
	 * token 在 请求头中的headerName值
	 */
	public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
	/**
	 * 返回体中的key值
	 * 存储token的字段名称
	 * access_token 用于访问校验
	 */
	public static final String TOKEN_NAME_FOR_ACCESS = "accessToken";
	/**
	 * 返回体中的key值
	 * 存储token的字段名称
	 * refresh_token 用于刷新 access_token
	 */
	public static final String TOKEN_NAME_FOR_REFRESH = "refreshToken";
	/**
	 * websocket 获取更新消息的目的地mapping值
	 */
	public static final String WEBSOCKET_DESTINATION_FOR_UPDATES = "/v1/updates";
	/**
	 * websocket 订阅更新消息的topic值：用于一对多广播
	 */
	public static final String WEBSOCKET_TOPIC_NAME_FOR_UPDATES = "/topic/v1/notifications/updates";
	/**
	 * websocket 订阅更新消息的queue值：用于一对一对话
	 */
	public static final String WEBSOCKET_QUEUE_NAME_FOR_UPDATES = "/queue/v1/notifications/updates";
	/**
	 * websocket 订阅报错信息消息的topic值：用于一对一对话
	 */
	public static final String WEBSOCKET_QUEUE_NAME_FOR_ERRORS = "/queue/v1/errors";
	/**
	 * 用户表：有效状态值 = 0
	 */
	public static final int VALID_STATUS_VALUE = 0;
	/**
	 * 用户表：无效状态值 = -1
	 */
	public static final int NOTVALID_STATUS_VALUE = -1;
	/**
	 * 用户类型：学校用户
	 */
	public static final int USERTYPE_SCHOOL = 1;
	/**
	 * 用户类型：管理员用户（教育局用户）
	 */
	public static final int USERTYPE_ADMIN = 2;
	/**
	 * 视频通道的通道类型值：1
	 */
	public static final int VIDEO_CHANNELTYPE = 1;
	/**
	 * 传感器的通道类型值：8
	 */
	public static final int SENSOR_CHANNELTYPE = 8;
	/**
	 * 视频播放的码流类型(1:RTSP 2:RTMP 3:HLS协)
	 * 默认值为0
	 */
	public static final String DEFAULT_STREAMTYPE = "0";
	/**
	 * 默认的用户密码
	 */
	public static final String DEFAULT_USER_PASSWORD = "123456";
	/**
	 * 统计功能中，用于存储百分比数值的字段名称前缀
	 * 需要统一前缀，因为需要利用统一的方法（“最大余数法”）对【百分数之和不等于100%】的方法对各百分比进行补偿使和为100%
	 */
	public static final String FIELDNAME_PREFIX_OF_PERCETAGE = "percentageOf";
}
