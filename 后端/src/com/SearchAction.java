package com;

import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import com.opensymphony.xwork2.ActionSupport;
import com.sun.org.apache.bcel.internal.generic.NEW;

import jdk.nashorn.internal.runtime.linker.LinkerCallSite;
import net.bytebuddy.description.field.FieldDescription.InGenericShape;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SearchAction extends ActionSupport {
	JSONObject jsonObject = new JSONObject();
	JSONObject category = new JSONObject();
	JSONArray data = new JSONArray();
	JSONArray resources = new JSONArray();
	Record record = new Record();
	int userId = 0;
	String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String execute() {
		try {
			// 验证登录态
			Cookie[] cookies = ServletActionContext.getRequest().getCookies();
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getName().equals("userId"))
						userId = Integer.parseInt(cookies[i].getValue());
				}
			}
			if (userId == 0) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error_code", 1);
				jsonObject.put("msg", "登录状态失效");
				jsonObject.put("data", new JSONObject());
				ServletActionContext.getRequest().setAttribute("json", jsonObject);
				return SUCCESS;
			}
			Configuration configuration = new Configuration().configure();
			SessionFactory factory = configuration.buildSessionFactory();
			Session session = factory.openSession();
			Transaction transaction = session.beginTransaction();
			String hql = "from Resources where bookName like :content or author like :content or category like :content or "
					+ "  keywords like :content";
			Query<Resources> query = session.createQuery(hql, Resources.class);
			query.setParameter("content", "%" + content + "%");
			query.setMaxResults(50);// 一次最多返回50条
			List<Resources> list = query.list();
			if (list.size() == 0) {
				jsonObject.put("error_code", 0);
				jsonObject.put("msg", "");
				jsonObject.put("data", data);
			} else {
				String curr_category = list.get(0).getCategory();// 当前分类
				for (int i = 0; i < list.size(); i++) {
					data.add(JSONObject.fromObject(list.get(i)));
					jsonObject.put("error_code", 0);
					jsonObject.put("msg", "");
					jsonObject.put("data", data);
				}
			}
			// 保存搜索记录
			record.setContent(content);
			record.setUserId(userId);
			record.setCreatedAt(new Date());
			session.saveOrUpdate(record);
			transaction.commit();
			session.close();
			factory.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			jsonObject.put("error_code", 1);
			jsonObject.put("msg", "");
			jsonObject.put("data", new JSONObject());
		}
		ServletActionContext.getRequest().setAttribute("json", jsonObject);
		return SUCCESS;
	}
}
