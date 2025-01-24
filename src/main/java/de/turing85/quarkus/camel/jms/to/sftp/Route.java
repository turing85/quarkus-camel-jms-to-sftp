package de.turing85.quarkus.camel.jms.to.sftp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;

import de.turing85.quarkus.camel.jms.to.sftp.config.RouteConfig;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.endpoint.dsl.SftpEndpointBuilderFactory;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;

@ApplicationScoped
@RequiredArgsConstructor
public class Route extends RouteBuilder {
  public static final String HEADER_FILE_NAME = "filename";

  private final ConnectionFactory connectionFactory;
  private final RouteConfig routeConfig;
  private final SftpEndpointBuilderFactory.SftpEndpointProducerBuilder sftpEndpoint;

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
        .to(sftpEndpoint)
        .log("Done!");
    // @formatter:on
  }
}
