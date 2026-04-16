package io.camunda.demo.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.dto.RobotDto;
import io.camunda.demo.services.ProductCatalogService;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoadProductCatalogWorker {

  private static final Logger LOG = LoggerFactory.getLogger(LoadProductCatalogWorker.class);

  private final ProductCatalogService productCatalogService;

  public LoadProductCatalogWorker(ProductCatalogService productCatalogService) {
    this.productCatalogService = productCatalogService;
  }

  @JobWorker(type = "load-product-catalog")
  public Map<String, Object> loadProductCatalog(final ActivatedJob job) {
    LOG.info("Processing load-product-catalog job: {}", job.getKey());

    final List<RobotDto> products = productCatalogService.findAllRobots();
    LOG.info("Loaded {} products from product catalog", products.size());

    LOG.info("load-product-catalog completed: {}", job.getKey());
    return Map.of("products", products, "productCount", products.size());
  }
}
