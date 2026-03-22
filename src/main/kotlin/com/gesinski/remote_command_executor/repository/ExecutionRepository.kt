package com.gesinski.remote_command_executor.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.gesinski.remote_command_executor.model.Execution
import com.gesinski.remote_command_executor.model.ExecutionStatus

interface ExecutionRepository : JpaRepository<Execution, String> {
    fun findByStatus(status: ExecutionStatus): List<Execution>
}