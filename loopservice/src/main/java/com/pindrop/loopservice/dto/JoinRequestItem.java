package com.pindrop.loopservice.dto;

import com.pindrop.loopservice.models.BaseItem;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class JoinRequestItem extends BaseItem {
    private String userId;
    private String status;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

