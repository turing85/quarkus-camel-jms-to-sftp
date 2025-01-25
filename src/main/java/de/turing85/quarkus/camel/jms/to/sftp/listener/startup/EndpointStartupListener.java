package de.turing85.quarkus.camel.jms.to.sftp.listener.startup;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import de.turing85.quarkus.camel.jms.to.sftp.config.SftpConfig;
import lombok.RequiredArgsConstructor;
import org.apache.camel.component.file.remote.SftpEndpoint;
import org.apache.camel.quarkus.core.events.EndpointAddEvent;

@ApplicationScoped
@RequiredArgsConstructor
class EndpointStartupListener {
  private final SftpConfig sftpConfig;

  void onConsumerAdd(@Observes final EndpointAddEvent event) {
    Optional<String> maybeKnownHosts = sftpConfig.knownHostsUri();
    if (event.getEndpoint() instanceof SftpEndpoint sftpEndpoint && maybeKnownHosts.isPresent()) {
      sftpEndpoint.getConfiguration().setStrictHostKeyChecking("yes");
      sftpEndpoint.getConfiguration().setKnownHostsUri(maybeKnownHosts.get());
    }
  }
}
