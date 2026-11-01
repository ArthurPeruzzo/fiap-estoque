package com.fiap.estoque.resources.testcontainer;

import org.testcontainers.containers.RabbitMQContainer;

public class RabbitTestContainer extends RabbitMQContainer {

    private static final String IMAGE_VERSION = "rabbitmq:4.1-management";

    public RabbitTestContainer() {
        super(IMAGE_VERSION);
    }
}
