package com.gesinski.remote_command_executor.service

import com.gesinski.remote_command_executor.model.Execution
import com.gesinski.remote_command_executor.model.ExecutionStatus
import com.gesinski.remote_command_executor.repository.ExecutionRepository
import org.springframework.stereotype.Service

@Service
class ExecutionService(private val repository: ExecutionRepository) {

    fun createExecution(command: String, cpuCount: Int): Execution {
        val execution = Execution(command = command, cpuCount = cpuCount)
        return repository.save(execution)
    }

    fun getExecution(id: String): Execution? = repository.findById(id).orElse(null)

    fun getQueuedExecutions(): List<Execution> = repository.findByStatus(ExecutionStatus.QUEUED)

    fun save(execution: Execution): Execution = repository.save(execution)
}