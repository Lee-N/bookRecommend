package com;

import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.opensymphony.xwork2.ActionSupport;

import net.sf.json.JSONObject;


public class RateAction extends ActionSupport{
	private String bookId;
    private int userId=0;
    private String rate;
	@Override
	public String execute() throws Exception {
		try {		
			//验证登录态

			Cookie[] cookies=ServletActionContext.getRequest().getCookies();
			if(cookies!=null) {
				for(int i=0;i<cookies.length;i++) {
					if(cookies[i].getName().equals("userId"))
						userId=Integer.parseInt(cookies[i].getValue());
				}
			}
			if (userId==0) {
				JSONObject jsonObject=new JSONObject();
				jsonObject.put("error_code", 1);
				jsonObject.put("msg", "登录状态失效");
				jsonObject.put("data",new JSONObject());
				ServletActionContext.getRequest().setAttribute("json", jsonObject);
				return SUCCESS;
			}
			System.out.println(userId);
			System.out.println(bookId);
			System.out.println(rate);
			
			Configuration configuration = new Configuration().configure();
		    SessionFactory factory = configuration.buildSessionFactory();
			Session session=factory.openSession();
			Transaction transaction=session.beginTransaction();	
			
			String sql="insert into rate values("+userId+","+bookId+","+rate+") on DUPLICATE key update rate=values(rate)";
  			SQLQuery query = session.createSQLQuery(sql);
  	        query.executeUpdate();
			 session.close();
			 factory.close();
	}catch (Exception e) {
		System.out.println(e.getMessage());
	}
		return SUCCESS;
	}
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	
	

}
