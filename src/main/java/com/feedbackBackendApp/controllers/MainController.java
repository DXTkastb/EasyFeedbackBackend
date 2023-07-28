package com.feedbackBackendApp.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.feedbackBackendApp.dbservice.DbService;
import com.feedbackBackendApp.responsedata.Feedback;
import com.feedbackBackendApp.responsedata.FeedbackData;
import com.feedbackBackendApp.responsedata.FeedbackStringData;
import com.feedbackBackendApp.responsedata.FinalFeedBack;
import com.feedbackBackendApp.responsedata.FinalFeedBackData;
import com.feedbackBackendApp.responsedata.User;
import com.feedbackBackendApp.responsedata.Vendor;
import com.feedbackBackendApp.responsedata.VendorFeebackData;
import com.feedbackBackendApp.services.ResponseGeneratorService;
import com.feedbackBackendApp.services.SseBroadcast;
import com.feedbackBackendApp.services.TokenService;

@CrossOrigin("*")
@Controller
public class MainController {

	private static final HttpHeaders mainHeaders = new HttpHeaders();
	static {
		mainHeaders.add("Access-Control-Allow-Headers", "*");
		mainHeaders.add("Access-Control-Expose-Headers", "*");
		mainHeaders.add("Content-type", " application/json");
	}

	@Autowired
	ResponseGeneratorService responsegenerator;

	@Autowired
	TokenService tokenService;

	@Autowired
	SseBroadcast sseBroadcast;

	@Autowired
	DbService dbservice;

	@PostMapping("/customer/feedbackToVendor")
	@ResponseBody
	public Boolean getResults(@RequestParam int vendorID, @RequestParam int orderNumber,
			@RequestBody FeedbackStringData data) throws InterruptedException {
		FinalFeedBackData response = responsegenerator.getResponse(data.getData(), orderNumber, vendorID);
		dbservice.insertData(response);// this is simple test db
		// sseBroadcast.broadcastToSession(vendorID, response);
		return true;
	}

	@GetMapping("/vendor/{vendorid}/db")
	@ResponseBody
	public List<FinalFeedBackData> getLastRecords(@RequestParam int numRecords, @PathVariable int vendorid) {

		List<FinalFeedBackData> dataList = dbservice.getOrders(vendorid, numRecords);

		return dataList;
	}

	//
	@Deprecated
	@RequestMapping("/vendor/sse")
	public SseEmitter sse(@RequestParam int vendorID) throws IIOException {
		System.out.println("REQUESTED SSE");
		SseEmitter sse = new SseEmitter(Long.MAX_VALUE);
		sse.onCompletion(() -> {
			System.out.println("Completed!");
		});

		sseBroadcast.newSseSession(vendorID, sse);
		return sse;
	}

