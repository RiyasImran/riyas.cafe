package com.riyas.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.riyas.cafe.JWT.JwtFilter;
import com.riyas.cafe.constants.CafeConstants;
import com.riyas.cafe.dao.CategoryDao;
import com.riyas.cafe.models.Category;
import com.riyas.cafe.service.CategoryService;
import com.riyas.cafe.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try{
            if (jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap, false)) {
                    categoryDao.save(getCategoryFromMap(requestMap,false));
                    return CafeUtils.getResponseEntity("Category Added Successfully", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.BAD_REQUEST);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try{
            if (jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap, true)){
                    Optional optional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));
                    if (!optional.isEmpty()){
                        categoryDao.save(getCategoryFromMap(requestMap, true));
                        return CafeUtils.getResponseEntity("Category updated successfully", HttpStatus.OK);
                    } else
                        return CafeUtils.getResponseEntity("Category id doesn't exist", HttpStatus.OK);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.BAD_REQUEST);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try{
            if (!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")) {
                log.info("inside getAllCategory filterValue");
                return new ResponseEntity<>(categoryDao.getAllCategory(), HttpStatus.OK);
            }
            return new ResponseEntity<>(categoryDao.findAll(), HttpStatus.OK);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")){
            if (requestMap.containsKey("id") && validateId)
                return true;
            else if (!validateId)
                return true;
        }
        return false;
    }

    private Category getCategoryFromMap(Map<String, String> requestMap, Boolean isAdd){
        Category category = new Category();
        if (isAdd){
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }
}
