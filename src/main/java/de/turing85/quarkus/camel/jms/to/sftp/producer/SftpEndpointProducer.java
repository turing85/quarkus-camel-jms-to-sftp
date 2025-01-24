package de.turing85.quarkus.camel.jms.to.sftp.producer;

import jakarta.enterprise.inject.Produces;

import de.turing85.quarkus.camel.jms.to.sftp.config.SftpConfig;
import io.quarkus.runtime.LaunchMode;
import org.apache.camel.builder.endpoint.dsl.SftpEndpointBuilderFactory;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.sftp;

class SftpEndpointProducer {
  @Produces
  SftpEndpointBuilderFactory.SftpEndpointProducerBuilder sftEndpoint(SftpConfig sftpConfig,
      LaunchMode launchMode) {
    // @formatter:off
    SftpEndpointBuilderFactory.SftpEndpointBuilder builder = sftp(sftpConfig.uploadPath())
        .username(sftpConfig.username())
        .password(sftpConfig.password())
        .useUserKnownHostsFile(false)
        .serverHostKeys("ssh-rsa")
        .knownHostsUri("classpath:known_hosts");
    // @formatter:on
    if (launchMode != LaunchMode.TEST) {
      builder.strictHostKeyChecking("yes");
    }
    return builder;
  }
}
