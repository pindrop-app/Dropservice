package com.pindrop.bookmarks.repo;

import com.pindrop.bookmarks.model.UserBookmark;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserBookmarkRepository {

    private final DynamoDbTable<UserBookmark> table;

    public UserBookmarkRepository(DynamoDbEnhancedClient enhancedClient,
                                  @Value("${aws.dynamodb.user-bookmarks-table:UserBookmarks}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(UserBookmark.class));
    }

    public void put(UserBookmark bookmark) {
        table.putItem(bookmark);
    }

    public UserBookmark get(String userId, String pinId) {
        return table.getItem(Key.builder().partitionValue(userId).sortValue(pinId).build());
    }

    public void delete(String userId, String pinId) {
        table.deleteItem(Key.builder().partitionValue(userId).sortValue(pinId).build());
    }

    public List<UserBookmark> listByUserId(String userId) {
        List<UserBookmark> out = new ArrayList<>();
        PageIterable<UserBookmark> pages = table.query(r -> r.queryConditional(
                QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build())
        ));
        pages.items().forEach(out::add);
        return out;
    }
}
