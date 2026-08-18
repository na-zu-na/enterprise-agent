package org.cc.enterpriseagent.document.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.document.api.AiDocumentClient;
import org.cc.enterpriseagent.document.vo.DocumentChunkResponseVO;
import org.cc.enterpriseagent.document.dto.DocumentParseRequestDTO;
import org.cc.enterpriseagent.document.entity.KnowledgeDocument;
import org.cc.enterpriseagent.document.mapper.DocumentMapper;
import org.cc.enterpriseagent.document.service.DocumentChunkService;
import org.cc.enterpriseagent.document.service.DocumentService;
import org.cc.enterpriseagent.document.vo.DocumentParseResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, KnowledgeDocument> implements DocumentService{
    @Autowired
    private AiDocumentClient aiDocumentClient;

    @Value("${document.storage-root}")
    private String storageRoot;

    @Autowired
    private DocumentChunkService documentChunkService;

    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    @Override
    public Result<DocumentParseResultVO> parseDocument(Long id) {
        KnowledgeDocument knowledgeDocument = baseMapper.selectById(id);

        if(knowledgeDocument == null){
            return Result.error(404,"文档不存在");
        }

        //添加状态判断
        if (Objects.equals(knowledgeDocument.getStatus(), "PROCESSING")) {
            return Result.error(500,"文档处理中");
        }

        //更新文件状态
        knowledgeDocument.setStatus("PROCESSING");
        baseMapper.updateById(knowledgeDocument);

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

            //存入chunk
            documentChunkService.replaceChunks(knowledgeDocument, aiResponse.getData());

            knowledgeDocument.setStatus("READY");
            //清空错误信息
            knowledgeDocument.setErrorMessage(null);
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

            return Result.error(409,e.getMessage());
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
