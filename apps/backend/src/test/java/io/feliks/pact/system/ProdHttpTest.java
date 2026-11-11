package io.feliks.pact.system;

import io.feliks.pact.bootstrap.PactApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PactApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("prod")
class ProdHttpTest {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void productionKeepsApiAndHealthButDisablesDocumentation() throws Exception {
        assertThat(get("/api/system/info").statusCode()).isEqualTo(200);
        assertThat(get("/actuator/health/readiness").statusCode()).isEqualTo(200);
        assertThat(get("/v3/api-docs").statusCode()).isEqualTo(404);
        assertThat(get("/v3/api-docs.yaml").statusCode()).isEqualTo(404);
        assertThat(get("/swagger-ui.html").statusCode()).isEqualTo(404);
        assertThat(get("/swagger-ui/index.html").statusCode()).isEqualTo(404);
        assertThat(get("/actuator/env").statusCode()).isEqualTo(404);
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
