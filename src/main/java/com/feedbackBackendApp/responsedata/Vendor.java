package com.feedbackBackendApp.responsedata;

import lombok.Data;

@Data
public class Vendor {

	String vendor_name;
	Long vendor_phone_number;
	String vendor_upi;
	
	public static Vendor dummy() {
		Vendor x = new Vendor();
		x.setVendor_name("LightCone");
		x.setVendor_phone_number(8497561238l);
		x.setVendor_upi("lightcones@okbank");
		return x;
	}
}
