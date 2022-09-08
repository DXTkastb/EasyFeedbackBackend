package com.feedbackBackendApp.dbservice;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.feedbackBackendApp.responsedata.Vendor;

public class VendorMapper implements RowMapper<Vendor> {

	@Override
	public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
	
		String nameString = rs.getString(2);
		
		if(nameString == null)
			return null;
		
		System.out.print(nameString);
		
		Vendor vendor = new Vendor();
		vendor.setVendorID(rs.getInt("VENDORID"));
		vendor.setVendorName(nameString);
		return vendor;
	}
	
	

}
