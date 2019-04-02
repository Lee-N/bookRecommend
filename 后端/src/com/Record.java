package com;

import java.util.Date;

public class Record {
   private int ID;
   private int userId;
   private Date createdAt;
   private String content;
public int getID() {
	return ID;
}
public void setID(int iD) {
	ID = iD;
}
public int getUserId() {
	return userId;
}
public void setUserId(int userId) {
	this.userId = userId;
}

public Date getCreatedAt() {
	return createdAt;
}
public void setCreatedAt(Date createdAt) {
	this.createdAt = createdAt;
}
public String getContent() {
	return content;
}
public void setContent(String content) {
	this.content = content;
}
   
}
