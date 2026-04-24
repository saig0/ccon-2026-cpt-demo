package io.camunda.demo;

import io.camunda.connector.jackson.ConnectorsObjectMapperSupplier;
import io.camunda.connector.runtime.annotation.ConnectorsObjectMapper;
import io.camunda.connector.runtime.core.document.DocumentFactoryImpl;
import io.camunda.connector.runtime.core.document.store.CamundaDocumentStore;
import io.camunda.connector.runtime.core.document.store.InMemoryDocumentStore;
import io.camunda.connector.runtime.core.secret.SecretProviderAggregator;
import io.camunda.connector.runtime.secret.EnvironmentSecretProvider;
import io.camunda.connector.validation.impl.DefaultValidationProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.document.DocumentFactory;
import io.camunda.connector.api.validation.ValidationProvider;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Test configuration that provides the infrastructure beans required by the
 * {@code connector-agentic-ai} auto-configuration when it is added to the test classpath.
 */
@Configuration
class ConnectorTestConfiguration {

  /**
   * Provides the {@link ObjectMapper} with connector-specific serialisation settings, qualified
   * with {@link ConnectorsObjectMapper} as expected by the langchain4j framework adapter.
   */
  @Bean
  @ConnectorsObjectMapper
  ObjectMapper connectorsObjectMapper() {
    return ConnectorsObjectMapperSupplier.getCopy();
  }

  /**
   * Provides a {@link SecretProviderAggregator} backed by an {@link EnvironmentSecretProvider}
   * so that secrets referenced in the BPMN (e.g. {@code {{secrets.AWS_BEDROCK_ACCESS_KEY}}}) are
   * resolved from environment variables at test time.
   */
  @Bean
  SecretProviderAggregator secretProviderAggregator(Environment environment) {
    return new SecretProviderAggregator(
        List.of(new EnvironmentSecretProvider(environment, "", false, false)));
  }

  /** Provides the connector validation provider used to validate connector input models. */
  @Bean
  ValidationProvider connectorValidationProvider() {
    return new DefaultValidationProvider();
  }

  /**
   * Exposes the shared in-memory document store as a Spring bean so that the
   * {@code aiAgentCamundaDocumentConversationStore} can inject it.
   */
  @Bean
  CamundaDocumentStore camundaDocumentStore() {
    return InMemoryDocumentStore.INSTANCE;
  }

  /**
   * Provides an in-memory {@link DocumentFactory} sufficient for the agentic-AI connector's
   * document-handling needs in tests (no real Camunda document store required).
   */
  @Bean
  DocumentFactory documentFactory(CamundaDocumentStore store) {
    return new DocumentFactoryImpl(store);
  }
}
