package de.turing85.quarkus.camel.jms.to.sftp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;

import de.turing85.quarkus.camel.jms.to.sftp.config.RouteConfig;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.sftp;

@ApplicationScoped
@RequiredArgsConstructor
public class Route extends RouteBuilder {
  public static final String HEADER_FILE_NAME = "filename";

  private final ConnectionFactory connectionFactory;
  private final RouteConfig routeConfig;

  @Override
  public void configure() {
    // @formatter:off
    from(jms(routeConfig.queue()).connectionFactory(connectionFactory))
        .routeId("jms-to-sftp")
        .choice()
            .when(header(HEADER_FILE_NAME).isNull())
                .setHeader(Exchange.FILE_NAME, simple("file-${date:now:yyyyMMddHHmmss}.txt"))
            .otherwise()
                .setHeader(Exchange.FILE_NAME, header(HEADER_FILE_NAME))
        .end()
        .log("Writing content \"${body}\" to file \"${header.%s}\"".formatted(Exchange.FILE_NAME))
        .to(sftp(routeConfig.sftp().uploadPath())
            .username(routeConfig.sftp().username())
            .password(routeConfig.sftp().password())
            .useUserKnownHostsFile(false)
            .serverHostKeys("ssh-rsa")
            .knownHostsUri("classpath:known_hosts"))
        .log("Done!");
    // @formatter:on
  }
}
