package com.skillbox.utils;

import com.skillbox.web.exception.DateTimeFormatException;

import java.time.Instant;

public class DateTimeUtils {
    private DateTimeUtils(){
    }

    public static Instant parseDate(String date){
        if(date == null || date.trim().isEmpty()){
            return null;
        }

        try{
            return Instant.parse(date);
        }catch (Exception e){
            throw new DateTimeFormatException("Системная ошибка! Формат даты в фильтрах не соответствует нужному!");
        }
    }
}
