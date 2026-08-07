package org.cc.enterpriseagent.knowledgebase.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBase;
import org.cc.enterpriseagent.knowledgebase.entity.KnowledgeBaseMember;
import org.cc.enterpriseagent.knowledgebase.mapper.KnowledgeBaseMemberMapper;
import org.cc.enterpriseagent.knowledgebase.service.KnowledgeBaseMemberService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseMemberServiceImpl extends ServiceImpl<KnowledgeBaseMemberMapper, KnowledgeBaseMember> implements KnowledgeBaseMemberService {
}
