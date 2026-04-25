package edu.eci.arep.twitter.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson GSON = new Gson();
    private static final int MAX_CHARS = 140;
    private final DynamoDbClient dynamodb = DynamoDbClient.builder().build();
    private final String tableName = System.getenv("POSTS_TABLE");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        Map<String, String> headers = corsHeaders();
        try {
            String rawBody = input.getBody();
            if (rawBody == null || rawBody.isBlank()) {
                return errorResponse(400, "Request body is required", headers);
            }

            JsonObject bodyJson = JsonParser.parseString(rawBody).getAsJsonObject();
            if (!bodyJson.has("content") || bodyJson.get("content").isJsonNull()) {
                return errorResponse(400, "Field 'content' is required", headers);
            }
            String content = bodyJson.get("content").getAsString().trim();

            if (content.isEmpty()) {
                return errorResponse(400, "Content must not be blank", headers);
            }
            if (content.length() > MAX_CHARS) {
                return errorResponse(400, "Post content must not exceed 140 characters", headers);
            }

            Map<String, Object> authContext = extractAuthorizerContext(input);
            String authorSub = getString(authContext, "sub");
            String authorName = getString(authContext, "name");

            String id = UUID.randomUUID().toString();
            String createdAt = Instant.now().toString();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(id).build());
            item.put("createdAt", AttributeValue.builder().s(createdAt).build());
            item.put("content", AttributeValue.builder().s(content).build());
            item.put("authorSub", AttributeValue.builder().s(authorSub).build());
            item.put("authorName", AttributeValue.builder().s(authorName).build());

            dynamodb.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build());

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("id", id);
            responseBody.put("content", content);
            responseBody.put("authorSub", authorSub);
            responseBody.put("authorName", authorName);
            responseBody.put("createdAt", createdAt);

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withHeaders(headers)
                .withBody(GSON.toJson(responseBody));

        } catch (Exception e) {
            return errorResponse(500, "Internal error: " + e.getMessage(), headers);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractAuthorizerContext(APIGatewayProxyRequestEvent input) {
        APIGatewayProxyRequestEvent.ProxyRequestContext ctx = input.getRequestContext();
        if (ctx != null && ctx.getAuthorizer() != null) {
            Object claims = ctx.getAuthorizer().get("claims");
            if (claims instanceof Map) {
                return (Map<String, Object>) claims;
            }
            return ctx.getAuthorizer();
        }
        return Map.of();
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private APIGatewayProxyResponseEvent errorResponse(int status, String message, Map<String, String> headers) {
        Map<String, String> body = Map.of("error", message);
        return new APIGatewayProxyResponseEvent()
            .withStatusCode(status)
            .withHeaders(headers)
            .withBody(GSON.toJson(body));
    }

    private Map<String, String> corsHeaders() {
        Map<String, String> h = new HashMap<>();
        h.put("Content-Type", "application/json");
        h.put("Access-Control-Allow-Origin", "*");
        h.put("Access-Control-Allow-Headers", "Authorization,Content-Type");
        return h;
    }
}
