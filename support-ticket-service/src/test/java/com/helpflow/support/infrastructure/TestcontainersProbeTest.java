package com.helpflow.support.infrastructure;

import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Minimal probe: verifies that Testcontainers can connect to the Docker daemon.
 * No Spring context is loaded; no container is started.
 * If this test passes, the full @Testcontainers integration test can run.
 */
class TestcontainersProbeTest {

    @Test
    void dockerDaemonIsReachableFromTestcontainers() {
        // DockerClientFactory.instance().client() throws if Docker is unreachable.
        assertNotNull(DockerClientFactory.instance().client(),
            "Testcontainers could not connect to Docker. " +
            "Ensure ~/.docker-java.properties contains api.version=1.44");
    }
}
