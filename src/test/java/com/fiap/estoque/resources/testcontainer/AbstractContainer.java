package com.fiap.estoque.resources.testcontainer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.List;

public abstract class AbstractContainer {

    protected static JdbcDatabaseContainer<?> genericContainer = new MysqlTestContainer();

    static {
        genericContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", genericContainer::getJdbcUrl);
        registry.add("spring.datasource.username", genericContainer::getUsername);
        registry.add("spring.datasource.password", genericContainer::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanupBeforeTest() {
        truncateTables();
    }

    @AfterEach
    public void truncateTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() " +
                        "AND table_name not in('flyway_schema_history', 'roles')",
                String.class
        );

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table));

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
