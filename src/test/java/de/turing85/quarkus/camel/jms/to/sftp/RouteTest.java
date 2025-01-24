package de.turing85.quarkus.camel.jms.to.sftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import com.google.common.truth.Truth;
import de.turing85.quarkus.camel.jms.to.sftp.config.RouteConfig;
import de.turing85.quarkus.camel.jms.to.sftp.resource.SftpTestContainer;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@QuarkusTest
@WithTestResource(SftpTestContainer.class)
@RequiredArgsConstructor
class RouteTest {
  private final RouteConfig routeConfig;
  private final ConnectionFactory connectionFactory;

  @Test
  void test() throws IOException, JMSException {
    // given
    final String bodyToSend = "Hello World";
    final String fileName = "HelloWorld.txt";

    // when
    try (JMSContext context = connectionFactory.createContext()) {
      final Message messageToSend = context.createTextMessage(bodyToSend);
      messageToSend.setStringProperty(Route.HEADER_FILE_NAME, fileName);
      context.createProducer().send(context.createQueue(routeConfig.queue()), messageToSend);
    }

    // then
    // @formatter:off
    final Path actualFile = Objects.requireNonNull(SftpTestContainer.temporaryDirectory())
        .resolve(fileName);
    Awaitility.await()
        .atMost(Duration.ofSeconds(2))
        .until(() -> Files.exists(actualFile));
    // @formatter:on
    Truth.assertThat(Files.readString(actualFile)).isEqualTo(bodyToSend);
  }
}
