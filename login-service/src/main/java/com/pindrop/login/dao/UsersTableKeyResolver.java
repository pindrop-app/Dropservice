package com.pindrop.login.dao;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;

import java.util.Locale;
import java.util.Optional;

/**
 * Resolves the DynamoDB key schema for the Users table at runtime.
 *
 * Why: different environments often use different key attribute names (UserId vs userId vs pk/sk).
 * This class makes login/signup resilient by discovering the actual key schema.
 */
@Slf4j
@Component
public class UsersTableKeyResolver {

    private final DynamoDbClient dynamoDbClient;

    @Value("${app.tables.users:Users}")
    private String usersTable;

    @Value("${app.users.pkPrefix:USER#}")
    private String pkPrefix;

    @Value("${app.users.skValue:PROFILE}")
    private String skValue;

    @Getter
    private volatile String partitionKeyName;

    @Getter
    private volatile String sortKeyName;

    public UsersTableKeyResolver(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void ensureResolved() {
        if (partitionKeyName != null) return;
        synchronized (this) {
            if (partitionKeyName != null) return;
            try {
                DescribeTableResponse resp = dynamoDbClient.describeTable(
                        DescribeTableRequest.builder().tableName(usersTable).build()
                );

                Optional<KeySchemaElement> pk = resp.table().keySchema().stream()
                        .filter(k -> k.keyType() == KeyType.HASH)
                        .findFirst();
                Optional<KeySchemaElement> sk = resp.table().keySchema().stream()
                        .filter(k -> k.keyType() == KeyType.RANGE)
                        .findFirst();

                this.partitionKeyName = pk.map(KeySchemaElement::attributeName).orElse("UserId");
                this.sortKeyName = sk.map(KeySchemaElement::attributeName).orElse(null);

                log.info("Resolved Users table key schema: pk='{}' sk='{}' table='{}'",
                        partitionKeyName, sortKeyName, usersTable);
            } catch (Exception e) {
                // Fallback: common schema
                this.partitionKeyName = "UserId";
                this.sortKeyName = null;
                log.warn("Could not describe DynamoDB table '{}' to resolve key schema. Falling back to pk='{}'. Root cause: {}",
                        usersTable, partitionKeyName, e.getMessage());
            }
        }
    }

    public String buildPartitionKeyValue(String loginId) {
        ensureResolved();
        String pkLower = partitionKeyName.toLowerCase(Locale.ROOT);
        if (pkLower.equals("pk") || pkLower.equals("partitionkey")) {
            return pkPrefix + loginId;
        }
        return loginId;
    }

    public String buildSortKeyValue() {
        ensureResolved();
        return skValue;
    }
}
