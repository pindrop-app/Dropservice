package com.pindrop.signup.config;

import com.pindrop.tablestoreddb.DynamoTableStorageBuilder;
import com.pindrop.tablestoreddb.TableStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {

    @Value("${app.aws.region:us-east-2}")
    private String awsRegion;

    @Bean
    public TableStorage tableStorage() {
        return new DynamoTableStorageBuilder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
