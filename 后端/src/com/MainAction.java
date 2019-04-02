package com;

import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.sun.xml.internal.ws.developer.StreamingAttachment;
import com.sun.xml.internal.ws.policy.EffectiveAlternativeSelector;

import net.sf.json.JSONObject;

public class MainAction extends ActionSupport {
	String method;

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public String execute() throws Exception {
		ServletActionContext.getResponse().addHeader("Access-Control-Allow-Origin", "http://localhost:63342");// 允许所有来源访问
		ServletActionContext.getResponse().addHeader("Access-Control-Allow-Method", "POST,GET");// 允许访问的方式
		ServletActionContext.getResponse().addHeader("Access-Control-Allow-Credentials", "true");
		if (method.equals("search")) {
			return "search";
		} else if (method.equals("register")) {
			return "register";
		} else if (method.equals("recommend")) {
			return "recommend";
		} else if (method.equals("login")) {
			return "login";
		} else if (method.equals("addHot")) {
			return "addHot";
		} else if (method.equals("rate")) {
			return "rate";
		} else if (method.equals("getRate")) {
			return "getRate";
		} else if (method.equals("check")) {
			Cookie[] cookies = ServletActionContext.getRequest().getCookies();
			String userId = null;
			if (cookies == null || cookies.length == 0) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error_code", 0);
				jsonObject.put("msg", "已登录");
				jsonObject.put("data", new JSONObject());
				ServletActionContext.getRequest().setAttribute("json", jsonObject);
			} else {
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getName().equals("userId"))
						userId = cookies[i].getValue();
				}
				if (userId == null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("error_code", 1);
					jsonObject.put("msg", "登录状态失效");
					jsonObject.put("data", new JSONObject());
					ServletActionContext.getRequest().setAttribute("json", jsonObject);
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("error_code", 0);
					jsonObject.put("msg", "已登录");
					jsonObject.put("data", new JSONObject());
					ServletActionContext.getRequest().setAttribute("json", jsonObject);
				}
			}
		}
		else {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("error_code", 1);
			jsonObject.put("msg", "方法未找到");
			jsonObject.put("data", new JSONObject());
			ServletActionContext.getRequest().setAttribute("json", jsonObject);
		}
		return SUCCESS;
	}
}
