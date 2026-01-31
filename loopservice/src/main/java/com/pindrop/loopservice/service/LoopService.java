package com.pindrop.loopservice.service;

import com.pindrop.loopservice.dto.JoinRequestItem;
import com.pindrop.loopservice.models.*;
import com.pindrop.loopservice.util.KeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class LoopService {

    private final DynamoDbClient dynamoDbClient;

    private final DynamoDbTable<LoopItem> loopTable;
    private final DynamoDbTable<LoopMemberItem> memberTable;
    private final DynamoDbTable<UserLoopItem> userLoopTable;
    private final DynamoDbTable<JoinRequestItem> requestTable;

    @Value("${aws.dynamodb.loops-table}")
    private String tableName;

    /* ================= DISCOVER ================= */

    public List<LoopItem> discoverLoops() {
        QueryConditional qc =
                QueryConditional.keyEqualTo(
                        Key.builder().partitionValue("DISCOVER").build()
                );

        List<LoopItem> result = new ArrayList<>();
        loopTable.index("GSI_DISCOVER")
                .query(qc)
                .forEach(page -> result.addAll(page.items()));
        return result;
    }

    /* ================= MY LOOPS ================= */

    public List<LoopItem> getMyLoops(String userId) {
        QueryConditional qc =
                QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(KeyUtil.userPk(userId)).build()
                );

        List<UserLoopItem> mappings =
                userLoopTable.query(qc).items().stream().toList();

        return mappings.stream()
                .map(m -> {
                    String loopId = m.getSk().replace("LOOP#", "");
                    return loopTable.getItem(
                            Key.builder()
                                    .partitionValue(KeyUtil.loopPk(loopId))
                                    .sortValue("META")
                                    .build()
                    );
                })
                .toList();
    }

    /* ================= CREATE LOOP ================= */

    public void createLoop(String userId, String name, String description, boolean isPrivate) {

        String loopId = UUID.randomUUID().toString().substring(0, 6);
        String now = Instant.now().toString();

        LoopItem loop = new LoopItem();
        loop.setPk(KeyUtil.loopPk(loopId));
        loop.setSk("META");
        loop.setLoopId(loopId);
        loop.setName(name);
        loop.setDescription(description);
        loop.setPrivate(isPrivate);
        loop.setCreatorId(userId);
        loop.setMemberCount(1);
        loop.setCreatedAt(now);
        loop.setGsi1pk("DISCOVER");
        loop.setGsi1sk(loopId);

        LoopMemberItem creator = new LoopMemberItem();
        creator.setPk(KeyUtil.loopPk(loopId));
        creator.setSk(KeyUtil.memberSk(userId));
        creator.setUserId(userId);
        creator.setRole("CREATOR");
        creator.setJoinedAt(now);

        UserLoopItem userLoop = new UserLoopItem();
        userLoop.setPk(KeyUtil.userPk(userId));
        userLoop.setSk(KeyUtil.userLoopSk(loopId));
        userLoop.setRole("CREATOR");

        loopTable.putItem(loop);
        memberTable.putItem(creator);
        userLoopTable.putItem(userLoop);
    }

    /* ================= JOIN OR REQUEST ================= */

    public void joinOrRequest(String loopId, String userId) {

        LoopItem loop = loopTable.getItem(
                Key.builder()
                        .partitionValue(KeyUtil.loopPk(loopId))
                        .sortValue("META")
                        .build()
        );

        if (!loop.getPrivate()) {
            joinTxn(loopId, userId);
        } else {
            JoinRequestItem req = new JoinRequestItem();
            req.setPk(KeyUtil.loopPk(loopId));
            req.setSk("REQUEST#USER#" + userId);
            req.setUserId(userId);
            req.setStatus("PENDING");
            requestTable.putItem(req);
        }
    }

    public void acceptRequest(String loopId, String userId) {
        joinTxn(loopId, userId);
        requestTable.deleteItem(
                Key.builder()
                        .partitionValue(KeyUtil.loopPk(loopId))
                        .sortValue("REQUEST#USER#" + userId)
                        .build()
        );
    }

    public void rejectRequest(String loopId, String userId) {
        requestTable.deleteItem(
                Key.builder()
                        .partitionValue(KeyUtil.loopPk(loopId))
                        .sortValue("REQUEST#USER#" + userId)
                        .build()
        );
    }

    /* ================= TRANSACTIONAL JOIN / LEAVE ================= */

    private void joinTxn(String loopId, String userId) {
        dynamoDbClient.transactWriteItems(
                TransactWriteItemsRequest.builder()
                        .transactItems(
                                putMember(loopId, userId),
                                putUserLoop(loopId, userId),
                                incrementMemberCount(loopId, 1)
                        )
                        .build()
        );
    }

    public void leaveLoop(String loopId, String userId) {
        dynamoDbClient.transactWriteItems(
                TransactWriteItemsRequest.builder()
                        .transactItems(
                                deleteItem(KeyUtil.loopPk(loopId), KeyUtil.memberSk(userId)),
                                deleteItem(KeyUtil.userPk(userId), KeyUtil.userLoopSk(loopId)),
                                incrementMemberCount(loopId, -1)
                        )
                        .build()
        );
    }

    /* ================= INVITES ================= */

    public List<JoinRequestItem> getMyInvites(String userId) {
        QueryConditional qc =
                QueryConditional.sortBeginsWith(
                        Key.builder()
                                .partitionValue(KeyUtil.userPk(userId))
                                .sortValue("INVITE#")
                                .build()
                );

        return requestTable.query(qc).items().stream().toList();
    }

    public void sendInvite(String loopId, String senderId, String targetUserId) {
        Map<String, AttributeValue> invite = Map.of(
                "pk", AttributeValue.fromS(KeyUtil.userPk(targetUserId)),
                "sk", AttributeValue.fromS("INVITE#LOOP#" + loopId),
                "loopId", AttributeValue.fromS(loopId),
                "senderId", AttributeValue.fromS(senderId),
                "status", AttributeValue.fromS("PENDING"),
                "sentAt", AttributeValue.fromS(Instant.now().toString())
        );

        dynamoDbClient.putItem(
                PutItemRequest.builder()
                        .tableName(tableName)
                        .item(invite)
                        .build()
        );
    }

    public void acceptInvite(String loopId, String userId) {
        // 1. Join loop
        joinTxn(loopId, userId);

        // 2. Delete invite
        requestTable.deleteItem(
                Key.builder()
                        .partitionValue(KeyUtil.userPk(userId))
                        .sortValue("INVITE#LOOP#" + loopId)
                        .build()
        );
    }


    public void rejectInvite(String loopId, String userId) {
        requestTable.deleteItem(
                Key.builder()
                        .partitionValue(KeyUtil.userPk(userId))
                        .sortValue("INVITE#LOOP#" + loopId)
                        .build()
        );
    }
    /* ================= MEMBERS ================= */

    public List<LoopMemberItem> getMembers(String loopId) {
        QueryConditional qc =
                QueryConditional.sortBeginsWith(
                        Key.builder()
                                .partitionValue(KeyUtil.loopPk(loopId))
                                .sortValue("MEMBER#")
                                .build()
                );

        return memberTable.query(qc).items().stream().toList();
    }

    public void promoteToOwner(String loopId, String targetUserId, String actorUserId) {
        validateOwner(loopId, actorUserId);
        LoopMemberItem member = getMember(loopId, targetUserId);
        if (!"CREATOR".equals(member.getRole())) {
            member.setRole("OWNER");
            memberTable.putItem(member);
        }
    }

    public void demoteOwner(String loopId, String targetUserId, String actorUserId) {
        validateOwner(loopId, actorUserId);
        LoopMemberItem member = getMember(loopId, targetUserId);
        if ("CREATOR".equals(member.getRole())) {
            throw new IllegalStateException("Creator cannot be demoted");
        }
        member.setRole("MEMBER");
        memberTable.putItem(member);
    }

    /* ================= HELPERS ================= */

    private LoopMemberItem getMember(String loopId, String userId) {
        LoopMemberItem member =
                memberTable.getItem(
                        Key.builder()
                                .partitionValue(KeyUtil.loopPk(loopId))
                                .sortValue(KeyUtil.memberSk(userId))
                                .build()
                );

        if (member == null) {
            throw new IllegalStateException("User is not a member");
        }
        return member;
    }

    private void validateOwner(String loopId, String userId) {
        LoopMemberItem actor = getMember(loopId, userId);
        if (!"CREATOR".equals(actor.getRole()) &&
                !"OWNER".equals(actor.getRole())) {
            throw new SecurityException("Not authorized");
        }
    }

    /* ================= TRANSACTION BUILDERS ================= */

    private TransactWriteItem putMember(String loopId, String userId) {
        return TransactWriteItem.builder()
                .put(Put.builder()
                        .tableName(tableName)
                        .item(Map.of(
                                "pk", AttributeValue.fromS(KeyUtil.loopPk(loopId)),
                                "sk", AttributeValue.fromS(KeyUtil.memberSk(userId)),
                                "userId", AttributeValue.fromS(userId),
                                "role", AttributeValue.fromS("MEMBER"),
                                "joinedAt", AttributeValue.fromS(Instant.now().toString())
                        ))
                        // DynamoDB attribute names are case-sensitive. We write "pk" and "sk".
                        // Prevent overwriting an existing membership record.
                        .conditionExpression("attribute_not_exists(pk) AND attribute_not_exists(sk)")
                        .build())
                .build();
    }


    private TransactWriteItem putUserLoop(String loopId, String userId) {
        return TransactWriteItem.builder()
                .put(Put.builder()
                        .tableName(tableName)
                        .item(Map.of(
                                "pk", AttributeValue.fromS(KeyUtil.userPk(userId)),
                                "sk", AttributeValue.fromS(KeyUtil.userLoopSk(loopId))
                        ))
                        // Prevent overwriting an existing user->loop edge.
                        .conditionExpression("attribute_not_exists(pk) AND attribute_not_exists(sk)")
                        .build())
                .build();
    }


    private TransactWriteItem deleteItem(String pk, String sk) {
        return TransactWriteItem.builder()
                .delete(Delete.builder()
                        .tableName(tableName)
                        .key(Map.of(
                                "pk", AttributeValue.fromS(pk),
                                "sk", AttributeValue.fromS(sk)
                        ))
                        .build())
                .build();
    }

    private TransactWriteItem incrementMemberCount(String loopId, int delta) {
        return TransactWriteItem.builder()
                .update(Update.builder()
                        .tableName(tableName)
                        .key(Map.of(
                                "pk", AttributeValue.fromS(KeyUtil.loopPk(loopId)),
                                "sk", AttributeValue.fromS("META")
                        ))
                        .updateExpression(
                                "SET memberCount = if_not_exists(memberCount, :zero) + :v"
                        )
                        .expressionAttributeValues(Map.of(
                                ":v", AttributeValue.fromN(String.valueOf(delta)),
                                ":zero", AttributeValue.fromN("0")
                        ))
                        .build())
                .build();
    }
}

