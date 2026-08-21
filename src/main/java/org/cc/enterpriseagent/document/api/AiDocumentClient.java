package org.cc.enterpriseagent.document.api;

import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.document.dto.AiRagRequestDTO;
import org.cc.enterpriseagent.document.dto.EmbeddingRequestDTO;
import org.cc.enterpriseagent.document.dto.RagAskRequestDTO;
import org.cc.enterpriseagent.document.vo.CitationVO;
import org.cc.enterpriseagent.document.vo.ChunkEmbeddingResponseVO;
import org.cc.enterpriseagent.document.vo.DocumentChunkResponseVO;
import org.cc.enterpriseagent.document.dto.DocumentParseRequestDTO;
import org.cc.enterpriseagent.document.vo.RagResponseVO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class AiDocumentClient {
    @Autowired
    private RestClient client;

    public Result<List<DocumentChunkResponseVO>> parseDocument(DocumentParseRequestDTO documentParseRequestDTO) {
        return client.post()
                .uri("/document/parse")
                .body(documentParseRequestDTO)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Result<RagResponseVO> ragQueryDocument(AiRagRequestDTO requestDTO) {
        return client.post()
                .uri("/rag/query")
                .body(requestDTO)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
