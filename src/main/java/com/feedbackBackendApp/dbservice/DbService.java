package com.feedbackBackendApp.dbservice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feedbackBackendApp.exceptions.UserDoesNotExistException;
import com.feedbackBackendApp.exceptions.VendorDoesNotExistsException;
import com.feedbackBackendApp.responsedata.Feedback;
import com.feedbackBackendApp.responsedata.FeedbackData;
import com.feedbackBackendApp.responsedata.FinalFeedBack;
import com.feedbackBackendApp.responsedata.FinalFeedBackData;
import com.feedbackBackendApp.responsedata.Message;
import com.feedbackBackendApp.responsedata.Sentence;
import com.feedbackBackendApp.responsedata.Sentiment;
import com.feedbackBackendApp.responsedata.TextSpan;
import com.feedbackBackendApp.responsedata.User;
import com.feedbackBackendApp.responsedata.Vendor;
import com.feedbackBackendApp.responsedata.VendorFeebackData;

/*

FULL QUERY : 
SELECT * FROM (SELECT * FROM (VENDOR INNER JOIN ORDERS USING (VENDORID)) LIMIT 10) VENDORORDERS JOIN MESSAGES USING (ORDERNUMBER,ORDERTIME);

*/

/*
 * SELECT * FROM VENDOR JOIN (SELECT * FROM MESSAGES JOIN FEEDBACK ON MESSAGES.FEEDBACK_MID = FEEDBACK.FEEDBACK_ID) DATA ON VENDOR.UPI_ID = DATA.VENDOR_UPI_ID;
 * 
 * 
 * SELECT MESSAGE,MESSAGE_SCORE,FEEDBACK_ID,FEEDBACK_MESSAGE,FEEDBACK_TIME,SCORE,INACCURATE FROM VENDOR JOIN (SELECT * FROM MESSAGES JOIN FEEDBACK ON MESSAGES.FEEDBACK_MID =   FEEDBACK.FEEDBACK_ID) DATA ON VENDOR.UPI_ID = DATA.VENDOR_UPI_ID;
 */

@Service
public class DbService {
	
	private static final String insertUserQuery = "INSERT INTO USER VALUES( :user_phone_number, :user_name , :password )";
	private static final String authUserQuery = "SELECT USER_NAME,USER_PHONE FROM USER WHERE USER_PHONE = :user_phone_number AND PASSWORD = :password";
	private static final String authVendorQuery = "SELECT VENDOR_NAME,VENDOR_PHONE FROM VENDOR WHERE VENDOR_PHONE = :vendorphone";
	private static final String getAllVendorFeedbacksQuery = "SELECT MESSAGE,MESSAGE_SCORE,FEEDBACK_ID,FEEDBACK_MESSAGE,FEEDBACK_TIME,SCORE,INACCURATE FROM (SELECT * FROM ( SELECT * FROM FEEDBACK WHERE VENDOR_UPI_ID IN (SELECT UPI_ID FROM VENDOR WHERE VENDOR_PHONE = :vendorphone ) ORDER BY FEEDBACK_ID DESC LIMIT 50) T1 JOIN MESSAGES ON MESSAGES.FEEDBACK_MID = T1.FEEDBACK_ID) T2 JOIN VENDOR ON VENDOR.UPI_ID = T2.VENDOR_UPI_ID";
	private static final String getOldUserFeedbacksQuery  = "SELECT MESSAGE,MESSAGE_SCORE,FEEDBACK_ID,FEEDBACK_MESSAGE,FEEDBACK_TIME,SCORE,INACCURATE FROM (SELECT * FROM ( SELECT * FROM FEEDBACK WHERE VENDOR_UPI_ID IN (SELECT UPI_ID FROM VENDOR WHERE VENDOR_PHONE = :vendorphone ) AND FEEDBACK_ID < :id ORDER BY FEEDBACK_ID DESC LIMIT 50) T1 JOIN MESSAGES ON MESSAGES.FEEDBACK_MID = T1.FEEDBACK_ID) T2 JOIN VENDOR ON VENDOR.UPI_ID = T2.VENDOR_UPI_ID";
	private static final String getUserFeedbacksQuery = "SELECT FEEDBACK_ID, FEEDBACK_MESSAGE, FEEDBACK_TIME, VENDOR_UPI_ID FROM FEEDBACK WHERE USER_PHN_NUM = :phonenumber ORDER BY FEEDBACK_ID LIMIT 50";
	private static final String checkVendorUpi = "SELECT 1 FROM VENDOR WHERE UPI_ID = :upi";	
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate2;

