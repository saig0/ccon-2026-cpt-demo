package io.camunda.demo.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Upgrade} entities.
 * Extend with custom query methods as the catalog grows.
 */
public interface UpgradeRepository extends JpaRepository<Upgrade, Long> {

}
