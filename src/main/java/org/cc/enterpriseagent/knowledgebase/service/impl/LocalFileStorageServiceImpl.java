package org.cc.enterpriseagent.knowledgebase.service.impl;

import org.cc.enterpriseagent.knowledgebase.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {
    private static final String UPLOAD_DIR = "src/main/resources/static/doc";

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new RuntimeException("文件名不能为空");
        }

        //判断文件格式
        originalFilename = Paths.get(originalFilename).getFileName().toString();
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0) {
            throw new RuntimeException("文件缺少扩展名");
        }

        String extension = originalFilename
                .substring(dotIndex + 1)
                .toLowerCase();

        //重命名文件
        String storedFilename = UUID.randomUUID() + "." + extension;

        try {
            Path uploadDir = Paths.get(
                    System.getProperty("user.dir"),
                    UPLOAD_DIR
            );

            Files.createDirectories(uploadDir);

            Path targetPath = uploadDir.resolve(storedFilename);

            file.transferTo(targetPath.toFile());

            // 返回相对访问路径
            return "/doc/" + storedFilename;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public void delete(String filePath) {

    }

    @Override
    public Resource load(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new RuntimeException("文件路径不能为空");
        }

        // /doc/xxx.md -> xxx.md
        String filename = Paths.get(filePath)
                .getFileName()
                .toString();

        Path path = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR, filename).normalize();

        if (!Files.exists(path)) {
            throw new RuntimeException("文件不存在");
        }

        if (!Files.isRegularFile(path)) {
            throw new RuntimeException("文件路径无效");
        }

        return new FileSystemResource(path);
    }
}
