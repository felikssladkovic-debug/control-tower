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
@ActiveProfiles("test")
class TestProfileHttpTest {

    @LocalServerPort
    private int port;

    @Test
    void testProfileServesOpenApiAndSwaggerUi() throws Exception {
        var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        var docs = client.send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v3/api-docs")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        var swagger = client.send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/swagger-ui.html")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(docs.statusCode()).isEqualTo(200);
        assertThat(swagger.statusCode()).isEqualTo(200);
    }
}
