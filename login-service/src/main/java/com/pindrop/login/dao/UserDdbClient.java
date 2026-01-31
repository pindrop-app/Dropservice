package com.pindrop.login.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@Component
public class UserDdbClient {

    private static final Logger log = LoggerFactory.getLogger(UserDdbClient.class);

    private static final String DEFAULT_USERS_TABLE = "Users";
    private static final String EMAIL_INDEX = "EmailIndex";

    private final DynamoDbClient dynamoDb;

    public UserDdbClient(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    // ✅ Keeps your existing service call working
    public Map<String, AttributeValue> getUser(String loginId) {
        return getUser(DEFAULT_USERS_TABLE, loginId);
    }

    // ✅ Main method used by DAO/service
    public Map<String, AttributeValue> getUser(String tableName, String loginId) {

        // Attempt 1: direct GetItem with pk+sk using loginId for both.
        // Works if you store UserId=email during signup (common dev shortcut).
        Map<String, AttributeValue> directKey = new HashMap<>();
        directKey.put("UserId", AttributeValue.builder().s(loginId).build());
        directKey.put("Email", AttributeValue.builder().s(loginId).build());

        Map<String, AttributeValue> direct = tryGetItem(tableName, directKey);
        if (!direct.isEmpty()) return direct;

        // Attempt 2: Use GSI EmailIndex to find the actual keys
        Map<String, AttributeValue> fromIndex = tryQueryByEmailIndex(tableName, loginId);
        if (fromIndex.isEmpty()) return Map.of();

        String userId = s(fromIndex, "UserId");
        String email = s(fromIndex, "Email");

        if (userId.isBlank() || email.isBlank()) {
            log.warn("EmailIndex returned item missing keys. keysPresent={}", fromIndex.keySet());
            return Map.of();
        }

        Map<String, AttributeValue> realKey = new HashMap<>();
        realKey.put("UserId", AttributeValue.builder().s(userId).build());
        realKey.put("Email", AttributeValue.builder().s(email).build());

        return tryGetItem(tableName, realKey);
    }

    private Map<String, AttributeValue> tryGetItem(String tableName, Map<String, AttributeValue> key) {
        long start = System.currentTimeMillis();
        try {
            GetItemResponse res = dynamoDb.getItem(GetItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .consistentRead(false)
                    .build());

            int attrs = res.hasItem() ? res.item().size() : 0;
            log.debug("dynamodb getItem table={} keyAttrs={} resultAttrs={} tookMs={}",
                    tableName, key.keySet(), attrs, System.currentTimeMillis() - start);

            if (res.hasItem() && res.item() != null && !res.item().isEmpty()) {
                return res.item();
            }
            return Map.of();

        } catch (DynamoDbException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("provided key element does not match the schema")) {
                log.warn("schema mismatch for getItem table={} keyAttrs={} tookMs={}",
                        tableName, key.keySet(), System.currentTimeMillis() - start);
                return Map.of();
            }
            log.error("dynamodb getItem failed table={} keyAttrs={}", tableName, key.keySet(), e);
            throw e;
        }
    }

    private Map<String, AttributeValue> tryQueryByEmailIndex(String tableName, String email) {
        long start = System.currentTimeMillis();
        try {
            QueryResponse res = dynamoDb.query(QueryRequest.builder()
                    .tableName(tableName)
                    .indexName(EMAIL_INDEX)
                    .keyConditionExpression("Email = :e")
                    .expressionAttributeValues(Map.of(
                            ":e", AttributeValue.builder().s(email).build()
                    ))
                    .limit(1)
                    .build());

            int count = res.count() == null ? 0 : res.count();
            log.debug("dynamodb query index={} table={} email={} count={} tookMs={}",
                    EMAIL_INDEX, tableName, email, count, System.currentTimeMillis() - start);

            List<Map<String, AttributeValue>> items = res.items();
            if (items == null || items.isEmpty()) return Map.of();
            return items.get(0);

        } catch (DynamoDbException e) {
            log.error("dynamodb query EmailIndex failed table={} email={}", tableName, email, e);
            throw e;
        }
    }

    private String s(Map<String, AttributeValue> item, String key) {
        AttributeValue v = item.get(key);
        if (v == null || v.s() == null) return "";
        return v.s();
    }
}
