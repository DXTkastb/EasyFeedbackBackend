package com.feedbackBackendApp.dbservice;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.feedbackBackendApp.responsedata.FinalFeedBackData;

public class OrdersMapper implements RowMapper<FinalFeedBackData>{

	@Override
	public FinalFeedBackData mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		FinalFeedBackData orders = new FinalFeedBackData();
		int num = rs.getInt(1);
		int time = rs.getInt(2);
		String numtime=num+ ""+time;
		orders.setNum(num);
		orders.setTime(time);
		orders.setSentimentScore(rs.getDouble(3));
		orders.setCategory(rs.getString(4));
		orders.setFeedback(rs.getString(5));
		orders.setVendorID(rs.getInt(6));
		orders.setIsInaccurate(rs.getInt(7));
		
		
		// TODO Auto-generated method stub
		return orders;
	}

}