	@PostMapping(value = "vendor/createvendor", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> createVendor(@RequestBody Vendor vendor) {

		int rowAffected = dbservice.createVendor(123456, "oscar");
		System.out.println("rows:" + rowAffected);
		if (rowAffected == 1) {
			return new ResponseEntity<Integer>(1, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<Integer>(2, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/vendor/auth/{vendorid}")
	public ResponseEntity<Vendor> authVendor(@PathVariable int vendorid) {
		Vendor vendor = dbservice.vendorAuth(vendorid);

		if (vendor == null) {
			return new ResponseEntity<Vendor>(vendor, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<Vendor>(vendor, HttpStatus.OK);
	}
	

	@PostMapping(value = "/check/upi")
	public ResponseEntity<Integer> checkUpi(@RequestBody String upi) {
		int count = dbservice.checkVendorUpi(upi);
		HttpStatusCode code = HttpStatus.OK;
		if(count == 0) code = HttpStatus.NOT_FOUND;
		return new ResponseEntity<>(count,code);
	}
	
	// add jwt token to headers
	@PostMapping(value = "/user/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<User> singupNewUser(@RequestBody User user) {
		User newUser = dbservice.createUser(user);
		HttpHeaders headers = new HttpHeaders();
		headers.add("auth-token", tokenService.generateTokenForUser(newUser.getUser_name(),newUser.getUser_phone_number()+""));
		return new ResponseEntity<>(newUser, headers, HttpStatus.OK);
	}

	// add jwt token to headers
	@PostMapping(value = "/user/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<User> authUser(@RequestBody User user) {
		User authUser = dbservice.userAuth(user);
		HttpHeaders headers = new HttpHeaders();
		headers.add("auth-token", tokenService.generateTokenForUser(authUser.getUser_name(),authUser.getUser_phone_number()+""));
		return new ResponseEntity<>(authUser, headers, HttpStatus.OK);
	}
	
	@PostMapping(value = "/check/user", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<User> checkUser(@RequestHeader("auth-token") String token) {
		User authUser = tokenService.generateUserFromToken(token);
		return new ResponseEntity<>(authUser,HttpStatus.OK);
	}


	@PostMapping(value = "/user/post/feedback")
	public ResponseEntity<String> postFeedback(@RequestParam(value= "phoneNum") long user_phone_number, @RequestParam(value = "upiLink") String vendor_upi_id,
			@RequestBody String feedback) {
		FinalFeedBack response = responsegenerator.getResponse(feedback, vendor_upi_id, user_phone_number);
		dbservice.insertData(response);
		return new ResponseEntity<>("", HttpStatus.OK);
	}
	
	@GetMapping(value = "/pffxx")
	public ResponseEntity<String> postFeedbackDummy() {
		String feedback = "These candies are a delightful treat! The flavors are rich and diverse, leaving a mouthwatering sensation with every bite. The texture is perfectly smooth, making them incredibly enjoyable to savor. A definite favorite for satisfying my sweet cravings!";
		FinalFeedBack response = responsegenerator.getResponse(feedback, "candyum@upi", 1122334455l);
		dbservice.insertData(response);
		return new ResponseEntity<>("DONE", HttpStatus.OK);
	}


	@PostMapping(value = "/vendor/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Vendor> singUpVendor(@RequestBody Vendor vendor) {
		dbservice.addVendor(vendor);
		HttpHeaders headers = new HttpHeaders();
		headers.addAll(mainHeaders);
		headers.add("auth-token",
				tokenService.generateTokenForVendor(vendor.getVendor_name(), vendor.getVendor_phone_number() + ""));
		vendor.setVendor_upi("");
		return new ResponseEntity<>(vendor, headers, HttpStatus.OK);
	}

	@PostMapping(value = "/vendor/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Vendor> loginVendor(@RequestBody Vendor vendor) {
		Vendor db_vendor = dbservice.vendorAuth(vendor.getVendor_phone_number());
		HttpHeaders headers = new HttpHeaders();
		headers.addAll(mainHeaders);
		headers.add("auth-token", tokenService.generateTokenForVendor(db_vendor.getVendor_name(),
				db_vendor.getVendor_phone_number() + ""));
		return new ResponseEntity<>(db_vendor,headers, HttpStatus.OK);
	}

	
	@GetMapping(value = "/vendor/feedbacks")
	@ResponseBody
	public List<FeedbackData> getVendorFeedbacks(@RequestHeader("auth-token") String token, @RequestParam Integer lastid) {
		Vendor vendor = tokenService.generateVendorFromToken(token);
		return dbservice.getVendorFeedbacks(vendor.getVendor_phone_number(),lastid);
	} 
	
	@GetMapping(value = "/vendor/auth")
	public ResponseEntity<Vendor> authoriseVendor(@RequestHeader("auth-token") String token) {
		Vendor vendor = tokenService.generateVendorFromToken(token);
		if(vendor == null) return new ResponseEntity<>(null,HttpStatus.UNAUTHORIZED);
		return new ResponseEntity<>(dbservice.vendorAuth(vendor.getVendor_phone_number()),HttpStatus.OK);		
	}
	
	@GetMapping(value = "/user/feedbacks")
	@ResponseBody
	public List<Feedback> getUserFeedbacks(@RequestHeader("auth-token") String token, @RequestParam Integer id) {
		System.out.println(token);
		System.out.println(id);
		User user = tokenService.generateUserFromToken(token);
		return dbservice.getUserFeedbacks(user,id);
	}

}
