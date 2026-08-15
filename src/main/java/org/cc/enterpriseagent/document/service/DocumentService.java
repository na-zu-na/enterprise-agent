package org.cc.enterpriseagent.document.service;

import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.document.dto.DocumentParseResponseVO;

public interface DocumentService {
    Result<DocumentParseResponseVO> parseDocument(Long id);
}
