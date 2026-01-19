package temp;

import com.felipedidio.logging.writer.WideEventWriter;
import com.google.gson.Gson;

import java.util.*;

public class WideEventExample {
    private static final Gson gson = new Gson();

    public record HttpRequest(String method, String path, String httpVersion, Map<String, List<String>> headers,
                              String body) {
    }

    public record UserSession(String username, List<String> permissions) {
    }

    public record HttpResponse(String statusCode, String statusMessage, String content) {
    }

    public static void main(String[] args) {
        // No authorization -> log should reflect that the signed-out user was ignored.
        {
            HttpRequest request = new HttpRequest("POST", "/api/integration/response", "HTTP/1.1", Map.of(), "{}");
            handleIntegrationResponse(request);
        }

        // Empty authorization -> exception, should be logged
        {
            HttpRequest request = new HttpRequest("POST", "/api/integration/response", "HTTP/1.1", Map.of("Authorization", List.of()), "{}");
            handleIntegrationResponse(request);
        }
        {
            HttpRequest request = new HttpRequest("POST", "/api/integration/response", "HTTP/1.1", Map.of("Authorization", List.of("admin")), "{}");
            handleIntegrationResponse(request);
        }
        {
            HttpRequest request = new HttpRequest("POST", "/api/integration/response", "HTTP/1.1", Map.of("Authorization", List.of("john")), "{}");
            handleIntegrationResponse(request);
        }
        {
            HttpRequest request = new HttpRequest("POST", "/api/integration/response", "HTTP/1.1", Map.of("Authorization", List.of("error")), "{}");
            handleIntegrationResponse(request);
        }
        {
            HttpRequest request = new HttpRequest("POST", "/api/integration/response", "HTTP/1.1", Map.of("Authorization", List.of("mark")), "{}");
            handleIntegrationResponse(request);
        }
    }

    private static void handleIntegrationResponse(HttpRequest request) {
        try (WideEventWriter event = MyWideEvent.EMITTER.begin()) {
            try {
                logRequest(event, request);

                UserSession session;
                try (WideEventWriter sessionGroup = event.group("session")) {
                    session = authenticate(request, event);
                    sessionGroup.set("authenticated", session != null);
                    if (session != null) {
                        sessionGroup.set("username", session.username());
                        sessionGroup.set("permissions", gson.toJsonTree(session.permissions()));
                    }
                }

                if (session == null) {
                    HttpResponse response = respondSessionFail(request, event);
                    logResponse(event, response);
                    return;
                }

                if (!session.permissions().contains("IntegrationResponse")) {
                    HttpResponse response = respondPermissionFail(request, event);
                    logResponse(event, response);
                    return;
                }

                HttpResponse response;
                try (WideEventWriter processingGroup = event.group("processing")) {
                    try {
                        boolean success = handleResponse(processingGroup, session, request.body);

                        if (success) {
                            response = new HttpResponse("200", "Success", "{}");
                        } else {
                            response = new HttpResponse("400", "Bad request", "{}");
                        }

                        logResponse(event, response);
                    } catch (Exception e) {
                        event.error(e);
                        response = respondProcessingFail(request, event);
                    }
                }

                logResponse(event, response);
            } catch (Exception e) {
                event.error(e);
            }
        }
    }

    private static HttpResponse respondProcessingFail(HttpRequest request, WideEventWriter event) {
        return new HttpResponse("400", "Bad request", "{}");
    }

    private static boolean handleResponse(WideEventWriter processingGroup, UserSession session, String body) {
        if (Objects.equals(session.username(), "error")) {
            throw new RuntimeException("Error", new RuntimeException("due", new RuntimeException("to", new RuntimeException("invalid", new RuntimeException("user")))));
        }
        return !Objects.equals(session.username(), "admin");
    }

    private static void logResponse(WideEventWriter event, HttpResponse response) {
        try (WideEventWriter responseGroup = event.group("response")) {
            responseGroup.set("status_code", response.statusCode());
            responseGroup.set("status_message", response.statusMessage());
            responseGroup.set("body", response.content());
        }
    }

    private static HttpResponse respondSessionFail(HttpRequest request, WideEventWriter event) {
        return new HttpResponse("401", "Unauthorized", "{}");
    }

    private static HttpResponse respondPermissionFail(HttpRequest request, WideEventWriter event) {
        return new HttpResponse("403", "Forbidden", "{}");
    }

    private static UserSession authenticate(HttpRequest request, WideEventWriter sessionGroup) {
        List<String> authorization = request.headers().get("Authorization");
        if (authorization == null) {
            return null;
        }
        String auth = authorization.get(0);

        List<String> permissions = new ArrayList<>();
        if (Objects.equals(auth, "admin")) {
            permissions.add("IntegrationResponse");
        } else if (Objects.equals(auth, "john")) {
            permissions.add("IntegrationResponse");
        } else if (Objects.equals(auth, "error")) {
            permissions.add("IntegrationResponse");
        }
        return new UserSession(auth, permissions);
    }

    private static void logRequest(WideEventWriter event, HttpRequest request) {
        try (WideEventWriter requestGroup = event.group("request")) {
            requestGroup.set("method", request.method());
            requestGroup.set("path", request.path());
            requestGroup.set("http_version", request.httpVersion());
            requestGroup.set("headers", gson.toJsonTree(request.headers()));
            requestGroup.set("body", request.body());
        }
    }
}
