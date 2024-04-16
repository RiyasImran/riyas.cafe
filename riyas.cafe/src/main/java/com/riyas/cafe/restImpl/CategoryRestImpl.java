package com.riyas.cafe.restImpl;

import com.riyas.cafe.constants.CafeConstants;
import com.riyas.cafe.models.Category;
import com.riyas.cafe.rest.CategoryRest;
import com.riyas.cafe.service.CategoryService;
import com.riyas.cafe.utils.CafeUtils;
import com.riyas.cafe.wrapper.UserWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CategoryRestImpl implements CategoryRest {
    @Autowired
    CategoryService categoryService;
    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try{
            return categoryService.addNewCategory(requestMap);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try{
            return categoryService.update(requestMap);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try{
            return categoryService.getAllCategory(filterValue);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;//CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
