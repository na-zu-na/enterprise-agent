package org.cc.enterpriseagent.document.service;

import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.document.vo.DocumentParseResultVO;

public interface DocumentService {
    Result<DocumentParseResultVO> parseDocument(Long id);
}
