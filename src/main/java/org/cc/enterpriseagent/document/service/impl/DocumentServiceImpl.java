package org.cc.enterpriseagent.document.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.common.UserContext;
import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.document.api.AiDocumentClient;
import org.cc.enterpriseagent.document.dto.AiRagRequestDTO;
import org.cc.enterpriseagent.document.dto.RagAskRequestDTO;
import org.cc.enterpriseagent.document.vo.CitationVO;
import org.cc.enterpriseagent.document.vo.DocumentChunkResponseVO;
import org.cc.enterpriseagent.document.dto.DocumentParseRequestDTO;
import org.cc.enterpriseagent.document.entity.KnowledgeDocument;
import org.cc.enterpriseagent.document.mapper.DocumentMapper;
import org.cc.enterpriseagent.document.service.DocumentChunkService;
import org.cc.enterpriseagent.document.service.DocumentService;
import org.cc.enterpriseagent.document.vo.DocumentParseResultVO;
import org.cc.enterpriseagent.document.vo.RagResponseVO;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;

@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, KnowledgeDocument> implements DocumentService{
    @Autowired
    private AiDocumentClient aiDocumentClient;

    @Value("${document.storage-root}")
    private String storageRoot;

    @Autowired
    private DocumentChunkService documentChunkService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Value("${rag.top-k}")
    private int topK;

    @Value("${rag.candidate-k}")
    private int candidateK;

    @Value("${rag.rrf-top-k}")
    private int rrfTopK;

    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    @Override
    public Result<DocumentParseResultVO> parseDocument(Long id) {
        KnowledgeDocument knowledgeDocument = baseMapper.selectById(id);

        if(knowledgeDocument == null){
            return Result.error(404,"文档不存在");
        }

        //修改数据库状态判断并修改状态保证原子性
        int updated = baseMapper.markProcessingIfAllowed(knowledgeDocument.getId());
        if (updated==0) {
            return Result.error(409,"文档处理中");
        }


        DocumentParseRequestDTO documentParseRequestDTO = new DocumentParseRequestDTO();
        documentParseRequestDTO.setDocumentId(id);
        documentParseRequestDTO.setKnowledgeBaseId(knowledgeDocument.getKnowledgeBaseId());
        documentParseRequestDTO.setFileSize(knowledgeDocument.getFileSize());
        documentParseRequestDTO.setFileType(knowledgeDocument.getFileType());
        documentParseRequestDTO.setName(knowledgeDocument.getName());
        documentParseRequestDTO.setOriginalName(knowledgeDocument.getOriginalName());
        documentParseRequestDTO.setVersion(knowledgeDocument.getVersion());
        String relativePath = knowledgeDocument.getStoragePath();
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        String storagePath = Paths.get(
                System.getProperty("user.dir"),
                storageRoot,
                relativePath
        ).toString();

        documentParseRequestDTO.setStoragePath(storagePath);

        try {
            Result<List<DocumentChunkResponseVO>> aiResponse = aiDocumentClient.parseDocument(documentParseRequestDTO);
            if (aiResponse == null || aiResponse.getCode() == null || aiResponse.getCode() != 200
                    || aiResponse.getData() == null) {
                String errorMessage = safeErrorMessage(
                        aiResponse == null ? "AI 服务未返回响应" : aiResponse.getMessage()
                );
                knowledgeDocument.setStatus("FAILED");
                knowledgeDocument.setErrorMessage(errorMessage);
                baseMapper.updateById(knowledgeDocument);
                return Result.error(500, errorMessage);
            }

            //存入chunk和embedding
            boolean replaceStatus = documentChunkService.replaceChunks(knowledgeDocument, aiResponse.getData());
            if (!replaceStatus) {
                throw new IllegalStateException("Chunk 保存失败");
            }

            //清空错误信息
            knowledgeDocument.setErrorMessage(null);
            knowledgeDocument.setStatus("READY");
            baseMapper.updateById(knowledgeDocument);

            DocumentParseResultVO result = new DocumentParseResultVO();
            result.setDocumentId(knowledgeDocument.getId());
            result.setStatus("READY");
            result.setChunkCount(aiResponse.getData().size());

            return Result.success(result);
        } catch (RuntimeException e) {
            String errorMessage = safeErrorMessage(e.getMessage());

            knowledgeDocument.setStatus("FAILED");
            knowledgeDocument.setErrorMessage(errorMessage);
            baseMapper.updateById(knowledgeDocument);

            return Result.error(500, errorMessage);
        }
    }

    @Override
    public Result<RagResponseVO> ragQueryDocument(RagAskRequestDTO requestDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        List<Long> accessibleKnowledgeBaseIds = knowledgeBaseService
                .getAccessibleKnowledgeBaseIds(userId);

        Long requestedKnowledgeBaseId=null;
        if (requestDTO.getKnowledgeBaseIds()!=null && !requestDTO.getKnowledgeBaseIds().isEmpty()){
            requestedKnowledgeBaseId = requestDTO.getKnowledgeBaseIds().get(0);
        }
        
        if (requestedKnowledgeBaseId != null) {
            if (!accessibleKnowledgeBaseIds.contains(requestedKnowledgeBaseId)) {
                return Result.error(403, "无访问知识库权限");
            }
        } else {
            requestDTO.setKnowledgeBaseIds(accessibleKnowledgeBaseIds);
        }

        if (requestDTO.getKnowledgeBaseIds().isEmpty()) {
            return Result.success(null);
        }

        try {
            AiRagRequestDTO  aiRagRequestDTO = new AiRagRequestDTO();
            aiRagRequestDTO.setQuery(requestDTO.getQuery());
            aiRagRequestDTO.setKnowledgeBaseIds(requestDTO.getKnowledgeBaseIds());
            aiRagRequestDTO.setTopK(topK);
            aiRagRequestDTO.setCandidateK(candidateK);
            aiRagRequestDTO.setRrfTopK(rrfTopK);

            Result<RagResponseVO> aiResponse = aiDocumentClient.ragQueryDocument(aiRagRequestDTO);
            return aiResponse == null
                    ? Result.error(502, "AI 检索服务未返回响应")
                    : aiResponse;
        } catch (RuntimeException e) {
            return Result.error(502, "AI 检索服务调用失败");
        }
    }


    private String safeErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "文档解析失败";
        }
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_MESSAGE_LENGTH - 3) + "...";
    }
}
