package com.feedbackBackendApp.dbservice;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.feedbackBackendApp.responsedata.Vendor;

public class VendorMapper implements RowMapper<Vendor> {

	@Override
	public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
		Vendor vendor = new Vendor();
		vendor.setVendor_name(rs.getString(1));
		vendor.setVendor_phone_number(rs.getLong(2));
		return vendor;
	}
}
