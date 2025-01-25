package de.turing85.quarkus.camel.jms.to.sftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import com.google.common.truth.Truth;
import de.turing85.quarkus.camel.jms.to.sftp.resource.artemis.ArtemisTestContainer;
import de.turing85.quarkus.camel.jms.to.sftp.resource.artemis.InjectJmsUrl;
import de.turing85.quarkus.camel.jms.to.sftp.resource.sftp.InjectSftpRoot;
import de.turing85.quarkus.camel.jms.to.sftp.resource.sftp.SftpTestContainer;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@QuarkusTest
@WithTestResource(ArtemisTestContainer.class)
@WithTestResource(SftpTestContainer.class)
class RouteTest {
  @InjectSftpRoot
  @Nullable
  Path sftpRootDirectory;

  @InjectJmsUrl
  @Nullable
  String url;

  @Test
  void test() throws IOException, JMSException {
    // given
    final String bodyToSend = "Hello World";
    final String fileName = "HelloWorld.txt";

    // when
    send(bodyToSend, fileName);

    // then
    // @formatter:off
    final Path actualFile = Objects.requireNonNull(sftpRootDirectory).resolve(fileName);
    Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .until(() -> Files.exists(actualFile));
    // @formatter:on
    Truth.assertThat(Files.readString(actualFile)).isEqualTo(bodyToSend);
  }

  private void send(final String messageBody, final String fileName) throws JMSException {
    final String username = ConfigProvider.getConfig()
        .getOptionalValue("quarkus.artemis.username", String.class).orElse(null);
    final String password = ConfigProvider.getConfig()
        .getOptionalValue("quarkus.artemis.password", String.class).orElse(null);
    final String queueName = ConfigProvider.getConfig().getValue("route.queue", String.class);
    try (
        final ActiveMQJMSConnectionFactory cf =
            new ActiveMQJMSConnectionFactory(Objects.requireNonNull(url), username, password);
        final JMSContext context = cf.createContext()) {
      final Message messageToSend = context.createTextMessage(messageBody);
      messageToSend.setStringProperty(Route.HEADER_FILE_NAME, fileName);
      context.createProducer().send(context.createQueue(queueName), messageToSend);
    }
  }
}
