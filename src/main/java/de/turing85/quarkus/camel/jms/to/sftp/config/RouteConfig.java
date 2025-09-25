package de.turing85.quarkus.camel.jms.to.sftp.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "route")
public interface RouteConfig {
  String queue();

  SftpConfig sftp();
}
