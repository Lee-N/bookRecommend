package com;

import java.io.Serializable;

public class Rate implements Serializable{
	private int userId;
	private int bookId;
	private int rate;
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getBookId() {
		return bookId;
	}
	public void setBookId(int bookId) {
		this.bookId = bookId;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rate Rate = (Rate) o;

        if (userId+"" != null ? !(userId==Rate.userId) : Rate.userId+"" != null) return false;
        if (bookId+"" != null ? !(bookId==Rate.bookId) : Rate.bookId+"" != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = userId+"" != null ? userId+"".hashCode() : 0;
        result = 31 * result + (bookId+"" != null ? bookId+"".hashCode() : 0);
        return result;
    }


}
