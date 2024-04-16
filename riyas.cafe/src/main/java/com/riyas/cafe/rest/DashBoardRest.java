package com.riyas.cafe.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;

@RequestMapping(path = "/dashboard")
public interface DashBoardRest {
    @GetMapping(path = "/getcount")
    ResponseEntity<Map<String,Object>> getCount ();
}
