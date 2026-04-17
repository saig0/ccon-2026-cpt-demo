package io.camunda.demo.repositories;

import io.camunda.demo.model.KnowledgeBaseEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBaseEntry, Long> {

  /**
   * Find all knowledge base entries whose {@code keywords} column contains the given keyword
   * as an exact comma-delimited token (case-insensitive). Wrapping commas are added to both
   * the stored keyword list and the search term so that partial-word false positives are avoided
   * (e.g. searching "base" will not match an entry tagged "database").
   */
  @Query("SELECT e FROM KnowledgeBaseEntry e WHERE LOWER(CONCAT(',', e.keywords, ',')) LIKE LOWER(CONCAT('%,', TRIM(:keyword), ',%'))")
  List<KnowledgeBaseEntry> findByKeyword(@Param("keyword") String keyword);
}
