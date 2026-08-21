package org.cc.enterpriseagent.knowledgebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.common.UserContext;
import org.cc.enterpriseagent.document.entity.KnowledgeDocument;
import org.cc.enterpriseagent.document.event.DocumentUploadedEvent;
import org.cc.enterpriseagent.document.mapper.DocumentMapper;
import org.cc.enterpriseagent.document.vo.*;
import org.cc.enterpriseagent.document.dto.UpdateDocumentNameRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.CreateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.dto.UpdateKnowledgeBaseRequestDTO;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBase;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBaseMember;
import org.cc.enterpriseagent.knowledgebase.mapper.KnowledgeBaseMapper;
import org.cc.enterpriseagent.knowledgebase.mapper.KnowledgeBaseMemberMapper;
import org.cc.enterpriseagent.knowledgebase.service.FileStorageService;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseService;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseDetailVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseListVO;
import org.cc.enterpriseagent.knowledgebase.vo.KnowledgeBaseVO;
import org.cc.enterpriseagent.user.entity.SysUser;
import org.cc.enterpriseagent.user.mapper.SysUserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper,KnowledgeBase> implements KnowledgeBaseService {
    @Autowired
    private KnowledgeBaseMemberMapper knowledgeBaseMemberMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public Result<KnowledgeBaseVO> createKnowledgeBase(CreateKnowledgeBaseRequestDTO requestDTO) {
        Long ownerId = UserContext.getUserId();

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        BeanUtils.copyProperties(requestDTO, knowledgeBase);
        knowledgeBase.setOwnerId(ownerId);
        knowledgeBase.setStatus((short) 1);
        knowledgeBase.setDocumentCount(0);
        knowledgeBase.setMemberCount(1);
        baseMapper.insert(knowledgeBase);

        KnowledgeBaseMember ownerMember = new KnowledgeBaseMember();
        ownerMember.setKnowledgeBaseId(knowledgeBase.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setMemberRole("ADMIN");
        ownerMember.setCreatedBy(ownerId);
        knowledgeBaseMemberMapper.insert(ownerMember);

        KnowledgeBaseVO knowledgeBaseVO = new KnowledgeBaseVO();
        BeanUtils.copyProperties(knowledgeBase, knowledgeBaseVO);
        return Result.success(knowledgeBaseVO);
    }

    @Override
    public Result<List<KnowledgeBaseListVO>> getAccessibleKnowledgeBases() {
        Long userId = UserContext.getUserId();
        List<KnowledgeBaseListVO> knowledgeBases = knowledgeBaseMemberMapper.selectAccessibleKnowledgeBases(userId);
        return new Result<>(knowledgeBases, "查询成功", 200);
    }

    @Override
    public List<Long> getAccessibleKnowledgeBaseIds(Long userId) {
        return userId == null ? List.of()
                : knowledgeBaseMemberMapper.selectAccessibleKnowledgeBaseIds(userId);
    }

    @Override
    public Result<KnowledgeBaseDetailVO> getKnowledgeBaseDetail(Long knowledgeBaseId) {
        KnowledgeBaseDetailVO knowledgeBaseDetail = knowledgeBaseMemberMapper
                .selectAccessibleKnowledgeBaseDetail(knowledgeBaseId, UserContext.getUserId());
        if (knowledgeBaseDetail == null) {
            return Result.error(404, "知识库不存在或无访问权限");
        }
        return new Result<>(knowledgeBaseDetail, "查询成功", 200);
    }

    @Override
    @Transactional
    public Result<Void> updateKnowledgeBase(Long knowledgeBaseId, UpdateKnowledgeBaseRequestDTO requestDTO) {
        Long userId = UserContext.getUserId();

        KnowledgeBase knowledgeBase = baseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            return Result.error(404, "知识库不存在");
        }

        if (canUpdateKnowledgeBase(knowledgeBase, userId)) {
            return Result.error(403, "无修改知识库权限");
        }

        BeanUtils.copyProperties(requestDTO, knowledgeBase);
        knowledgeBase.setStatus(requestDTO.getStatus().shortValue());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(knowledgeBase);
        return new Result<>(null, "修改成功", 200);
    }

    @Override
    @Transactional
    public Result<Void> deleteKnowledgeBase(Long knowledgeBaseId) {
        Long userId = UserContext.getUserId();
        KnowledgeBase knowledgeBase = baseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            return Result.error(404, "知识库不存在");
        }

        SysUser currentUser = sysUserMapper.selectById(userId);
        boolean isOwner = userId != null && userId.equals(knowledgeBase.getOwnerId());
        boolean isSystemAdmin = currentUser != null && "SYSTEM_ADMIN".equals(currentUser.getRoleCode());
        if (!isOwner && !isSystemAdmin) {
            return Result.error(403, "无删除知识库权限");
        }

        //执行逻辑删除而非物理删除
        baseMapper.deleteById(knowledgeBaseId);
        return new Result<>(null, "删除成功", 200);
    }

    @Override
    @Transactional
    public Result<DocumentUploadVO> uploadDocument(Long knowledgeBaseId, MultipartFile file) {
        Long userId = UserContext.getUserId();
        KnowledgeBase knowledgeBase = baseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            return Result.error(404, "知识库不存在");
        }

        if (canUploadDocument(knowledgeBase, userId)) {
            return Result.error(403, "无修改知识库权限");
        }

        if (file == null || file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }

        if (file.getSize() > 20L * 1024 * 1024) {
            return Result.error(400, "文件大小不能超过20MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return Result.error(400, "文件名不能为空");
        }

        //判断文件名和后缀
        originalFilename = Paths.get(originalFilename)
                .getFileName()
                .toString();

        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0) {
            return Result.error(400, "文件缺少扩展名");
        }

        String extension = originalFilename
                .substring(dotIndex + 1)
                .toLowerCase();

        if (!"txt".equals(extension) && !"md".equals(extension)) {
            return Result.error(400, "当前仅支持 .txt 和 .md 文件");
        }

        String documentName = originalFilename.substring(0, dotIndex);

        // 文件存储交给独立类
        String filePath = fileStorageService.upload(file);

        //上传文档
        KnowledgeDocument document = new KnowledgeDocument();
        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setName(documentName);
        document.setOriginalName(originalFilename);
        document.setFileType(extension);
        document.setFileSize(file.getSize());
        document.setStoragePath(filePath);
        document.setStatus("UPLOADED");
        document.setVersion(1);
        document.setUploadedBy(userId);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        documentMapper.insert(document);

        //发布事件
        applicationEventPublisher.publishEvent(
                new DocumentUploadedEvent(document.getId())
        );

        //修改知识库
        baseMapper.update(null, new LambdaUpdateWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getId, knowledgeBaseId)
                .setSql("document_count = document_count + 1")
                .set(KnowledgeBase::getUpdatedAt, LocalDateTime.now()));

        SysUser uploader = sysUserMapper.selectById(userId);

        DocumentUploadVO vo = new DocumentUploadVO();
        BeanUtils.copyProperties(document, vo);
        vo.setUploaderName(
                uploader != null ? uploader.getNickname() : null
        );

        return new Result<>(vo, "上传成功", 200);
    }

    @Override
    public Result<DocumentPageVO> getKnowledgeBaseDocuments(Long knowledgeBaseId, Integer page, Integer size,
                                                             String keyword, String status, String fileType) {
        if (page == null || page < 1 || size == null || size < 1) {
            return Result.error(400, "页码和每页数量必须大于 0");
        }

        KnowledgeBase knowledgeBase = baseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            return Result.error(404, "知识库不存在");
        }

        if (canAccessKnowledgeBase(knowledgeBase, UserContext.getUserId())) {
            return Result.error(403, "无访问知识库权限");
        }

        Page<DocumentListVO> pageRequest = new Page<>(page, size);
        IPage<DocumentListVO> pageResult = documentMapper.selectDocumentPage(
                pageRequest, knowledgeBaseId, keyword, status, fileType);
        DocumentPageVO documentPage = new DocumentPageVO(
                pageResult.getRecords(),
                pageResult.getTotal(),
                page,
                size);
        return new Result<>(documentPage, "查询成功", 200);
    }

    @Override
    public Result<DocumentDetailVO> getDocumentDetail(Long documentId) {
        DocumentDetailVO documentDetail = documentMapper.selectDocumentDetail(documentId);
        if (documentDetail == null) {
            return Result.error(404, "文档不存在");
        }

        KnowledgeBase knowledgeBase = baseMapper.selectById(documentDetail.getKnowledgeBaseId());
        if (canAccessKnowledgeBase(knowledgeBase, UserContext.getUserId())) {
            return Result.error(403, "无访问知识库权限");
        }
        return new Result<>(documentDetail, "查询成功", 200);
    }

    @Override
    @Transactional
    public Result<Void> updateDocumentName(Long documentId, UpdateDocumentNameRequestDTO requestDTO) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            return Result.error(404, "文档不存在");
        }

        KnowledgeBase knowledgeBase = baseMapper.selectById(document.getKnowledgeBaseId());
        if (canUploadDocument(knowledgeBase, UserContext.getUserId())) {
            return Result.error(403, "无修改文档权限");
        }

        documentMapper.update(null, new LambdaUpdateWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getId, documentId)
                .set(KnowledgeDocument::getName, requestDTO.getName().trim())
                .set(KnowledgeDocument::getUpdatedAt, LocalDateTime.now()));
        return new Result<>(null, "文档修改成功", 200);
    }

    @Override
    @Transactional
    public Result<Void> deleteDocument(Long documentId) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            return Result.error(404, "文档不存在");
        }

        KnowledgeBase knowledgeBase = baseMapper.selectById(document.getKnowledgeBaseId());
        if (canUploadDocument(knowledgeBase, UserContext.getUserId())) {
            return Result.error(403, "无删除文档权限");
        }

        documentMapper.deleteById(documentId);
        baseMapper.update(null, new LambdaUpdateWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getId, document.getKnowledgeBaseId())
                .setSql("document_count = GREATEST(document_count - 1, 0)")
                .set(KnowledgeBase::getUpdatedAt, LocalDateTime.now()));
        return new Result<>(null, "文档删除成功", 200);
    }

    @Override
    public ResponseEntity<Resource> downloadDocument(Long documentId) {
        Long userId=UserContext.getUserId();

        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        KnowledgeBase knowledgeBase = baseMapper.selectById(document.getKnowledgeBaseId());
        if (canAccessKnowledgeBase(knowledgeBase, UserContext.getUserId())) {
            throw new RuntimeException("无访问数据库权限");
        }

        //加载数据
        Resource resource = fileStorageService.load(document.getStoragePath());

        String originalName = document.getOriginalName();
        //对文件名进行url编码
        String encodedFilename = URLEncoder.encode(
                originalName,
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFilename
                    )
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result<DocumentPreviewVO> previewDocument(Long id) {
        Long  userId=UserContext.getUserId();

        KnowledgeDocument document = documentMapper.selectById(id);
        if (document == null) {
            return Result.error(404, "文档不存在");
        }

        KnowledgeBase knowledgeBase = baseMapper.selectById(document.getKnowledgeBaseId());
        if (canAccessKnowledgeBase(knowledgeBase, userId)) {
            return Result.error(403, "暂无权限");
        }

        Resource file = fileStorageService.load(document.getStoragePath());

        String content;
        try (InputStream inputStream = file.getInputStream()) {
            content=new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取问内容失败",e);
        }

        DocumentPreviewVO documentPreviewVO = new DocumentPreviewVO();
        BeanUtils.copyProperties(document, documentPreviewVO);
        documentPreviewVO.setContent(content);

        return Result.success(documentPreviewVO);
    }

    private boolean canUpdateKnowledgeBase(KnowledgeBase knowledgeBase,Long userId) {
        if (knowledgeBase == null) {
            return true;
        }

        if (userId == null) {
            return true;
        }

        boolean isOwner = userId.equals(knowledgeBase.getOwnerId());

        SysUser currentUser = sysUserMapper.selectById(userId);
        boolean isSystemAdmin = currentUser != null
                && "SYSTEM_ADMIN".equals(currentUser.getRoleCode());

        boolean isKnowledgeBaseAdmin = knowledgeBaseMemberMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeBaseMember>()
                        .eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBase.getId())
                        .eq(KnowledgeBaseMember::getUserId, userId)
                        .eq(KnowledgeBaseMember::getMemberRole, "ADMIN")
        ) != null;

        return !isOwner && !isKnowledgeBaseAdmin && !isSystemAdmin;
    }

    private boolean canUploadDocument(KnowledgeBase knowledgeBase, Long userId) {
        if (knowledgeBase == null || userId == null) {
            return true;
        }

        // 知识库创建人
        boolean isOwner = userId.equals(knowledgeBase.getOwnerId());

        // 系统管理员
        SysUser currentUser = sysUserMapper.selectById(userId);
        boolean isSystemAdmin = currentUser != null
                && "SYSTEM_ADMIN".equals(currentUser.getRoleCode());

        // 知识库 ADMIN 或 EDITOR
        boolean isKnowledgeBaseEditorOrAdmin = knowledgeBaseMemberMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeBaseMember>()
                        .eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBase.getId())
                        .eq(KnowledgeBaseMember::getUserId, userId)
                        .in(KnowledgeBaseMember::getMemberRole, "ADMIN", "EDITOR")
        ) != null;

        return !isOwner && !isKnowledgeBaseEditorOrAdmin && !isSystemAdmin;
    }

    public boolean canAccessKnowledgeBase(KnowledgeBase knowledgeBase, Long userId) {
        if (knowledgeBase == null || userId == null) {
            return true;
        }

        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null) {
            return true;
        }

        if ("SYSTEM_ADMIN".equals(currentUser.getRoleCode())
                || userId.equals(knowledgeBase.getOwnerId())) {
            return false;
        }

        if ("PUBLIC".equals(knowledgeBase.getVisibility())
                && Short.valueOf((short) 1).equals(currentUser.getStatus())) {
            return false;
        }

        return knowledgeBaseMemberMapper.selectOne(new LambdaQueryWrapper<KnowledgeBaseMember>()
                .eq(KnowledgeBaseMember::getKnowledgeBaseId, knowledgeBase.getId())
                .eq(KnowledgeBaseMember::getUserId, userId)) == null;
    }
}
