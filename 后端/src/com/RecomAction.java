package com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.From;
import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import com.opensymphony.xwork2.ActionSupport;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RecomAction extends ActionSupport {
	String recSearch = "";// 最近一次浏览内容
	String hobby = "";
	int weights = 0;
	JSONObject jsonObject = new JSONObject();
	JSONObject category = new JSONObject();
	JSONArray data = new JSONArray();
	JSONArray resources = new JSONArray();
	int userId = 0;
	int bookSum = 0;
	int userSum = 0;
	int maxSimuserID = 0;// 最相似用户的id
	int maxLikebookID1 = 0;
	int maxLikebookID2 = 0;
	int maxLikebookID3 = 0;
	int allBookSize = 0;
	float Matrix[][];// 相似度矩阵
	double maxSim = 0;// 用户最大相似度
	Map<Integer, Double> userSim = new HashMap<>();// 用户相似度哈希表
	List<Map.Entry<Integer, Double>> userSimArray;// 用于用户相似度排序的array
	List<Rate> userRate = new ArrayList<>();
	List<Resources> allBooks;
	List<Resources> maxSimBooks = new ArrayList<>(3);

	public String execute() {
		try {
			// 验证登录态
			Cookie[] cookies = ServletActionContext.getRequest().getCookies();
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getName().equals("userId"))
						userId = Integer.parseInt(cookies[i].getValue());
					else if (cookies[i].getName().equals("hobby")) {
						hobby = cookies[i].getValue();
					}
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

			/* 基于用户协同过滤 */

			// 统计书的总数
			String sql = "select count(*) sum from resources union select count(*) sum from user ";
			SQLQuery query1 = session.createSQLQuery(sql);
			bookSum = Integer.parseInt(query1.list().get(0).toString());
			userSum = Integer.parseInt(query1.list().get(1).toString());
			System.out.println("bookSum=" + bookSum);
			System.out.println("userSum=" + userSum);
			Matrix = new float[userSum][bookSum]; // 稀疏矩阵
			// 得到所有的用户评分
			String hql = "from Rate";
			Query<Rate> query4 = session.createQuery(hql, Rate.class);
			List<Rate> rateList = query4.list();

			if(rateList.size()!=0) {
				// 赋值并中心化处理
				int allRate = 0;
				int rateSum = 0;
				Map<Integer, Float> average = new HashMap<Integer, Float>(); // 存放每一个用户的平均评分
				int nowUser = rateList.get(0).getUserId();
				for (int i = 0; i < rateList.size(); i++) {
					if (rateList.get(i).getUserId() == nowUser) {
						allRate += rateList.get(i).getRate();
						rateSum++;
					} else {
						average.put(nowUser, (float) allRate / (float) rateSum);
						nowUser = rateList.get(i).getUserId();
						allRate = rateList.get(i).getRate();
						rateSum = 1;
					}
					if (rateList.get(i).getUserId() == userId) {
						userRate.add(rateList.get(i));
					}
				}

				average.put(nowUser, (float) allRate / (float) rateSum);

				for (int i = 0; i < rateList.size(); i++) {
					int userID = rateList.get(i).getUserId() - 1;
					int bookID = rateList.get(i).getBookId() - 1;
					int rate = rateList.get(i).getRate();
					Matrix[userID][bookID] = (float) rate - average.get(userID + 1);
				}

				// 赋值并中心化完毕

				// 计算余弦相似度寻找相似用户
				for (int i = 0; i < userSum; i++) {
					if (i != userId - 1) {
						double sim = 0;
						// 分子
						float o = 0;
						// 分母1
						float p = 0;
						// 分母2
						float q = 0;
						for (int j = 0; j < bookSum; j++) {
							o += Matrix[i][j] * Matrix[userId - 1][j];
							p += Matrix[i][j] * Matrix[i][j];
							q += Matrix[userId - 1][j] * Matrix[userId - 1][j];
						}
						sim = o / (Math.sqrt(p) + Math.sqrt(q));
						if(Double.isNaN(sim))sim=0; // 解决0/0导致NAN的问题
						System.out.println("相似度"+sim);
						userSim.put(i + 1, sim);
					}
				}

				// 用户相似度排序
				userSimArray = new ArrayList<Map.Entry<Integer, Double>>(userSim.entrySet());
				Collections.sort(userSimArray, new Comparator<Map.Entry<Integer, Double>>() {
					@Override
					public int compare(Map.Entry<Integer, Double> r1, Map.Entry<Integer, Double> r2) {
						return -(r1.getValue().compareTo(r2.getValue()));
					}
				});
				maxSim = userSimArray.get(0).getValue();
				maxSimuserID = userSimArray.get(0).getKey();
				System.out.println("最大相似度" + maxSim);
				System.out.println("最相似用户" + maxSimuserID);
				sql="";
				
//				认为两个用户相似度大于0.3为相似
				if(maxSim>=0.3) {
					// 最相似用户评分最高的两本书
					sql = "select * from resources where bookId in (select * from  (select bookId from rate where userId="
							+ maxSimuserID + " order by rate desc limit 2)as n) union ";
				}
				
			}
		

			

			

			/* 基于内容推荐 */

			// 用户评分数组排序
			Collections.sort(userRate, new Comparator<Rate>() {
				@Override
				public int compare(Rate o1, Rate o2) {
					if (o1.getRate() > o2.getRate())
						return -1;
					else if (o1.getRate() < o2.getRate())
						return 1;
					else
						return 0;
				}
			});

			hql = "from Resources";
			Query<Resources> query5 = session.createQuery(hql, Resources.class);
			allBooks = query5.list();
			allBookSize = allBooks.size();

			/*
			 * 认为评分大于三的书才是用户喜欢的 所以评分大于三才进行推荐 如果用户只评价了一本书找相似度最高的三本书进行推荐
			 * 如果用户只对两本书进行过评价评价第一的推荐相似度最高的两本书 评价第二的推荐相似度最高的一本书 如果只有一个评价超过
			 * 三分就根据唯一一本超过三分的推荐三本相似度最高的书 如果用户评价超过三本书就对 三本每本书评分都超过三分 每一本书都推荐相似度最高的一本
			 * 如果两本书超过三分 就两本中分较高的推荐两本 剩下评分较低的推荐一本
			 */
			if (userRate.size() == 0) {

			} else if (userRate.size() == 1 && userRate.get(0).getRate() >= 3) {
				String result = getRecom1(userRate.get(0).getBookId());
				String a[] = result.split("#");
				sql+="  select * from resources where bookId in("+a[0]+","+a[1]+","+a[2]+") union ";
			} else if (userRate.size() == 2) {
				int rate1 = userRate.get(0).getRate();
				int rate2 = userRate.get(1).getRate();
				int bookId1 = userRate.get(0).getBookId();
				int bookId2 = userRate.get(1).getBookId();
				String result = "";
				if (rate2 >= 3) {
					result = getRecom2(bookId1, bookId2);
					String a[] = result.split("#");
					sql+="  select * from resources where bookId in("+a[0]+","+a[1]+","+a[2]+") union ";
				} else if (rate1 >= 3) {
					result = getRecom1(bookId1);
					String a[] = result.split("#");
					sql+="  select * from resources where bookId in("+a[0]+","+a[1]+","+a[2]+") union ";
				}
			} else {
				int rate1 = userRate.get(0).getRate();
				int rate2 = userRate.get(1).getRate();
				int rate3 = userRate.get(2).getRate();
				int bookId1 = userRate.get(0).getBookId();
				int bookId2 = userRate.get(1).getBookId();
				int bookId3 = userRate.get(2).getBookId();
				String result = "";
				if (rate3 >= 3) {
					result = getRecom3(bookId1, bookId2, bookId3);
					String a[] = result.split("#");
					sql+="  select * from resources where bookId in("+a[0]+","+a[1]+","+a[2]+") union ";
				} else if (rate2 >= 3) {
					result = getRecom2(bookId1, bookId2);
					String a[] = result.split("#");
					sql+="  select * from resources where bookId in("+a[0]+","+a[1]+","+a[2]+") union ";
				} else if (rate1 >= 3) {
					result = getRecom1(bookId1);
					String a[] = result.split("#");
					sql+="  select * from resources where bookId in("+a[0]+","+a[1]+","+a[2]+") union ";
				}

			}
			
			/* 基于热度推荐 解决冷启动 */
			sql += " select * from (select * from resources order by hot desc limit 2) as b";

			/* 基于搜索历史 */

			// 获取最近两条搜索记录
			hql = "from Record where userId=:userId order by createdAt desc";
			Query<Record> query2 = session.createQuery(hql, Record.class);
			query2.setParameter("userId", userId);
			query2.setMaxResults(2);
			List<Record> list = query2.list();
			if (list.size() != 0) {
				sql += " union select * from (select * from Resources where CONCAT(bookName,',',description,',',keywords,',',category) regexp '";
				for (int i = 0; i < list.size(); i++) {// 如果有最近搜索记录的话加上最近搜索记录
					recSearch = list.get(i).getContent();// 获取最近搜索记录
					if (i != list.size() - 1) {			
						sql += recSearch + "|";
					} else
						sql += recSearch;
				}
				sql += "' limit 3)as a  ";
			}

			
			/*根据注册时填的爱好推荐进行填充(解决冷启动)*/
			sql+=" union select * from ((select * from Resources where CONCAT(bookName,',',description,',',keywords,',',category) regexp '";
			String[] hobbies = hobby.split("#");// 把hobby拆分 拼接
			int limit=20/hobbies.length;
			if (limit<1)limit=1;
			for (int i = 0; i < hobbies.length; i++) {
				if (i != hobbies.length - 1)
					sql += hobbies[i] + "' limit "+limit+") "
							+ " union (select * from Resources where CONCAT(bookName,',',description,',',keywords,',',category) regexp '";
				else
					sql += hobbies[i]+"' limit "+(20-(limit*(hobbies.length-1)))+"))as a ";
			}

			SQLQuery<Resources> query3 = session.createSQLQuery(sql).addEntity(Resources.class);
			List<Resources> list2 = query3.list();
			if (list2.size() == 0) {
				jsonObject.put("error_code", 0);
				jsonObject.put("msg", "");
				jsonObject.put("data", data);
			} else {
				for (int i = 0; i < list2.size(); i++) {
					data.add(JSONObject.fromObject(list2.get(i)));
					jsonObject.put("error_code", 0);
					jsonObject.put("msg", "");
					jsonObject.put("data", data);
				}
				transaction.commit();
				session.close();
				factory.close();
			}
		} catch (Exception e) {
			System.err.println("ERROR");
			System.out.println(e.getMessage());
			jsonObject.put("error_code", 1);
			jsonObject.put("msg", "");
			jsonObject.put("data", new JSONObject());
		}
		ServletActionContext.getRequest().setAttribute("json", jsonObject);
		return SUCCESS;
	}

	// 推荐一本书
	public String getRecom1(int bookId) {
		maxLikebookID1 = bookId;
		double maxBookSim1 = 0;
		double maxBookSim2 = 0;
		double maxBookSim3 = 0;
		int maxSimBookId1 = 0;
		int maxSimBookId2 = 0;
		int maxSimBookId3 = 0;
		int maxcount = 0;
		double bookSim = 0;
		List<String> b2 = Arrays.asList(allBooks.get(maxLikebookID1 - 1).getKeywords().split("#")); // 将用户最喜欢的书的特性拆分成数组
		for (int i = 0; i < allBookSize; i++) {
			if (allBooks.get(i).getBookId() != maxLikebookID1) {
				int count = 0;
				List<String> b1 = Arrays.asList(allBooks.get(i).getKeywords().split("#")); // 将每本书的特性拆分成数组
				for (int j = 0; j < b2.size(); j++) {
					if (b1.indexOf(b2.get(j)) != -1) {
						count++; // 如果有属性相同则count++
					}
				}
				if (count > maxcount)
					maxcount = count;
				bookSim = (double) count / (b1.size() + b2.size() - count);
				if (bookSim > maxBookSim1) {
					maxBookSim3 = maxBookSim2;
					maxBookSim2 = maxBookSim1;
					maxBookSim1 = bookSim;
					maxSimBookId3 = maxSimBookId2;
					maxSimBookId2 = maxSimBookId1;
					maxSimBookId1 = i+1;
				} else if (bookSim > maxBookSim2) {
					maxBookSim3 = maxBookSim2;
					maxBookSim2 = bookSim;
					maxSimBookId3 = maxSimBookId2;
					maxSimBookId2 = i+1;
				} else if (bookSim > maxBookSim3) {
					maxBookSim3 = bookSim;
					maxSimBookId3 = i+1;
				}
			}
		}
		System.out.println("原图书ID: "+bookId);
		System.out.println("最大相似度: "+maxBookSim1+" "+maxBookSim2+" "+maxBookSim3);
		System.out.println("最大相似图书ID: "+maxSimBookId1 + " " + maxSimBookId2 + " " + maxSimBookId3);
		return maxSimBookId1 + "#" + maxSimBookId2 + "#" + maxSimBookId3;
	}

	// 推荐两本书
	public String getRecom2(int bookId1, int bookId2) {
		maxLikebookID1 = userRate.get(0).getBookId();
		maxLikebookID2 = userRate.get(1).getBookId();
		double maxBookSim1 = 0;
		double maxBookSim2 = 0;
		double maxBookSim3 = 0;
		int maxSimBookId1 = 0;
		int maxSimBookId2 = 0;
		int maxSimBookId3 = 0;
		double bookSim1 = 0;
		double bookSim2 = 0;
		List<String> b1 = Arrays.asList(allBooks.get(maxLikebookID1 - 1).getKeywords().split("#"));
		List<String> b2 = Arrays.asList(allBooks.get(maxLikebookID2 - 1).getKeywords().split("#"));
		for (int i = 0; i < allBookSize; i++) {
			int count1 = 0;
			int count2 = 0;
			List<String> b3 = Arrays.asList(allBooks.get(i).getKeywords().split("#"));
			for (int j = 0; j < b1.size(); j++) {
				if (b3.indexOf(b1.get(j)) != -1) {
					count1++;
				}
				if (b3.indexOf(b2.get(j)) != -1) {
					count2++;
				}
			}
			bookSim1 = (double) count1 / (b1.size() + b3.size() - count1);
			bookSim2 = (double) count2 / (b2.size() + b3.size() - count2);
			if (bookSim1 > maxBookSim1 && (maxLikebookID1 - 1) != i) {
				maxBookSim2 = maxBookSim1;
				maxBookSim1 = bookSim1;
				maxSimBookId2 = maxSimBookId1;
				maxSimBookId1 = i+1;
			} else if (bookSim1 > maxBookSim2 && (maxLikebookID1 - 1) != i) {
				maxBookSim2 = bookSim1;
				maxSimBookId2 = i+1;
			}
			if (bookSim2 > maxBookSim3 && (maxLikebookID2 - 1) != i) {
				maxBookSim3 = bookSim2;
				maxSimBookId3 = i+1;
			}
		}
		System.out.println("原图书ID: "+bookId1+" "+bookId2);
		System.out.println("最大相似度: "+maxBookSim1+" "+maxBookSim2+" "+maxBookSim3);
		System.out.println("最大相似图书ID: "+maxSimBookId1 + " " + maxSimBookId2 + " " + maxSimBookId3);
		return maxSimBookId1 + "#" + maxSimBookId2 + "#" + maxSimBookId3;
	}

	// 推荐三本书
	public String getRecom3(int bookId1, int bookId2, int bookId3) {
		maxLikebookID1 = bookId1;
		maxLikebookID2 = bookId2;
		maxLikebookID3 = bookId3;
		double maxBookSim1 = 0;
		double maxBookSim2 = 0;
		double maxBookSim3 = 0;
		int maxSimBookId1 = 0;
		int maxSimBookId2 = 0;
		int maxSimBookId3 = 0;
		double bookSim1 = 0;
		double bookSim2 = 0;
		double bookSim3 = 0;
		List<String> b1 = Arrays.asList(allBooks.get(maxLikebookID1 - 1).getKeywords().split("#"));
		List<String> b2 = Arrays.asList(allBooks.get(maxLikebookID2 - 1).getKeywords().split("#"));
		List<String> b3 = Arrays.asList(allBooks.get(maxLikebookID3 - 1).getKeywords().split("#"));
		for (int i = 0; i < allBookSize; i++) {
			int count1 = 0;
			int count2 = 0;
			int count3 = 0;
			List<String> b4 = Arrays.asList(allBooks.get(i).getKeywords().split("#"));
			for (int j = 0; j < b1.size(); j++) {
				if (b4.indexOf(b1.get(j)) != -1) {
					count1++;
				}
				if (b4.indexOf(b2.get(j)) != -1) {
					count2++;
				}
				if (b4.indexOf(b3.get(j)) != -1) {
					count3++;
				}
			}
			bookSim1 = (double) count1 / (b1.size() + b4.size() - count1);
			bookSim2 = (double) count2 / (b2.size() + b4.size() - count2);
			bookSim3 = (double) count3 / (b3.size() + b4.size() - count3);
			if (bookSim1 > maxBookSim1 && (maxLikebookID1 - 1) != i) {
				maxBookSim1 = bookSim1;
				maxSimBookId1 = i+1;
			}
			if (bookSim2 > maxBookSim2 && (maxLikebookID2 - 1) != i) {
				maxBookSim2 = bookSim2;
				maxSimBookId2 = i+1;
			}
			if (bookSim3 > maxBookSim3 && (maxLikebookID3 - 1) != i) {
				maxBookSim3 = bookSim3;
				maxSimBookId3 = i+1;
			}
		}
		System.out.println("原图书ID: "+bookId1+" "+bookId2+" "+bookId3);
		System.out.println("最大相似度: "+maxBookSim1+" "+maxBookSim2+" "+maxBookSim3);
		System.out.println("最大相似图书ID: "+maxSimBookId1 + " " + maxSimBookId2 + " " + maxSimBookId3);
		return maxSimBookId1 + "#" + maxSimBookId2 + "#" + maxSimBookId3;
	}
}
