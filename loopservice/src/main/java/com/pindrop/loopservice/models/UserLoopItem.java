package com.pindrop.loopservice.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class UserLoopItem extends BaseItem {

    private String role; // CREATOR / OWNER / MEMBER

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

