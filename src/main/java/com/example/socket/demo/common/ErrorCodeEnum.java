package com.example.socket.demo.common;

/**
 * 错误码枚举类（所有的错误信息）
 * 
 * @author arges
 *
 */
public enum ErrorCodeEnum
{
	/**
	 * 
	 */
	SUCCESS("0", "成功"),
	/**
	 * 
	 */
	FAIL("1", "系统错误"),

	/**
	 * 没有接口调用权限
	 */
	POWER("9", "没有权限"),

	/** 家长模块对应的错误码 **/
	/**
	 * 注册对应的号码重复
	 */
	PHONE_REPEAT_ERROR("0101", "号码已经存在"),

	/**
	 * 登录号码不存在
	 */
	PHONE_NOTEXIT_ERROR("0102", "号码不存在"),

	/**
	 * 登录号码无效（被冻结）
	 */
	PHONE_DISABLE_ERROR("0103", "账号无效"),

	/**
	 * 登陆对应的用户名密码错误
	 */
	PHONE_ERROR_LOGIN("0104", "用户名密码错误"),

	/**
	 * 验证码错误
	 */
	CODE_ERROR("0105", "验证码错误"),

	/**
	 * 入园号码错误
	 */
	PHONE_ERROR("0106", "孩子入园号码错误"),

	/**
	 * 登录、用户模块
	 */

	LOGIN_CODE_ERROR("0201", "验证码错误"),

	/**
	 * 用户名或密码错误
	 */
	LOGIN_ERROR("0202", "用户名或密码错误"),

	/**
	 * 视频直播控制时间设置错误
	 */
	VIDEO_MANAGER_ERROR("0301", "控制时间设置有误"),

	/**
	 * 参数类型错误，参数日期格式错误，参数数量不对应。。。
	 */
	WRONG_PARAMETER("-2", "参数错误：缺失或非法"),

	/**
	 * URI中的版本号错误：不是有效的版本号（不存在或已经放弃维护的版本）
	 */
	WRONG_VERSIONNUMBER("1", "找不到对应的有效版本"),

	/**
	 * 身份认证失败的返回标识码：401
	 * 前端不支持处理401的响应状态码：response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	 */
	UNAUTHORIZED("401", "身份认证失败"),

	TOKEN_INVALID("401", "无效token"),

	/**
	 * token 过期
	 */
	TOKEN_TIMEOUT("401", "无效token：token已过期"),

	/**
	 * token 签名错误
	 */
	TOKEN_SIGNATURE_MISSMATCH("401", "无效token：token签名错误"),

	/**
	 * 缺少 token 信息
	 */
	TOKEN_MISSING("401", "无效token：token缺失");

	/**
	 * 错误码
	 */
	private String code;

	/**
	 * 错误信息
	 */
	private String info;

	private ErrorCodeEnum(String code, String info)
	{
		this.code = code;
		this.info = info;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getInfo()
	{
		return info;
	}

	public void setInfo(String info)
	{
		this.info = info;
	}
}
