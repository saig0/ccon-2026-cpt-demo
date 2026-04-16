package io.camunda.demo.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.dto.KnowledgeBaseEntryDto;
import io.camunda.demo.services.KnowledgeBaseService;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Job worker that searches the Camunda Robotics troubleshooting knowledge base.
 *
 * <p>If a {@code keyword} variable is present on the job, only entries matching that keyword are
 * returned (as {@code knowledgeBaseEntries}). Otherwise all entries are returned.
 */
@Component
public class KnowledgeBaseWorker {

  private static final Logger LOG = LoggerFactory.getLogger(KnowledgeBaseWorker.class);

  private final KnowledgeBaseService knowledgeBaseService;

  public KnowledgeBaseWorker(KnowledgeBaseService knowledgeBaseService) {
    this.knowledgeBaseService = knowledgeBaseService;
  }

  @JobWorker(type = "search-knowledge-base")
  public Map<String, Object> searchKnowledgeBase(
      final ActivatedJob job,
      @Variable(name = "keyword") @Nullable String keyword) {

    LOG.info("Processing search-knowledge-base job: {}", job.getKey());

    final List<KnowledgeBaseEntryDto> entries;
    if (keyword != null && !keyword.isBlank()) {
      entries = knowledgeBaseService.findByKeyword(keyword);
      LOG.info("Found {} knowledge base entry/entries for keyword '{}'", entries.size(), keyword);
    } else {
      entries = knowledgeBaseService.findAll();
      LOG.info("Loaded all {} knowledge base entries", entries.size());
    }

    LOG.info("search-knowledge-base completed: {}", job.getKey());
    return Map.of("knowledgeBaseEntries", entries, "knowledgeBaseEntryCount", entries.size());
  }
}
