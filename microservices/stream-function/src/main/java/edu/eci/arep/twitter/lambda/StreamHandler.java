package edu.eci.arep.twitter.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson GSON = new Gson();
    private final DynamoDbClient dynamodb = DynamoDbClient.builder().build();
    private final String tableName = System.getenv("POSTS_TABLE");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        Map<String, String> headers = corsHeaders();
        try {
            ScanResponse response = dynamodb.scan(ScanRequest.builder()
                .tableName(tableName)
                .build());

            List<Map<String, String>> posts = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                Map<String, String> post = new HashMap<>();
                post.put("id", getStr(item, "id"));
                post.put("content", getStr(item, "content"));
                post.put("authorSub", getStr(item, "authorSub"));
                post.put("authorName", getStr(item, "authorName"));
                post.put("createdAt", getStr(item, "createdAt"));
                posts.add(post);
            }

            posts.sort(Comparator.comparing(
                (Map<String, String> p) -> p.get("createdAt"),
                Comparator.reverseOrder()
            ));

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(headers)
                .withBody(GSON.toJson(posts));

        } catch (Exception e) {
            Map<String, String> body = Map.of("error", "Internal error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withHeaders(headers)
                .withBody(GSON.toJson(body));
        }
    }

    private String getStr(Map<String, AttributeValue> item, String key) {
        AttributeValue val = item.get(key);
        return val != null ? val.s() : "";
    }

    private Map<String, String> corsHeaders() {
        Map<String, String> h = new HashMap<>();
        h.put("Content-Type", "application/json");
        h.put("Access-Control-Allow-Origin", "*");
        h.put("Access-Control-Allow-Headers", "Authorization,Content-Type");
        return h;
    }
}
