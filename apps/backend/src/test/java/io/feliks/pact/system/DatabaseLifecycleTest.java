package io.feliks.pact.system;

import io.feliks.pact.bootstrap.PactApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseLifecycleTest {

    @Test
    void migrationAndTestDataSurviveApplicationRestart() throws Exception {
        try (var postgres = database()) {
            PostgresTestDatabase.startDatabase(postgres);
            var url = PostgresTestDatabase.jdbcUrl(postgres);

            try (var first = application(url)) {
                assertThat(get(first, "/actuator/health/readiness")).isEqualTo(200);
                try (var migrator = DriverManager.getConnection(url, "pact_migrator", "test-migrator")) {
                    try (var statement = migrator.createStatement()) {
                        try (var result = statement.executeQuery("SELECT has_schema_privilege('pact_app', 'pact', 'USAGE')")) {
                            assertThat(result.next()).isTrue();
                            assertThat(result.getBoolean(1)).isTrue();
                        }
                        try (var result = statement.executeQuery("SELECT has_schema_privilege('pact_app', 'pact', 'CREATE')")) {
                            assertThat(result.next()).isTrue();
                            assertThat(result.getBoolean(1)).isFalse();
                        }
                        statement.execute("CREATE TABLE pact.restart_fixture (id integer PRIMARY KEY, note text NOT NULL)");
                        statement.execute("GRANT INSERT, SELECT ON pact.restart_fixture TO pact_app");
                    }
                }
                try (var app = DriverManager.getConnection(url, "pact_app", "test-app");
                     var statement = app.createStatement()) {
                    statement.executeUpdate("INSERT INTO pact.restart_fixture VALUES (1, 'retained')");
                    assertThatThrownBy(() -> statement.executeQuery("SELECT * FROM pact.flyway_schema_history"))
                            .isInstanceOf(SQLException.class);
                }
            }

            try (var second = application(url);
                 var app = DriverManager.getConnection(url, "pact_app", "test-app");
                 var migrator = DriverManager.getConnection(url, "pact_migrator", "test-migrator")) {
                assertThat(get(second, "/actuator/health/readiness")).isEqualTo(200);
                try (var statement = app.createStatement();
                     var result = statement.executeQuery("SELECT note FROM pact.restart_fixture WHERE id = 1")) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getString(1)).isEqualTo("retained");
                }
                try (var statement = migrator.createStatement();
                     var result = statement.executeQuery("SELECT version, success FROM pact.flyway_schema_history WHERE version IS NOT NULL")) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getString("version")).isEqualTo("1");
                    assertThat(result.getBoolean("success")).isTrue();
                    assertThat(result.next()).isFalse();
                }
            }
        }
    }

    @Test
    void databaseLossChangesReadinessButNotLivenessAndPreventsNewStartup() throws Exception {
        var postgres = database();
        PostgresTestDatabase.startDatabase(postgres);
        var url = PostgresTestDatabase.jdbcUrl(postgres);
        try (var app = application(url)) {
            assertThat(get(app, "/actuator/health/readiness")).isEqualTo(200);
            postgres.stop();
            assertThat(get(app, "/actuator/health/readiness")).isEqualTo(503);
            assertThat(get(app, "/actuator/health/liveness")).isEqualTo(200);
        } finally {
            postgres.stop();
        }
        assertThatThrownBy(() -> {
            try (var ignored = application(url)) {
                // A successful context startup would violate the fail-fast contract.
            }
        }).isInstanceOf(Exception.class);
    }

    private PostgreSQLContainer<?> database() {
        return new PostgreSQLContainer<>("postgres:17.11-trixie")
                .withDatabaseName("pact")
                .withUsername("postgres")
                .withPassword("test-admin");
    }

    private ConfigurableApplicationContext application(String url) {
        return new SpringApplicationBuilder(PactApplication.class)
                .properties(
                        "server.port=0",
                        "spring.profiles.active=test",
                        "PACT_DB_JDBC_URL=" + url,
                        "PACT_DB_APP_PASSWORD=test-app",
                        "PACT_DB_MIGRATOR_PASSWORD=test-migrator",
                        "spring.datasource.hikari.connection-timeout=1000")
                .run();
    }

    private int get(ConfigurableApplicationContext context, String path) throws Exception {
        var port = context.getEnvironment().getRequiredProperty("local.server.port");
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
