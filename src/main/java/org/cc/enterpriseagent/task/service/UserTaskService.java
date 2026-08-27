package org.cc.enterpriseagent.task.service;

import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.task.dto.CreateUserTaskRequestDTO;
import org.cc.enterpriseagent.task.dto.UpdateUserTaskRequestDTO;
import org.cc.enterpriseagent.task.dto.UpdateUserTaskStatusRequestDTO;
import org.cc.enterpriseagent.task.vo.UserTaskVO;

import java.util.List;

public interface UserTaskService {

    Result<UserTaskVO> createTask(CreateUserTaskRequestDTO requestDTO);

    Result<List<UserTaskVO>> getMyTasks();

    Result<UserTaskVO> updateTask(Long taskId, UpdateUserTaskRequestDTO requestDTO);

    Result<Void> updateTaskStatus(Long taskId, UpdateUserTaskStatusRequestDTO requestDTO);

    Result<Void> deleteTask(Long taskId);
}
