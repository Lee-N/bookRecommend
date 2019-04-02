package com;


import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import com.opensymphony.xwork2.*;
import com.sun.org.apache.bcel.internal.generic.NEW;

import net.sf.json.JSONObject;
public class LoginAction extends ActionSupport {
	String username;
	String password;
	JSONObject jsonObject=new JSONObject();
	JSONObject data=new JSONObject();
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
public String execute(){
	try {
		Configuration configuration = new Configuration().configure();
	    SessionFactory factory = configuration.buildSessionFactory();
		Session session=factory.openSession();
		Transaction transaction=session.beginTransaction();
		String hql = "from User where username=:username and password=:password";
	    Query<User> query = session.createQuery(hql,User.class);
	    query.setParameter("username", username);
	    query.setParameter("password", password);
	    List<User> list = query.list();
	    if(list.size()==0) {
	    	jsonObject.put("error_code",1);
	    	jsonObject.put("msg","用户名或密码错误");
	    	jsonObject.put("data",data);
	    }
	    else {
	    	jsonObject.put("error_code",0);
	    	jsonObject.put("msg","");
	    	data.put("name",list.get(0).getName());
	    	jsonObject.put("data",data);
	    	Cookie userId=new Cookie("userId",list.get(0).getUserId()+"");
	    	Cookie hobby=new Cookie("hobby", list.get(0).getHobby());
	    	userId.setMaxAge(3600*12);
	    	hobby.setMaxAge(3600*12);
	    	ServletActionContext.getResponse().addCookie(userId);
	    	ServletActionContext.getResponse().addCookie(hobby);
	    }
		transaction.commit();
		session.close();
		factory.close();
	} catch (Exception e) {
		System.out.println(e.getMessage());
    	jsonObject.put("error_code",1);
	}
	ServletActionContext.getRequest().setAttribute("json", jsonObject);
	return SUCCESS;
}
}
