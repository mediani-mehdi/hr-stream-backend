package com.medev.hrstream.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaUpdateRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaUpdateRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        dropStaleApplicationStatusCheck();
    }

    private void dropStaleApplicationStatusCheck() {
        try {
            String sql = """
                    DO $$
                    BEGIN
                        IF EXISTS (
                            SELECT 1 FROM information_schema.table_constraints
                            WHERE constraint_name = 'job_application_status_check'
                              AND table_name = 'job_application'
                        ) THEN
                            ALTER TABLE job_application DROP CONSTRAINT job_application_status_check;
                        END IF;
                    END $$;
                    """;
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            // Already dropped or doesn't exist -- no-op
        }
    }
}
