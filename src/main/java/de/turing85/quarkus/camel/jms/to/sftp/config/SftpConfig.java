package de.turing85.quarkus.camel.jms.to.sftp.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "route.sftp")
public interface SftpConfig {
  String url();

  String username();

  String password();

  default String uploadPath() {
    return "%s/upload".formatted(url());
  }
}
