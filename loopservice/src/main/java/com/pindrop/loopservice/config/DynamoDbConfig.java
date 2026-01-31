package com.pindrop.loopservice.config;

import com.pindrop.loopservice.dto.JoinRequestItem;
import com.pindrop.loopservice.models.BaseItem;
import com.pindrop.loopservice.models.LoopItem;
import com.pindrop.loopservice.models.LoopMemberItem;
import com.pindrop.loopservice.models.UserLoopItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.dynamodb.loops-table}")
    private String tableName;

    /* ======================================================
       LOW-LEVEL CLIENT (for transactions)
       ====================================================== */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /* ======================================================
       ENHANCED CLIENT (for query/get)
       ====================================================== */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(
            DynamoDbClient dynamoDbClient) {

        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    /* ======================================================
       TABLE BEAN (THIS FIXES YOUR ERROR)
       ====================================================== */
    @Bean
    public DynamoDbTable<BaseItem> baseItem(
            DynamoDbEnhancedClient enhancedClient) {

        return enhancedClient.table(
                tableName,
                TableSchema.fromBean(BaseItem.class)
        );
    }

    @Bean
    public DynamoDbTable<LoopItem> loopTable(DynamoDbEnhancedClient client) {
        return client.table(tableName, TableSchema.fromBean(LoopItem.class));
    }

    @Bean
    public DynamoDbTable<LoopMemberItem> memberTable(DynamoDbEnhancedClient client) {
        return client.table(tableName, TableSchema.fromBean(LoopMemberItem.class));
    }

    @Bean
    public DynamoDbTable<UserLoopItem> userLoopTable(DynamoDbEnhancedClient client) {
        return client.table(tableName, TableSchema.fromBean(UserLoopItem.class));
    }

    @Bean
    public DynamoDbTable<JoinRequestItem> requestTable(DynamoDbEnhancedClient client) {
        return client.table(tableName, TableSchema.fromBean(JoinRequestItem.class));
    }
}
