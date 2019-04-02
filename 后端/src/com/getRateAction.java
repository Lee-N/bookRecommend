package com;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import com.opensymphony.xwork2.ActionSupport;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class getRateAction extends ActionSupport {
	int userId = 0;
	JSONObject jsonObject = new JSONObject();
	JSONArray data=new JSONArray();

	@Override
	public String execute() throws Exception {
		try {
			Cookie[] cookies = ServletActionContext.getRequest().getCookies();
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals("userId"))
					userId = Integer.parseInt(cookies[i].getValue());
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
			String hql = "from Rate r where r.userId=:userId";
			Query query = session.createQuery(hql);
			query.setParameter("userId", userId);
			List<Rate> rates = query.list();
			if(rates.size()!=0) {
				for(int i=0;i<rates.size();i++)
					data.add(JSONObject.fromObject(rates.get(i)));
			}
			transaction.commit();
			jsonObject.put("error_code", 0);
			jsonObject.put("msg", "");
			jsonObject.put("data", data);
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
