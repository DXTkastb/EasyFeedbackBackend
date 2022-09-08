package com.feedbackBackendApp.dbservice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feedbackBackendApp.responsedata.FinalFeedBackData;
import com.feedbackBackendApp.responsedata.Sentence;
import com.feedbackBackendApp.responsedata.Vendor;

/*

FULL QUERY : 

SELECT * FROM (SELECT * FROM (VENDOR INNER JOIN ORDERS USING (VENDORID)) LIMIT 10) VENDORORDERS JOIN MESSAGES USING (ORDERNUMBER,ORDERTIME);

*/

@Service
public class DbService {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public DbService(@Qualifier("dbsource") DataSource datasource) {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);

	}

	public List<FinalFeedBackData> getOrders(int vendorID, int limit) {
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		mapSqlParameterSource.addValue("id", vendorID);
		mapSqlParameterSource.addValue("limit", limit);
		List<FinalFeedBackData> ordersList = namedParameterJdbcTemplate.query(
				"SELECT * FROM  ORDERS WHERE VENDORID = :id LIMIT :limit;", mapSqlParameterSource, new OrdersMapper());

		for (FinalFeedBackData orders : ordersList) {

			orders.setSentences(getRecordsForEachOrder(orders.getNum(), orders.getTime()));
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

}
