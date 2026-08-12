package org.cc.enterpriseagent.knowledgebase.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String upload(MultipartFile file);

    void delete(String filePath);

    Resource load(String filePath);
}
