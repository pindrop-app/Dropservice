package com.pindrop.signup.dao;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserDdbClient {

    private static final Logger log = LoggerFactory.getLogger(UserDdbClient.class);

    private final DynamoDbClient dynamoDbClient;

    private final UsersTableKeyResolver keyResolver;

    @Value("${app.tables.users:Users}")
    private String usersTable;

    public void putUser(String loginId, String email, String passwordHash, String dob, String createdAt) {
        // Ensure we have resolved the actual PK/SK attribute names for the Users table.
        keyResolver.ensureResolved();

        String pkName = keyResolver.getPartitionKeyName();
        String skName = keyResolver.getSortKeyName();

        // If the table is modeled as pk/sk, we commonly prefix the partition key.
        String pkValue = keyResolver.buildPartitionKeyValue(loginId);

        Map<String, AttributeValue> userItem = new HashMap<>();
        userItem.put(pkName, AttributeValue.fromS(pkValue));
        if (skName != null) {
            userItem.put(skName, AttributeValue.fromS(keyResolver.buildSortKeyValue()));
        }

        // Store both legacy + normalized attribute names to keep older code + UI happy.
        userItem.put("UserId", AttributeValue.fromS(loginId));
        userItem.put("userId", AttributeValue.fromS(loginId));
        userItem.put("Email", AttributeValue.fromS(email));
        userItem.put("email", AttributeValue.fromS(email));
        userItem.put("Password", AttributeValue.fromS(passwordHash));
        userItem.put("passwordHash", AttributeValue.fromS(passwordHash));
        userItem.put("DOB", AttributeValue.fromS(dob));
        userItem.put("dob", AttributeValue.fromS(dob));
        userItem.put("CreatedAt", AttributeValue.fromS(createdAt));
        userItem.put("createdAt", AttributeValue.fromS(createdAt));
        userItem.put("Status", AttributeValue.fromS("CREATED"));
        userItem.put("status", AttributeValue.fromS("CREATED"));

        PutItemRequest request = PutItemRequest.builder()
                .tableName(usersTable)
                .item(userItem)
                .conditionExpression(skName == null
                        ? String.format("attribute_not_exists(%s)", pkName)
                        : String.format("attribute_not_exists(%s) AND attribute_not_exists(%s)", pkName, skName))
                .build();

        dynamoDbClient.putItem(request);
    }
}
