package com.cafe.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.cafe.model.Bill;

public interface BillService {
	ResponseEntity<String> generateReport(Map<String, Object> requestMap);
	
	ResponseEntity<List<Bill>> getBills();
}
