package com.feedbackBackendApp.dbservice;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;


import com.feedbackBackendApp.responsedata.VendorFeebackData;

public class VendorFeedbackMapper implements RowMapper<VendorFeebackData>{

	@Override
	public VendorFeebackData mapRow(ResultSet rs, int rowNum) throws SQLException {
		VendorFeebackData vfd = new VendorFeebackData();
		vfd.setMessage(rs.getString(1));
		vfd.setMessage_score(rs.getDouble(2));
		vfd.setFeedback_id(rs.getInt(3));
		vfd.setComplete_message(rs.getString(4));
		vfd.setTime(rs.getDate(5));
		vfd.setScore(rs.getDouble(6));
		vfd.setInaccurate(rs.getInt(7));
		return vfd;
	}

}
