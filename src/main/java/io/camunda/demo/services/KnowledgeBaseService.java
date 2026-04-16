package io.camunda.demo.services;

import io.camunda.demo.dto.KnowledgeBaseEntryDto;
import io.camunda.demo.repositories.KnowledgeBaseRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for querying the Camunda Robotics troubleshooting knowledge base.
 * Returns {@link KnowledgeBaseEntryDto} objects instead of JPA entities to avoid
 * lazy-loading issues outside the transaction boundary.
 */
@Service
@Transactional(readOnly = true)
public class KnowledgeBaseService {

  private final KnowledgeBaseRepository knowledgeBaseRepository;

  public KnowledgeBaseService(KnowledgeBaseRepository knowledgeBaseRepository) {
    this.knowledgeBaseRepository = knowledgeBaseRepository;
  }

  /** Returns all entries in the knowledge base as DTOs. */
  public List<KnowledgeBaseEntryDto> findAll() {
    return knowledgeBaseRepository.findAll().stream()
        .map(KnowledgeBaseEntryDto::from)
        .toList();
  }

  /**
   * Returns all knowledge base entries whose keywords contain the given tag/keyword
   * (case-insensitive substring match).
   */
  public List<KnowledgeBaseEntryDto> findByKeyword(String keyword) {
    return knowledgeBaseRepository.findByKeyword(keyword).stream()
        .map(KnowledgeBaseEntryDto::from)
        .toList();
  }
}
