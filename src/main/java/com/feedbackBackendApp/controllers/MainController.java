package com.feedbackBackendApp.controllers;

import java.util.List;
import javax.imageio.IIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.feedbackBackendApp.dbservice.DbService;
import com.feedbackBackendApp.responsedata.FeedbackStringData;
import com.feedbackBackendApp.responsedata.FinalFeedBackData;
import com.feedbackBackendApp.responsedata.Vendor;
import com.feedbackBackendApp.services.ResponseGeneratorService;
import com.feedbackBackendApp.services.SseBroadcast;

@CrossOrigin("*")
@Controller
public class MainController {
	// static final String content = "Pasta was delicious. It was creamy and had
	// just the right amount of cheese. I order pasta frequently and I am never
	// disappointed.The ambience had a positive vibe and other customers also looked
	// satisfied. The only thing which I disliked was Green salad";

	@Autowired
	ResponseGeneratorService responsegenerator;

	@Autowired
	SseBroadcast sseBroadcast;

	@Autowired
	DbService dbservice;

	@PostMapping("/customer/feedbackToVendor")
	@ResponseBody
	public Boolean getResults(@RequestParam int vendorID, @RequestParam int orderNumber,
			@RequestBody FeedbackStringData data) throws InterruptedException {

		FinalFeedBackData response = responsegenerator.getResponse(data.getData(), orderNumber, vendorID); // fetch
																											// google ai
																											// results
		dbservice.insertData(response);// this is simple test db
		sseBroadcast.broadcastToSession(vendorID, response);
		return true;
	}

	@GetMapping("/vendor/{vendorid}/db")
	@ResponseBody
	public List<FinalFeedBackData> getLastRecords(@RequestParam int numRecords, @PathVariable int vendorid) {

		List<FinalFeedBackData> dataList = dbservice.getOrders(vendorid, numRecords);

		return dataList;
	}

	//
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

		int rowAffected = dbservice.createVendor(vendor.getVendorID(), vendor.getVendorName());
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

}
