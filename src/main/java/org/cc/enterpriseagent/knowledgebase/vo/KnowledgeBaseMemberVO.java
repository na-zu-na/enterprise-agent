package org.cc.enterpriseagent.knowledgebase.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeBaseMemberVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String memberRole;
    private LocalDateTime joinedAt;
}
