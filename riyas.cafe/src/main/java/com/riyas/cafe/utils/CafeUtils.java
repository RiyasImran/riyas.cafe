package com.riyas.cafe.utils;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public class CafeUtils {
    private CafeUtils(){}

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus){
        return new ResponseEntity<String>("{\"message\":\""+responseMessage+"\"}",httpStatus);
    }

    public static String getUUID(){
        Date date = new Date();
        long time = date.getTime();
        return " bill-" + time;
    }

    public static Map<String, Object> getJson(String data){
        if (!Strings.isNullOrEmpty(data)){
            return new Gson().fromJson(data, new TypeToken<Map<String, Object>>(){}.getType());
        } else
            return new HashMap<>();
    }

    public static Boolean isFileExist(String path){
        log.info("insde isFileExist {}", path);
        try{
            File file = new File(path);
            return (file != null && file.exists()) ? Boolean.TRUE : Boolean.FALSE;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
}
