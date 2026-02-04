package com.pindrop.signup.dao;

import com.pindrop.tablestoreddb.TableStorage;
import com.pindrop.tablestoreddb.model.PutRequest;
import com.pindrop.tablestoreddb.model.TableKey;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserDdbClient {

    private static final Logger log = LoggerFactory.getLogger(UserDdbClient.class);

    private final TableStorage tableStorage;

    @Value("${app.tables.users:Users}")
    private String usersTable;

    public void putUser(String loginId, String email, String passwordHash, String dob, String createdAt) {
        TableKey key = TableKey.builder()
                .tableName(usersTable)
                .partitionKey(loginId)
                .sortKey(email)
                .build();

        Map<String, Object> userItem = new HashMap<>();
        // Store both legacy + normalized attribute names
        userItem.put("loginId", loginId);
        userItem.put("email", email);
        userItem.put("passwordHash", passwordHash);
        userItem.put("dob", dob);
        userItem.put("createdAt", createdAt);
        userItem.put("status", "CREATED");

        PutRequest request = PutRequest.builder()
                .key(key)
                .item(userItem)
                .conditionExpression("attribute_not_exists(pk) AND attribute_not_exists(sk)")
                .build();

        log.info("Storing user in DDB. request={}", request);
        tableStorage.put(request);
    }
}
