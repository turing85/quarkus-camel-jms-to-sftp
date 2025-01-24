package de.turing85.quarkus.camel.jms.to.sftp.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import io.quarkus.logging.Log;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class SftpTestContainer implements QuarkusTestResourceLifecycleManager {
  public static final DockerImageName IMAGE =
      DockerImageName.parse("atmoz/sftp").withTag("alpine-3.7").withRegistry("docker.io");
  public static final String SFTP_USER = "sftp-user";
  public static final String SFTP_PASSWORD = "sftp-pass";

  @Nullable
  private static GenericContainer<?> container;

  @Getter
  @Nullable
  private static Path temporaryDirectory;

  @Override
  public Map<String, String> start() {
    stop();
    try {
      temporaryDirectory = Files.createTempDirectory("quarkus-camel-jms-to-sftp").toAbsolutePath();
      // See https://stackoverflow.com/a/41886487/4216641
      Files.setPosixFilePermissions(temporaryDirectory,
          PosixFilePermissions.fromString("rwxrwxrwx"));

      // @formatter:off
      container = new GenericContainer<>(IMAGE)
          .withEnv(Map.of("SFTP_USERS", "%s:%s:1001".formatted(SFTP_USER, SFTP_PASSWORD)))
          .withFileSystemBind(
              temporaryDirectory.toString(),
              "/home/%s/upload".formatted(SFTP_USER),
              BindMode.READ_WRITE)
          .withCopyToContainer(
              MountableFile.forClasspathResource("ssh_host_ed25519_key"),
              "/etc/ssh/ssh_host_ed25519_key")
          .withCopyToContainer(
              MountableFile.forClasspathResource("ssh_host_rsa_key"),
              "/etc/ssh/ssh_host_rsa_key")
          .withExposedPorts(22);
      container.start();
      final int sftpPort = container.getMappedPort(22);
      return Map.of(
          "route.sftp.url", "localhost:%d".formatted(sftpPort),
          "route.sftp.username", SFTP_USER,
          "route.sftp.password", SFTP_PASSWORD);
      // @formatter:on
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    Optional.ofNullable(container).ifPresent(GenericContainer::stop);
    deleteTemporaryDirectoryIfPresent();
  }

  private static void deleteTemporaryDirectoryIfPresent() {
    if (temporaryDirectory != null) {
      try (Stream<Path> fileStream = Files.walk(temporaryDirectory)) {
        // @formatter:off
        boolean allDeleted = fileStream
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .map(File::delete)
            .reduce(Boolean::logicalAnd)
            .orElse(false);
        if (!allDeleted) {
          Log.warnf("Unable to delete %s", temporaryDirectory);
        }
        temporaryDirectory = null;
        // @formatter:on
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
