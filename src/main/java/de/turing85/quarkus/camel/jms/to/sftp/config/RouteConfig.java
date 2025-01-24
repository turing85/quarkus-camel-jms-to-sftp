package de.turing85.quarkus.camel.jms.to.sftp.config;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "route")
@ConfigRoot
public interface RouteConfig {
  String queue();

  SftpConfig sftp();
}
