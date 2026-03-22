package com.gesinski.remote_command_executor.controller

import com.gesinski.remote_command_executor.dto.CreateExecutionRequest
import com.gesinski.remote_command_executor.service.ExecutionService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/executions")
class ExecutionController(
    private val executionService: ExecutionService
) {

    @PostMapping
    fun createExecution(@RequestBody request: CreateExecutionRequest): Map<String, String> {
        val execution = executionService.createExecution(request.command, request.cpuCount)
        return mapOf("executionId" to execution.id)
    }

    @GetMapping("/{id}")
    fun getExecution(@PathVariable id: String) =
        executionService.getExecution(id)
}