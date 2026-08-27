package org.cc.enterpriseagent.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.cc.enterpriseagent.common.UserContext;
import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.task.dto.CreateUserTaskRequestDTO;
import org.cc.enterpriseagent.task.dto.UpdateUserTaskRequestDTO;
import org.cc.enterpriseagent.task.dto.UpdateUserTaskStatusRequestDTO;
import org.cc.enterpriseagent.task.entity.UserTask;
import org.cc.enterpriseagent.task.mapper.UserTaskMapper;
import org.cc.enterpriseagent.task.service.UserTaskService;
import org.cc.enterpriseagent.task.vo.UserTaskVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserTaskServiceImpl extends ServiceImpl<UserTaskMapper, UserTask>
        implements UserTaskService {

    @Override
    @Transactional
    public Result<UserTaskVO> createTask(CreateUserTaskRequestDTO requestDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        LocalDateTime now = LocalDateTime.now();
        UserTask task = new UserTask();
        task.setUserId(userId);
        task.setTitle(requestDTO.getTitle().trim());
        task.setDescription(requestDTO.getDescription());
        task.setStatus("TODO");
        task.setDueAt(requestDTO.getDueAt());
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setDeleted(false);

        baseMapper.insert(task);

        UserTaskVO taskVO = new UserTaskVO();
        BeanUtils.copyProperties(task, taskVO);
        return Result.success(taskVO);
    }

    @Override
    public Result<List<UserTaskVO>> getMyTasks() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        List<UserTaskVO> tasks = baseMapper.selectList(
                        new LambdaQueryWrapper<UserTask>()
                                .eq(UserTask::getUserId, userId)
                                .orderByAsc(UserTask::getDueAt)
                                .orderByDesc(UserTask::getCreatedAt)
                ).stream()
                .map(task -> {
                    UserTaskVO taskVO = new UserTaskVO();
                    BeanUtils.copyProperties(task, taskVO);
                    return taskVO;
                })
                .toList();

        return Result.success(tasks);
    }

    @Override
    @Transactional
    public Result<UserTaskVO> updateTask(Long taskId, UpdateUserTaskRequestDTO requestDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        int updated = baseMapper.update(null, new LambdaUpdateWrapper<UserTask>()
                .eq(UserTask::getId, taskId)
                .eq(UserTask::getUserId, userId)
                .set(UserTask::getTitle, requestDTO.getTitle().trim())
                .set(UserTask::getDescription, requestDTO.getDescription())
                .set(UserTask::getDueAt, requestDTO.getDueAt())
                .set(UserTask::getUpdatedAt, LocalDateTime.now()));
        if (updated == 0) {
            return Result.error(404, "任务不存在或无操作权限");
        }

        UserTask task = baseMapper.selectOne(new LambdaQueryWrapper<UserTask>()
                .eq(UserTask::getId, taskId)
                .eq(UserTask::getUserId, userId));
        UserTaskVO taskVO = new UserTaskVO();
        BeanUtils.copyProperties(task, taskVO);
        return Result.success(taskVO);
    }

    @Override
    @Transactional
    public Result<Void> updateTaskStatus(Long taskId,
                                         UpdateUserTaskStatusRequestDTO requestDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        int updated = baseMapper.update(null, new LambdaUpdateWrapper<UserTask>()
                .eq(UserTask::getId, taskId)
                .eq(UserTask::getUserId, userId)
                .set(UserTask::getStatus, requestDTO.getStatus())
                .set(UserTask::getUpdatedAt, LocalDateTime.now()));

        if (updated == 0) {
            return Result.error(404, "任务不存在或无操作权限");
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> deleteTask(Long taskId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        int deleted = baseMapper.delete(new LambdaQueryWrapper<UserTask>()
                .eq(UserTask::getId, taskId)
                .eq(UserTask::getUserId, userId));

        if (deleted == 0) {
            return Result.error(404, "任务不存在或无操作权限");
        }
        return Result.success();
    }
}
