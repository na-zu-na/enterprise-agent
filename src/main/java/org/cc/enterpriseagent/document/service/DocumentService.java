package org.cc.enterpriseagent.document.service;

import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.document.vo.DocumentChunkResponseVO;
import org.cc.enterpriseagent.document.vo.DocumentParseResultVO;

import java.util.List;

public interface DocumentService {
    Result<DocumentParseResultVO> parseDocument(Long id);
}
