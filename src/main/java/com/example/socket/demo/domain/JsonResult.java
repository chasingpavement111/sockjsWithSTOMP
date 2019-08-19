package com.example.socket.demo.domain;

import java.io.Serializable;
import java.util.Map;

import com.example.socket.demo.common.ErrorCodeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @ClassName: JsonResult
 * @Description: 接口返回统一类
 * @author tdd
 * @date 2016年11月16日 上午10:50:59
 * 
 * @param <T>
 * @param <Y>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)//只传非null值字段给前端
public class JsonResult<T> implements Serializable
{
	private static final long serialVersionUID = -2077979445301602551L;

	/**
	 * 响应编码
	 */
	private String result_code;

	/**
	 * 响应描述
	 */
	private String result_desc;

	// 响应对象
	private T result_data;

	private Map<String, Object> map;

	public JsonResult()
	{
	}

	public JsonResult(Object result_data)
	{
		result_code = ErrorCodeEnum.SUCCESS.getCode();
		result_desc = ErrorCodeEnum.SUCCESS.getInfo();
		this.result_data = (T) result_data;
	}

	public String getResult_code()
	{
		return result_code;
	}

	private void setResult_code(String result_code)
	{
		this.result_code = result_code;
	}

	public String getResult_desc()
	{
		return result_desc;
	}

	public void setResult_desc(String result_desc)
	{
		this.result_desc = result_desc;
	}

	public T getResult_data()
	{
		return result_data;
	}

	private void setResult_data(T result_data)
	{
		this.result_data = result_data;
	}

	public Map<String, Object> getMap()
	{
		return map;
	}

	public void setMap(Map<String, Object> map)
	{
		this.map = map;
	}

	public void setSuccess(T data)
	{
		setResult_code(ErrorCodeEnum.SUCCESS.getCode());
		setResult_desc(ErrorCodeEnum.SUCCESS.getInfo());
		setResult_data(data);
	}

	public void setFail()
	{
		setResult_code(ErrorCodeEnum.FAIL.getCode());
		setResult_desc(ErrorCodeEnum.FAIL.getInfo());
	}

	public void setResult(ErrorCodeEnum enmu)
	{
		setResult_code(enmu.getCode());
		setResult_desc(enmu.getInfo());
	}
}
