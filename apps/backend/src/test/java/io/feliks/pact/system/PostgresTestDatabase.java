package io.feliks.pact.system;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

abstract class PostgresTestDatabase {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.11-trixie")
            .withDatabaseName("pact")
            .withUsername("postgres")
            .withPassword("test-admin");

    static {
        try {
            startDatabase(POSTGRES);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(POSTGRES::stop));
    }

    static void startDatabase(PostgreSQLContainer<?> postgres) throws Exception {
        postgres.start();
        postgres.copyFileToContainer(MountableFile.forClasspathResource("db/test-roles.sql"), "/tmp/test-roles.sql");
        var result = postgres.execInContainer("psql", "-v", "ON_ERROR_STOP=1", "-U", "postgres", "-d", "pact", "-f", "/tmp/test-roles.sql");
        if (result.getExitCode() != 0) {
            throw new IllegalStateException("Could not initialize Testcontainers roles: " + result.getStderr());
        }
    }

    static String jdbcUrl(PostgreSQLContainer<?> postgres) {
        if ("true".equals(System.getenv("PACT_TEST_DIRECT_CONTAINER_NETWORK"))) {
            var ip = postgres.getContainerInfo().getNetworkSettings().getNetworks().get("bridge").getIpAddress();
            return "jdbc:postgresql://" + ip + ":5432/pact";
        }
        return postgres.getJdbcUrl();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("PACT_DB_JDBC_URL", () -> jdbcUrl(POSTGRES));
        registry.add("PACT_DB_APP_PASSWORD", () -> "test-app");
        registry.add("PACT_DB_MIGRATOR_PASSWORD", () -> "test-migrator");
    }
}
