package com.riyas.cafe.serviceImpl;

import com.riyas.cafe.dao.BillDao;
import com.riyas.cafe.dao.CategoryDao;
import com.riyas.cafe.dao.ProductDao;
import com.riyas.cafe.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashBoardServiceImpl implements DashBoardService {
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    ProductDao productDao;
    @Autowired
    BillDao billDao;
    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
            Map<String,Object> map = new HashMap<>();
            map.put("product", productDao.count());
            map.put("category", categoryDao.count());
            map.put("bill", billDao.count());
            return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
