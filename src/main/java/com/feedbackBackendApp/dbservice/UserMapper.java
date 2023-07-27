package com.feedbackBackendApp.dbservice;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.feedbackBackendApp.responsedata.User;

public class UserMapper implements RowMapper<User>{
	@Override
	public User mapRow(ResultSet rs, int rowNum) throws SQLException {
		String user_name = rs.getString(1);	
		Long user_phone_number = rs.getLong(2);
		User user = new User();
		user.setUser_name(user_name);
		user.setUser_phone_number(user_phone_number);
		return user;
	}

} 
