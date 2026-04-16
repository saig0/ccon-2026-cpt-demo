package io.camunda.demo.repositories;

import io.camunda.demo.model.KnowledgeBaseEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBaseEntry, Long> {

  /**
   * Find all knowledge base entries whose {@code keywords} column contains the given keyword
   * (case-insensitive, substring match within the comma-separated keyword list).
   */
  @Query("SELECT e FROM KnowledgeBaseEntry e WHERE LOWER(e.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<KnowledgeBaseEntry> findByKeyword(@Param("keyword") String keyword);
}
