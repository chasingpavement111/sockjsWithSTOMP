package com.example.socket.demo.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.websocket.Session;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * websocket消息交换类
 *
 * @author zhangjie
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomSocketMessage implements Serializable
{
	private static final long serialVersionUID = 176717301088288437L;

	/**
	 * 消息类型：与 MessageMapping.value 一致
	 * alarmCount : 学校报警总数
	 * alarmInfo : 新增的报警信息 todo List<AlarmRecordBean>
	 * sensorInfo : 变更的传感器信息 todo List<ChannelRtRecordBean>
	 */
	private String msgType;

	private Session session;

	/**
	 * 消息发布时间
	 */
	private Date issuedTime;

	/**
	 * 推送目标用户的类型
	 */
	private Integer userType;

	/**
	 * 推送目标用户的所属学校id
	 */
	private Long businessId;

	/**
	 * 学校报警总数信息体
	 * key=报警类型（com.arges.web.common.AlarmTypeEnum.englishName）
	 * value=报警总数
	 */
	private List<Map<String, Object>> list;

	public String getMsgType()
	{
		return msgType;
	}

	public void setMsgType(String msgType)
	{
		this.msgType = msgType;
	}

	public Session getSession()
	{
		return session;
	}

	public void setSession(Session session)
	{
		this.session = session;
	}

	public Date getIssuedTime()
	{
		return issuedTime;
	}

	public void setIssuedTime(Date issuedTime)
	{
		this.issuedTime = issuedTime;
	}

	public Integer getUserType()
	{
		return userType;
	}

	public void setUserType(Integer userType)
	{
		this.userType = userType;
	}

	public Long getBusinessId()
	{
		return businessId;
	}

	public void setBusinessId(Long businessId)
	{
		this.businessId = businessId;
	}

	public List<Map<String, Object>> getList()
	{
		return list;
	}

	public void setList(List<Map<String, Object>> list)
	{
		this.list = list;
	}
}