	@Autowired
	AsyncDbService asyncDbService;
	
	@Autowired
	public DbService(@Qualifier("dbsource") DataSource datasource, @Qualifier("dbsource2") DataSource datasource2) {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);
		namedParameterJdbcTemplate2 = new NamedParameterJdbcTemplate(datasource2);
	}

	public List<FinalFeedBackData> getOrders(int vendorID, int limit) {
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		mapSqlParameterSource.addValue("id", vendorID);
		mapSqlParameterSource.addValue("limit", limit);
		List<FinalFeedBackData> ordersList = namedParameterJdbcTemplate.query(
				"SELECT * FROM  ORDERS WHERE VENDORID = :id LIMIT :limit;", mapSqlParameterSource, new OrdersMapper());
		for (FinalFeedBackData orders : ordersList) {
			// orders.setSentences(getRecordsForEachOrder(orders.getNum(),
			// orders.getTime()));
			orders.setNegativeSentences(new ArrayList<>());
			orders.setPositiveSentences(new ArrayList<>());
			orders.setNeutralSentences(new ArrayList<>());

			List<Sentence> recordList = (getRecordsForEachOrder(orders.getNum(), orders.getTime()));
			for (Sentence sentence : recordList) {
				if (sentence.getSentiment().getScore() < -0.1) {
					orders.getNegativeSentences().add(sentence);
				} else if (sentence.getSentiment().getScore() > 0.3)
					orders.getPositiveSentences().add(sentence);
				else
					orders.getNeutralSentences().add(sentence);
			}
		}
		return ordersList;
	}
	


	public List<Sentence> getRecordsForEachOrder(int ordernumber, int time) {
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		mapSqlParameterSource.addValue("ordernumber", ordernumber);
		mapSqlParameterSource.addValue("time", time);
		List<Sentence> sentences = namedParameterJdbcTemplate.query(
				"SELECT MESSAGE,MESSAGESCORE FROM ORDERS JOIN MESSAGES USING (ORDERNUMBER,ORDERTIME) WHERE ORDERNUMBER = :ordernumber AND ORDERTIME = :time;",
				mapSqlParameterSource, new SentenceMapper());
		return sentences;
	}

	public int insertData(FinalFeedBackData finalFeedBackData) {
		String insertQuery = "INSERT INTO ORDERS VALUES(:num,:time,:score,:category,:feedback,:vendorid,:inacc)";
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();

		int num = finalFeedBackData.getNum();
		int time = finalFeedBackData.getTime();
		mapSqlParameterSource.addValue("num", num);
		mapSqlParameterSource.addValue("time", time);
		mapSqlParameterSource.addValue("score", finalFeedBackData.getSentimentScore());
		mapSqlParameterSource.addValue("category", finalFeedBackData.getCategory());
		mapSqlParameterSource.addValue("feedback", finalFeedBackData.getFeedback());
		mapSqlParameterSource.addValue("vendorid", finalFeedBackData.getVendorID());
		mapSqlParameterSource.addValue("inacc", finalFeedBackData.getIsInaccurate());
		namedParameterJdbcTemplate.update(insertQuery, mapSqlParameterSource);

		if (finalFeedBackData.getIsInaccurate() == 0) {
			List<Sentence> list = finalFeedBackData.getSentences();
			int length = list.size();
			int index = 0;
			MapSqlParameterSource[] sentences = new MapSqlParameterSource[length];

			for (Sentence value : list) {

				MapSqlParameterSource mapSqlParameterSource2 = new MapSqlParameterSource();
				mapSqlParameterSource2.addValue("num", num);
				mapSqlParameterSource2.addValue("time", time);
				mapSqlParameterSource2.addValue("message", value.getText().getContent());
				mapSqlParameterSource2.addValue("messagescore", value.getSentiment().getScore());

				sentences[index] = mapSqlParameterSource2;
				index = index + 1;
			}
			namedParameterJdbcTemplate.batchUpdate("INSERT INTO MESSAGES VALUES( :message, :messagescore, :num, :time)",
					sentences);
		}
		return 1;
	}

	public int insertData(FinalFeedBack finalFeedBackData) {
		String insertQuery = "INSERT INTO FEEDBACK (FEEDBACK_MESSAGE,VENDOR_UPI_ID,USER_PHN_NUM,FEEDBACK_TIME,SCORE,CATEGORY,INACCURATE) VALUES( :feedback , :vendorid , :userphone , :time , :score , :category , :inaccurate )";
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();

		mapSqlParameterSource.addValue("feedback", finalFeedBackData.getFeedback());
		mapSqlParameterSource.addValue("vendorid", finalFeedBackData.getVendorID());
		mapSqlParameterSource.addValue("userphone", finalFeedBackData.getUser_phone_number());
		mapSqlParameterSource.addValue("time", finalFeedBackData.getTime());
		mapSqlParameterSource.addValue("score", finalFeedBackData.getSentimentScore());
		mapSqlParameterSource.addValue("category", finalFeedBackData.getCategory());
		mapSqlParameterSource.addValue("inaccurate", finalFeedBackData.getIsInaccurate());

		KeyHolder keyHolder = new GeneratedKeyHolder();

		namedParameterJdbcTemplate2.update(insertQuery, mapSqlParameterSource, keyHolder);
		int primaryKey = (keyHolder.getKey().intValue());
		asyncDbService.batchMessageInsert(finalFeedBackData,primaryKey);
		return 1;
	}

	public int createVendor(int vendorID, String name) {
		MapSqlParameterSource newVendor = new MapSqlParameterSource();
		System.out.println("vendor id" + vendorID);
		newVendor.addValue("vendorid", vendorID);
		newVendor.addValue("vendorname", name);
		try {
			namedParameterJdbcTemplate.update("INSERT INTO VENDOR VALUES(:vendorid , :vendorname)", newVendor);
		} catch (Exception e) {
			System.out.println(e);
			return 2;
		}
		return 1;
	}

	public Vendor vendorAuth(int vendorID) {
		MapSqlParameterSource isVendor = new MapSqlParameterSource();
		isVendor.addValue("vendorid", vendorID);
		List<Vendor> myvendor = namedParameterJdbcTemplate.query("SELECT * FROM VENDOR WHERE VENDORID = :vendorid",
				isVendor, new VendorMapper());
		if (myvendor == null || myvendor.isEmpty())
			return null;

		return myvendor.get(0);
	}

	// NEW DB SERVICE APIS

	public User createUser(User user) {
		MapSqlParameterSource resource = new MapSqlParameterSource();
		resource.addValue("user_name", user.getUser_name());
		resource.addValue("user_phone_number", user.getUser_phone_number());
		resource.addValue("password", user.getPassword());
		namedParameterJdbcTemplate2.update(insertUserQuery, resource);
		return user;
	}

	public User userAuth(User user) {
		MapSqlParameterSource resource = new MapSqlParameterSource();
		resource.addValue("user_phone_number", user.getUser_phone_number());
		resource.addValue("password", user.getPassword());
		List<User> users = namedParameterJdbcTemplate2.query(authUserQuery, resource, new UserMapper());
		if (users.size() == 0)
			throw new UserDoesNotExistException("User does not exists");
		return users.get(0);
	}
	
	public Vendor vendorAuth(Long phoneNumber) {
		MapSqlParameterSource resource = new MapSqlParameterSource();
		resource.addValue("vendorphone", phoneNumber);
		
		List<Vendor> vendors = namedParameterJdbcTemplate2.query(authVendorQuery, resource, new VendorMapper());
		if (vendors.size() == 0)
			throw new VendorDoesNotExistsException("User does not exists");
		return vendors.get(0);
	}
	
	public boolean addVendor(Vendor vendor) {
		String query = "INSERT INTO VENDOR VALUES( :vendorname , :vendorphone , :vendorupi )";
		MapSqlParameterSource resource = new MapSqlParameterSource();
		resource.addValue("vendorname",vendor.getVendor_name());
		resource.addValue("vendorphone",vendor.getVendor_phone_number());
		resource.addValue("vendorupi",vendor.getVendor_upi());
		namedParameterJdbcTemplate2.update(query,resource);
		return true;
	}
	public List<Vendor> getAllVendors(){
		String query = "Select * from VENDOR";
		MapSqlParameterSource resource = new MapSqlParameterSource();
		List<Vendor> vendors = namedParameterJdbcTemplate2.query(query, resource, new VendorMapper());
		return vendors;		
	}
	
	public List<VendorFeebackData> getAllVendorFeedbacks(Long vendorPhone, int lastID) {
		MapSqlParameterSource resource = new MapSqlParameterSource();
		resource.addValue("vendorphone",vendorPhone);
		List<VendorFeebackData> list;
		if(lastID == 0) 
			{list = namedParameterJdbcTemplate2.query(getAllVendorFeedbacksQuery, resource, new VendorFeedbackMapper());
				
			}
		else {
			resource.addValue("id",lastID);
			list = namedParameterJdbcTemplate2.query(getOldUserFeedbacksQuery, resource, new VendorFeedbackMapper());
		}
		return list;	
	}
	
	public List<FeedbackData> getVendorFeedbacks(Long phone, int lastID) {
		List<VendorFeebackData> feedbacks = getAllVendorFeedbacks(phone,lastID);
		Map<Integer,FeedbackData> map = new LinkedHashMap<>();
		
		List<FeedbackData> dataList = new ArrayList<>();
		for(VendorFeebackData vfd:feedbacks) {
			FeedbackData fd;
			if(map.containsKey(vfd.getFeedback_id())) {
				fd = map.get(vfd.getFeedback_id());
			}
			else {
				fd = new FeedbackData();
				fd.setFeedback_id(vfd.getFeedback_id());
				fd.setTime(vfd.getTime());
				fd.setScore(vfd.getScore());
				fd.setInaccurate(vfd.getInaccurate());
				fd.setFeedback(vfd.getComplete_message());
				fd.setNegativeSentences(new ArrayList<>());
				fd.setPositiveSentences(new ArrayList<>());
				fd.setNeutralSentences(new ArrayList<>());			
				map.put(vfd.getFeedback_id(),fd);
			}
			Sentiment sentiment = new Sentiment();
			TextSpan textspan = new TextSpan();
			sentiment.setScore(vfd.getMessage_score());
			textspan.setContent(vfd.getMessage());
			double mscore = vfd.getMessage_score();
			Sentence sentence = new Sentence();
			
			sentence.setText(textspan);
			sentence.setSentiment(sentiment);
			
			if(mscore >= 0.3) fd.getPositiveSentences().add(sentence);
			else if(mscore >= -0.3) fd.getNeutralSentences().add(sentence);
			else fd.getNegativeSentences().add(sentence);
			
		}
		for(Map.Entry<Integer,FeedbackData> entry : map.entrySet()) {
			dataList.add(entry.getValue());
		}
		return dataList;
	}
	
	public int checkVendorUpi(String upi) {
		MapSqlParameterSource resource = new MapSqlParameterSource();
		resource.addValue("upi",upi);
		return namedParameterJdbcTemplate2.queryForList(checkVendorUpi,resource).size();
	}
	
	public List<Feedback> getUserFeedbacks(User user, int last_id) {
		MapSqlParameterSource resource = new MapSqlParameterSource();
		resource.addValue("phonenumber",user.getUser_phone_number());
		resource.addValue("id",last_id);
		return namedParameterJdbcTemplate2.query(getUserFeedbacksQuery,resource,new FeedbackMapper());
	}
}
