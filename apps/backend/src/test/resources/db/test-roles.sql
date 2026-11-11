-- Known throwaway credentials, used only inside Testcontainers.
CREATE ROLE pact_migrator LOGIN PASSWORD 'test-migrator';
CREATE ROLE pact_app LOGIN PASSWORD 'test-app';
REVOKE CONNECT ON DATABASE pact FROM PUBLIC;
GRANT CONNECT, CREATE ON DATABASE pact TO pact_migrator;
GRANT CONNECT ON DATABASE pact TO pact_app;
