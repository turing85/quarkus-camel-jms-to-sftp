package de.turing85.quarkus.camel.jms.to.sftp.config;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "route.sftp")
public interface SftpConfig {
  String url();

  String username();

  String password();

  Optional<String> knownHostsUri();

  default String uploadPath() {
    return "%s/upload".formatted(url());
  }
}
