package org.cc.enterpriseagent.document.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cc.enterpriseagent.document.event.DocumentChunksReplacedEvent;
import org.cc.enterpriseagent.document.service.DocumentChunkEsSyncService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentChunkEsSyncListener {
    private final DocumentChunkEsSyncService documentChunkEsSyncService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentChunksReplaced(DocumentChunksReplacedEvent event) {
        try {
            documentChunkEsSyncService.syncDocument(event.documentId());
        } catch (Exception e) {
            log.error("文档分块同步 ES 失败，documentId={}",
                    event.documentId(), e);
        }
    }
}
