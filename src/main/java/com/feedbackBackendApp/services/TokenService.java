package com.feedbackBackendApp.services;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import org.springframework.stereotype.Service;

import com.feedbackBackendApp.responsedata.User;
import com.feedbackBackendApp.responsedata.Vendor;

@Service
public class TokenService {
	public static final String keyCode = "r834t8172ryn4xn902xfm0aa3eduhjfu789nwrhf029xmf0uk20as3u894yjur9";
	private static final Key key = Keys.hmacShaKeyFor(keyCode.getBytes());

	public String generateTokenForUser(String username, String phoneNumber) {
		String token =
				Jwts.builder()
						.claim("username", username)
						.claim("phoneNumber", phoneNumber)
						.signWith(key)			
						.compact();
				return token;
	}
	
	public String generateTokenForVendor(String vendorName, String phoneNumber) {
		String token =
				Jwts.builder()
						.claim("vendorName", vendorName)
						.claim("phoneNumber", phoneNumber)
						.signWith(key)			
						.compact();
				return token;
	}
	
	public User generateUserFromToken(String jws) {
		User user = null;
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws).getBody();		
			String phoneNumber = claims.get("phoneNumber", String.class);
			String username = claims.get("username",String.class);
			user = new User();
			user.setUser_name(username);
			user.setUser_phone_number(Long.parseLong(phoneNumber));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return user;
	}
	
	public Vendor generateVendorFromToken(String jws) {
		Vendor vendor = null;
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws).getBody();	
			String vendorName = claims.get("vendorName", String.class);
			String phoneNumber = claims.get("phoneNumber", String.class);
			vendor = new Vendor();
			vendor.setVendor_name(vendorName);
			vendor.setVendor_phone_number(Long.parseLong(phoneNumber));
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return vendor;
	}
}
