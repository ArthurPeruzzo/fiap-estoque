package com.fiap.estoque.resources.testcontainer.integration;

import com.fiap.estoque.resources.testcontainer.AbstractContainer;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.ContainerState;

import java.util.Optional;

@ActiveProfiles("integration-test")
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AbstractContainerIntegrationTest extends AbstractContainer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateContainerSuccessFully() {
        boolean isCreated = genericContainer.isCreated();
        String containerId = genericContainer.getContainerId();
        String jdbcUrl = genericContainer.getJdbcUrl();
        String username = genericContainer.getUsername();
        String password = genericContainer.getPassword();
        String databaseName = genericContainer.getDatabaseName();
        String image = Optional.of(genericContainer)
                .map(ContainerState::getCurrentContainerInfo)
                .map(InspectContainerResponse::getConfig)
                .map(ContainerConfig::getImage).orElse(null);

        Assertions.assertTrue(isCreated);
        Assertions.assertNotNull(containerId);
        Assertions.assertNotNull(jdbcUrl);
        Assertions.assertEquals("root", username);
        Assertions.assertEquals("root", password);
        Assertions.assertEquals("estoque", databaseName);
        Assertions.assertNotNull(image);
        Assertions.assertEquals("mysql:8.4.8", image);
    }

}
