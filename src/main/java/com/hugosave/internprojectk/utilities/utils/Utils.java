package com.hugosave.internprojectk.utilities.utils;

import com.google.protobuf.Timestamp;
import com.hugosave.intern.project.proto.AssetPriceEntityList;
import com.hugosave.intern.project.proto.RealTimePriceList;
import com.hugosave.internprojectk.constants.ConfigConstants;
import com.hugosave.internprojectk.utilities.utils.exception.CustomException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String getUserId(String authorizationHeader) {
        if(Objects.equals(authorizationHeader, "") || authorizationHeader == null){
            throw new CustomException(ExceptionStatusCode.UNAUTHORIZED_404);
        }
        String token = authorizationHeader.replace(String.format("%s ", ConfigConstants.BEARER_TOKEN), "");

        try {
            JWTClaimsSet jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
            return jwtClaimsSet.getSubject();
        } catch (ParseException e) {
            logger.error("Error decoding JWT", e);
            throw new CustomException(ExceptionStatusCode.UNAUTHORIZED_404);
        }
    }

    public static String generatePageToken(Timestamp date) {
        try {
            String dateStr = date.toString();
            byte[] encodedBytes = Base64.getEncoder().encode(dateStr.getBytes(StandardCharsets.UTF_8));
            return new String(encodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Error generating page token", e);
            throw new RuntimeException("Error generating page token", e);
        }
    }

    public static String convertToTimestamp(String input) {
        try {
            long seconds = Long.parseLong(input.split(":")[1].trim());
            Instant instant = Instant.ofEpochSecond(seconds);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ConfigConstants.TIMESTAMP_PATTERN);
            return localDateTime.format(formatter);
        } catch (Exception e) {
            logger.error("Error converting timestamp", e);
            throw new RuntimeException("Error converting timestamp", e);
        }
    }
}
