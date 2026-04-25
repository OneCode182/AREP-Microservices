package edu.eci.arep.twitter.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class UserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson GSON = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        Map<String, String> headers = corsHeaders();
        try {
            Map<String, Object> authContext = extractAuthorizerContext(input);

            String sub = getString(authContext, "sub");
            String email = getString(authContext, "email");
            String name = getString(authContext, "name");

            Map<String, String> body = new HashMap<>();
            body.put("sub", sub);
            body.put("email", email);
            body.put("name", name);

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(headers)
                .withBody(GSON.toJson(body));
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
