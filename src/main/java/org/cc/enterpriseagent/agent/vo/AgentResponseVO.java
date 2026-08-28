package org.cc.enterpriseagent.agent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.cc.enterpriseagent.document.vo.CitationVO;

import java.util.List;
import java.util.Map;

@Data
public class AgentResponseVO {
    private Long conversationId;

    private String answer;

    private List<CitationVO> citations;

    private String title;

    private Map<String, Object> checkpoint;

    private String status;

    private ApprovalVO approval;
}
