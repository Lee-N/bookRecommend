package com;

import java.sql.SQLException;

import org.apache.struts2.ServletActionContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.opensymphony.xwork2.ActionSupport;
import com.sun.org.apache.bcel.internal.generic.NEW;

import net.sf.json.JSONObject;

public class RegistAction extends ActionSupport {
	User user=new User();
	String username;
	String password;
    String name;
    String hobby;
	JSONObject jsonObject=new JSONObject();
	@Override
	public String execute() throws Exception {
		try{
		    user.setUsername(username);
		    user.setPassword(password);
            user.setName(name);
		    user.setHobby(hobby);
		    Configuration configuration=new Configuration().configure();
	        SessionFactory factory=configuration.buildSessionFactory();
		    Session session=factory.openSession();
		    Transaction transaction=session.beginTransaction();
		    session.saveOrUpdate(user);
		    jsonObject.put("error_code",0);
			jsonObject.put("msg","");
		    jsonObject.put("data", new JSONObject());
			transaction.commit();
			session.close();
			factory.close();
		}catch (Exception e) {
			jsonObject.put("error_code",1);
			jsonObject.put("msg","用户名已存在");
			jsonObject.put("data", new JSONObject());
		}
		ServletActionContext.getRequest().setAttribute("json", jsonObject);
		return SUCCESS;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHobby() {
		return hobby;
	}
	public void setHobby(String hobby) {
		this.hobby = hobby;
	}
	
	
	
}
