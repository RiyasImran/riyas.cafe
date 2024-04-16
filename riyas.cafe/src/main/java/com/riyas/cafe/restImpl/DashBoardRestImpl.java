package com.riyas.cafe.restImpl;

import com.riyas.cafe.rest.DashBoardRest;
import com.riyas.cafe.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
public class DashBoardRestImpl implements DashBoardRest {
    @Autowired
    DashBoardService dashBoardService;
    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
        try{
            return dashBoardService.getCount();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
