package io.camunda.demo.dto;

import io.camunda.demo.model.KnowledgeBaseEntry;
import java.util.Arrays;
import java.util.List;

/** Type-safe DTO for an entry in the Camunda Robotics troubleshooting knowledge base. */
public record KnowledgeBaseEntryDto(
    Long id,
    String problem,
    List<String> keywords,
    String solution) {

  public static KnowledgeBaseEntryDto from(KnowledgeBaseEntry entry) {
    List<String> keywordList = Arrays.stream(entry.getKeywords().split(","))
        .map(String::trim)
        .filter(k -> !k.isEmpty())
        .toList();
    return new KnowledgeBaseEntryDto(
        entry.getId(),
        entry.getProblem(),
        keywordList,
        entry.getSolution());
  }
}
