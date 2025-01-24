package de.turing85.quarkus.camel.jms.to.sftp.listener.startup;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.LaunchMode;
import lombok.RequiredArgsConstructor;
import org.apache.camel.component.file.remote.SftpEndpoint;
import org.apache.camel.quarkus.core.events.EndpointAddEvent;

@ApplicationScoped
@RequiredArgsConstructor
class EndpointStartupListener {
  private final LaunchMode launchMode;

  void onConsumerAdd(@Observes final EndpointAddEvent event) {
    if (event.getEndpoint() instanceof SftpEndpoint sftpEndpoint && launchMode != LaunchMode.TEST) {
      sftpEndpoint.getConfiguration().setStrictHostKeyChecking("yes");
    }
  }
}
