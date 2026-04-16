package io.camunda.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a troubleshooting entry in the Camunda Robotics knowledge base.
 * Each entry describes a known problem, associated keywords/tags for search,
 * and the recommended solution.
 */
@Entity
@Table(name = "knowledge_base_entries")
public class KnowledgeBaseEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Short description of the problem. */
  @Column(nullable = false, length = 1000)
  private String problem;

  /**
   * Comma-separated keywords/tags used to search for this entry,
   * e.g. "bender,fuel,beer,fabrication".
   */
  @Column(nullable = false, length = 1000)
  private String keywords;

  /** Recommended solution for the problem. */
  @Column(nullable = false, length = 2000)
  private String solution;

  protected KnowledgeBaseEntry() {
  }

  public KnowledgeBaseEntry(String problem, String keywords, String solution) {
    this.problem = problem;
    this.keywords = keywords;
    this.solution = solution;
  }

  public Long getId() {
    return id;
  }

  public String getProblem() {
    return problem;
  }

  public void setProblem(String problem) {
    this.problem = problem;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public String getSolution() {
    return solution;
  }

  public void setSolution(String solution) {
    this.solution = solution;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseEntry{id=" + id + ", problem='" + problem + "', keywords='" + keywords + "'}";
  }
}
