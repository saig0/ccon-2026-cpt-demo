package io.camunda.demo.repositories;

import java.util.List;

import io.camunda.demo.model.Robot;
import io.camunda.demo.model.RobotIntent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotRepository extends JpaRepository<Robot, Long> {

  /** Find all robots with a given intent/category, e.g. {@link RobotIntent#TRANSLATION}. */
  List<Robot> findByIntent(RobotIntent intent);

  /** Find all versions of a robot model, e.g. all "C3PO" versions. */
  List<Robot> findByModelId(String modelId);
}
