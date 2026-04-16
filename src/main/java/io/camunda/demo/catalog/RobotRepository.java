package io.camunda.demo.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotRepository extends JpaRepository<Robot, Long> {

  /** Find all robots with a given intent/category, e.g. "translation". */
  List<Robot> findByIntent(String intent);

  /** Find all versions of a robot model, e.g. all "C3PO" versions. */
  List<Robot> findByModelId(String modelId);
}
