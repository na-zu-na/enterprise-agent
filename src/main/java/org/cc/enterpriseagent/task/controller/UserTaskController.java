package org.cc.enterpriseagent.task.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cc.enterpriseagent.common.utils.Result;
import org.cc.enterpriseagent.task.dto.CreateUserTaskRequestDTO;
import org.cc.enterpriseagent.task.dto.UpdateUserTaskRequestDTO;
import org.cc.enterpriseagent.task.dto.UpdateUserTaskStatusRequestDTO;
import org.cc.enterpriseagent.task.service.UserTaskService;
import org.cc.enterpriseagent.task.vo.UserTaskVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserTaskController {

    private final UserTaskService userTaskService;

    @PostMapping("/tasks")
    public Result<UserTaskVO> createTask(
            @Valid @RequestBody CreateUserTaskRequestDTO requestDTO) {
        return userTaskService.createTask(requestDTO);
    }

    @GetMapping("/tasks")
    public Result<List<UserTaskVO>> getMyTasks() {
        return userTaskService.getMyTasks();
    }

    @PutMapping("/tasks/{id}")
    public Result<UserTaskVO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserTaskRequestDTO requestDTO) {
        return userTaskService.updateTask(id, requestDTO);
    }

    @PutMapping("/tasks/{id}/status")
    public Result<Void> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserTaskStatusRequestDTO requestDTO) {
        return userTaskService.updateTaskStatus(id, requestDTO);
    }

    @DeleteMapping("/tasks/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        return userTaskService.deleteTask(id);
    }
}
