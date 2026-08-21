package org.cc.enterpriseagent.document.service;

import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.document.dto.RagAskRequestDTO;
import org.cc.enterpriseagent.document.vo.CitationVO;
import org.cc.enterpriseagent.document.vo.DocumentParseResultVO;
import org.cc.enterpriseagent.document.vo.RagResponseVO;

public interface DocumentService {
    Result<DocumentParseResultVO> parseDocument(Long id);

    Result<RagResponseVO> ragQueryDocument(RagAskRequestDTO requestDTO);
}
