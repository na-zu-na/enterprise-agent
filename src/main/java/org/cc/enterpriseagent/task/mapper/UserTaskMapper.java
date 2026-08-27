package org.cc.enterpriseagent.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.cc.enterpriseagent.task.entity.UserTask;

@Mapper
public interface UserTaskMapper extends BaseMapper<UserTask> {
}
