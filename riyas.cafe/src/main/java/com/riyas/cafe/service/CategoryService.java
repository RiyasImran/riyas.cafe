package com.riyas.cafe.service;

import com.riyas.cafe.models.Category;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface CategoryService {
    ResponseEntity<String> addNewCategory(Map<String,String> requestMap);

    ResponseEntity<String> update(Map<String,String> requestMap);

    ResponseEntity<List<Category>> getAllCategory(String filterValue);
}