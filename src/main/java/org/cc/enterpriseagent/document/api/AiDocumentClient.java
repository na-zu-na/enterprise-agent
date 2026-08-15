package org.cc.enterpriseagent.document.api;

import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.document.dto.DocumentParseRequestDTO;
import org.cc.enterpriseagent.document.dto.DocumentParseResponseVO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiDocumentClient {
    @Autowired
    private RestClient client;

    public Result<DocumentParseResponseVO> parseDocument(DocumentParseRequestDTO documentParseRequestDTO) {
        return client.post()
                .uri("/document/post")
                .body(documentParseRequestDTO)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
