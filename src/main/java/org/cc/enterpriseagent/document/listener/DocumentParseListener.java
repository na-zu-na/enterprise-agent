package org.cc.enterpriseagent.document.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cc.enterpriseagent.document.event.DocumentUploadedEvent;
import org.cc.enterpriseagent.document.service.DocumentService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentParseListener {
    private final DocumentService documentService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentUploadedEvent(DocumentUploadedEvent event) {
        try {
            documentService.parseDocument(event.documentId());
        } catch (Exception e) {
            log.error("文档自动解析异常，documentId={}", event.documentId(), e);
        }
    }
}
