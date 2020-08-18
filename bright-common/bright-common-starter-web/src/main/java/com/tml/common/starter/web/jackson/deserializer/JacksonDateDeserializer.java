package com.tml.common.starter.web.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tml.common.starter.web.jackson.converter.StringToDateUtil;


import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * Jackson Date序列化器
 * </p>
 *
 * @author TuMingLong
 * @date 2020-04-02
 */
public class JacksonDateDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String date = jp.getText();
        return StringToDateUtil.convert(date);
    }

}
