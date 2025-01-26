package de.turing85.quarkus.camel.jms.to.sftp.resource.artemis;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jspecify.annotations.Nullable;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class ArtemisTestContainer
    implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
  // @formatter:off
  public static final DockerImageName CONTAINER_IMAGE = DockerImageName
      .parse("arkmq-org/activemq-artemis-broker")
      .withTag("artemis.2.39.0")
      .withRegistry("quay.io");
  // @formatter:on
  public static final int JMS_PORT = 61616;
  public static final String JMS_USER = "jms-user";
  public static final String JMS_PASSWORD = "jms-pass";

  private static final String CONTAINER_NAME = "artemis";

  @Nullable
  private static GenericContainer<?> container;

  @Nullable
  private static String containerNetworkId;

  @Nullable
  private static Integer mappedJmsPort;

  @Override
  public void setIntegrationTestContext(DevServicesContext context) {
    containerNetworkId = context.containerNetworkId().orElse(null);
  }

  @Override
  public Map<String, String> start() {
    stop();
    container = createAndStartContainer();
    mappedJmsPort = container.getMappedPort(JMS_PORT);
    return createConfig(mappedJmsPort);
  }

  private static GenericContainer<?> createAndStartContainer() {
    // @formatter:off
    final GenericContainer<?> container = new GenericContainer<>(CONTAINER_IMAGE)
        .withEnv("AMQ_USER", JMS_PASSWORD)
        .withEnv("AMQ_PASSWORD", JMS_PASSWORD)
        .withExposedPorts(JMS_PORT)
        .withCreateContainerCmdModifier(command -> command.withName(CONTAINER_NAME))
        .waitingFor(Wait.forLogMessage(".*AMQ241004.*", 1));
    Optional.ofNullable(containerNetworkId).ifPresent(container::withNetworkMode);
    // @formatter:on
    container.start();
    return container;
  }

  private static Map<String, String> createConfig(int mappedJmsPort) {
    final int port = Objects.isNull(containerNetworkId) ? mappedJmsPort : JMS_PORT;
    final String host = Objects.isNull(containerNetworkId) ? "localhost" : CONTAINER_NAME;
    // @formatter:off
    return Map.of(
        "quarkus.artemis.url", "tcp://%s:%d".formatted(host, port),
        "quarkus.artemis.username", JMS_USER,
        "quarkus.artemis.password", JMS_PASSWORD);
    // @formatter:on
  }

  @Override
  public void stop() {
    Optional.ofNullable(container).ifPresent(GenericContainer::stop);
    mappedJmsPort = null;
  }

  @Override
  public void inject(TestInjector testInjector) {
    // @formatter:off
    testInjector.injectIntoFields(
        Optional.ofNullable(mappedJmsPort)
            .map("tcp://localhost:%d"::formatted)
            .orElseThrow(() ->
                new IllegalStateException("artemis container has not yet been initialized.")),
        new TestInjector.AnnotatedAndMatchesType(InjectJmsUrl.class, String.class));
    // @formatter:on
  }
}
