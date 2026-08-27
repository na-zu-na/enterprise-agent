package org.cc.enterpriseagent.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.cc.enterpriseagent.agent.entity.AgentMessage;

@Mapper
public interface AgentMessageMapper extends BaseMapper<AgentMessage> {
}
