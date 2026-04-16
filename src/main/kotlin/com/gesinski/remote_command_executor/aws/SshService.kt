package com.gesinski.remote_command_executor.aws

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SshService(
    @Value("\${ec2.key-path}") private val keyPath: String
) {

    private val logger = LoggerFactory.getLogger(SshService::class.java)

    fun executeCommand(
        ip: String,
        command: String,
        user: String = "ubuntu",
        timeoutSeconds: Long
    ): String {

        val process = ProcessBuilder(
            "ssh",
            "-i", keyPath,
            "-o", "StrictHostKeyChecking=no",
            "-o", "ConnectTimeout=10",
            "$user@$ip",
            command
        )
            .redirectErrorStream(true)
            .start()

        val finished = process.waitFor(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)

        if (!finished) {
            process.destroyForcibly()
            throw RuntimeException("SSH command timeout after $timeoutSeconds seconds")
        }

        val output = process.inputStream.bufferedReader().readText()

        logger.info("SSH executed on $ip with output length=${output.length}")

        return output
    }

    fun waitForSshReady(
        ip: String,
        user: String = "ubuntu",
        retries: Int = 30,
        delayMs: Long = 5000
    ) {

        repeat(retries) { attempt ->

            val process = ProcessBuilder(
                "ssh",
                "-i", keyPath,
                "-o", "StrictHostKeyChecking=no",
                "-o", "ConnectTimeout=5",
                "$user@$ip",
                "echo READY"
            )
                .redirectErrorStream(true)
                .start()

            val finished = process.waitFor(7, java.util.concurrent.TimeUnit.SECONDS)
            val output = if (finished) process.inputStream.bufferedReader().readText() else ""

            if (finished && output.contains("READY")) {
                logger.info("SSH READY for $ip")
                return
            }

            logger.warn("SSH not ready [$attempt/$retries] for $ip")

            Thread.sleep(delayMs)
        }

        throw RuntimeException("SSH not ready after $retries retries for $ip")
    }
}