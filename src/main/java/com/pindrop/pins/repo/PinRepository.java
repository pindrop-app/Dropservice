package com.pindrop.pins.repo;

import com.pindrop.pins.model.Pin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PinRepository {

    private final DynamoDbTable<Pin> table;

    public PinRepository(DynamoDbEnhancedClient enhancedClient,
                         @Value("${aws.dynamodb.pins-table:Pins}") String pinsTableName) {
        this.table = enhancedClient.table(pinsTableName, TableSchema.fromBean(Pin.class));
    }

    public void put(Pin pin) {
        table.putItem(pin);
    }

    public Pin getById(String pinId) {
        return table.getItem(Key.builder().partitionValue(pinId).build());
    }

    public List<Pin> listAll() {
        List<Pin> out = new ArrayList<>();
        table.scan().items().forEach(out::add);
        return out;
    }

    public List<Pin> listByUserId(String userId) {
        return scanWithEquals("userId", userId);
    }

    public List<Pin> listPinnedByUserId(String userId) {
        return scanWithEquals("pinnedByUserId", userId);
    }

    private List<Pin> scanWithEquals(String field, String value) {
        Expression filter = Expression.builder()
                .expression(field + " = :" + field)
                .putExpressionValue(":" + field, AttributeValue.builder().s(value).build())
                .build();

        ScanEnhancedRequest req = ScanEnhancedRequest.builder()
                .filterExpression(filter)
                .build();

        List<Pin> out = new ArrayList<>();
        table.scan(req).items().forEach(out::add);
        return out;
    }
}
