-- Flyway creates the pact schema before V1 so its history table lives there.
-- No application table privileges are granted before those tables exist.
REVOKE ALL ON SCHEMA pact FROM PUBLIC;
GRANT USAGE ON SCHEMA pact TO pact_app;
