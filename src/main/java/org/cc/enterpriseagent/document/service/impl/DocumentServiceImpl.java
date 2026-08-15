package org.cc.enterpriseagent.document.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.common.Result;
import org.cc.enterpriseagent.document.api.AiDocumentClient;
import org.cc.enterpriseagent.document.dto.DocumentParseRequestDTO;
import org.cc.enterpriseagent.document.dto.DocumentParseResponseVO;
import org.cc.enterpriseagent.document.entity.KnowledgeDocument;
import org.cc.enterpriseagent.document.mapper.DocumentMapper;
import org.cc.enterpriseagent.document.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.nio.file.Paths;

@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, KnowledgeDocument> implements DocumentService{
    @Autowired
    private AiDocumentClient aiDocumentClient;

    @Value("${document.storage-root}")
    private String storageRoot;

    @Override
    public Result<DocumentParseResponseVO> parseDocument(Long id) {
        KnowledgeDocument knowledgeDocument = baseMapper.selectById(id);

        if(knowledgeDocument == null){
            return Result.error(404,"文档不存在");
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
            Result<DocumentParseResponseVO> aiResponse = aiDocumentClient.parseDocument(documentParseRequestDTO);
            if (aiResponse == null || aiResponse.getCode() == null || aiResponse.getCode() != 200
                    || aiResponse.getData() == null) {
                String errorMessage = aiResponse == null ? "AI 服务未返回响应" : aiResponse.getMessage();
                knowledgeDocument.setStatus("FAILED");
                knowledgeDocument.setErrorMessage(errorMessage);
                baseMapper.updateById(knowledgeDocument);
                return Result.error(500, errorMessage);
            }

            knowledgeDocument.setStatus("READY");
            baseMapper.updateById(knowledgeDocument);

            return Result.success(aiResponse.getData());
        } catch (RestClientException e) {
            knowledgeDocument.setStatus("FAILED");
            knowledgeDocument.setErrorMessage(e.getMessage());
            baseMapper.updateById(knowledgeDocument);

            return Result.error(500,e.getMessage());
        }


    }
}
