package com.smartCity.Web.review;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReviewSchemaUpdater implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public ReviewSchemaUpdater(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (var connection = dataSource.getConnection()) {
            String databaseName = connection.getMetaData().getDatabaseProductName().toLowerCase();

            List<String> statements = databaseName.contains("mysql")
                    ? List.of(
                            "ALTER TABLE reviews DROP INDEX uk_review_user_target",
                            "INSERT INTO review_entries (id, user_id, target_type, target_id, rating, comment, created_at, updated_at) "
                                    + "SELECT r.id, r.user_id, r.target_type, r.target_id, r.rating, r.comment, r.created_at, r.updated_at "
                                    + "FROM reviews r LEFT JOIN review_entries e ON e.id = r.id WHERE e.id IS NULL")
                    : List.of(
                            "ALTER TABLE reviews DROP CONSTRAINT IF EXISTS uk_review_user_target",
                            "ALTER TABLE reviews DROP INDEX IF EXISTS uk_review_user_target",
                            "INSERT INTO review_entries (id, user_id, target_type, target_id, rating, comment, created_at, updated_at) "
                                    + "SELECT r.id, r.user_id, r.target_type, r.target_id, r.rating, r.comment, r.created_at, r.updated_at "
                                    + "FROM reviews r LEFT JOIN review_entries e ON e.id = r.id WHERE e.id IS NULL");

            for (String statement : statements) {
                try {
                    jdbcTemplate.execute(statement);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
