package com.pindrop.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class DDBClient {

    private static final String USERS_TABLE = "Users";

    private final DynamoDbClient dynamoDbClient;

    public void putUser(String loginId, String email, String passwordHash, String dob, String createdAt) {

        Map<String, AttributeValue> userItem = new HashMap<>();
        userItem.put("UserId", AttributeValue.fromS(loginId));
        userItem.put("Email", AttributeValue.fromS(email));
        userItem.put("Password", AttributeValue.fromS(passwordHash));
        userItem.put("DOB", AttributeValue.fromS(dob));
        userItem.put("CreatedAt", AttributeValue.fromS(createdAt));
        userItem.put("Status", AttributeValue.fromS("CREATED"));

        PutItemRequest request = PutItemRequest.builder()
                .tableName(USERS_TABLE)
                .item(userItem)
                // prevents overwriting if loginId already exists
                .conditionExpression("attribute_not_exists(UserId)")
                .build();

        dynamoDbClient.putItem(request);
    }

}
