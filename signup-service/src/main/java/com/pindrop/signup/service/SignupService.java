package com.pindrop.signup.service;

import com.pindrop.signup.dto.SignupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class SignupService {

    private static final Logger log = LoggerFactory.getLogger(SignupService.class);

    private final DynamoDbClient ddb;
    private final PasswordEncoder passwordEncoder;
    private final String usersTable;

    public SignupService(DynamoDbClient ddb,
                         PasswordEncoder passwordEncoder,
                         @Value("${app.tables.users:Users}") String usersTable) {
        this.ddb = ddb;
        this.passwordEncoder = passwordEncoder;
        this.usersTable = usersTable;
    }

    public String register(SignupRequest req) {
        String userId = req.getLoginId().trim();
        String createdAt = Instant.now().toString();

        // Never log raw passwords.
        log.info("signup attempt userId={} dob={} displayNamePresent={}",
                userId,
                safe(req.getDob()),
                req.getDisplayName() != null && !req.getDisplayName().isBlank());

        Map<String, AttributeValue> item = new HashMap<>();
        // Your DynamoDB Users table is keyed by "Email" (case-sensitive).
        // In this service we treat loginId as the email.
        item.put("Email", AttributeValue.fromS(userId));
        item.put("email", AttributeValue.fromS(userId));

        // Keep legacy attribute for any older code/UI that still reads UserId.
        item.put("UserId", AttributeValue.fromS(userId));
        item.put("userId", AttributeValue.fromS(userId));
        item.put("Password", AttributeValue.fromS(passwordEncoder.encode(req.getPassword())));
        item.put("passwordHash", item.get("Password"));
        item.put("DOB", AttributeValue.fromS(req.getDob()));
        item.put("dob", item.get("DOB"));
        item.put("CreatedAt", AttributeValue.fromS(createdAt));
        item.put("createdAt", item.get("CreatedAt"));
        item.put("Status", AttributeValue.fromS("ACTIVE"));
        item.put("status", item.get("Status"));
        if (req.getDisplayName() != null && !req.getDisplayName().isBlank()) {
            item.put("DisplayName", AttributeValue.fromS(req.getDisplayName().trim()));
            item.put("displayName", item.get("DisplayName"));
        }

        PutItemRequest put = PutItemRequest.builder()
                .tableName(usersTable)
                .item(item)
                // prevent overwrite if user exists
                .conditionExpression("attribute_not_exists(Email)")
                .build();

        try {
            ddb.putItem(put);
            log.info("signup success userId={}", userId);
            return createdAt;
        } catch (ConditionalCheckFailedException e) {
            log.warn("signup conflict userId={}", userId);
            throw new IllegalArgumentException("User already exists");
        }
    }

    private static String safe(String v) {
        return v == null ? "null" : v;
    }
}
