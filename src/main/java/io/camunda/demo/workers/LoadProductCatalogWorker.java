package io.camunda.demo.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.catalog.ProductCatalogService;
import io.camunda.demo.catalog.Robot;
import java.util.LinkedHashMap;
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

    final List<Robot> robots = productCatalogService.findAllRobots();
    LOG.info("Loaded {} robots from product catalog", robots.size());

    final List<Map<String, Object>> products = robots.stream()
        .map(this::toProductMap)
        .toList();

    LOG.info("load-product-catalog completed: {}", job.getKey());
    return Map.of("products", products, "productCount", products.size());
  }

  private Map<String, Object> toProductMap(Robot robot) {
    final Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", robot.getId());
    map.put("modelId", robot.getModelId());
    map.put("modelVersion", robot.getModelVersion());
    map.put("name", robot.getName());
    map.put("description", robot.getDescription());
    map.put("intent", robot.getIntent());
    map.put("price", robot.getPrice());
    map.put("compatibleUpgrades", robot.getCompatibleUpgrades().stream()
        .map(u -> Map.of("id", u.getId(), "name", u.getName(), "price", u.getPrice()))
        .toList());
    return map;
  }
}
