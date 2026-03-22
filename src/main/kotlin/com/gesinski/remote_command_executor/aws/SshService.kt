package com.gesinski.remote_command_executor.aws

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
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
        retries: Int = 12,
        sleepMs: Long = 10000
    ): String {
        val jsch = JSch()
        jsch.addIdentity(keyPath)

        var lastException: Exception? = null

        repeat(retries) { attempt ->
            try {
                val session = jsch.getSession("ubuntu", ip, 22)
                session.setConfig("StrictHostKeyChecking", "no")
                session.connect(5000)

                val channel = session.openChannel("exec") as ChannelExec
                channel.setCommand(command)
                channel.inputStream = null
                val input = channel.inputStream
                channel.connect()

                val output = input.bufferedReader().readText()

                channel.disconnect()
                session.disconnect()

                logger.info("SSH command executed successfully on $ip")
                return output
            } catch (e: Exception) {
                lastException = e
                logger.warn("SSH attempt ${attempt + 1} failed for $ip, retrying in ${sleepMs / 1000}s...")
                Thread.sleep(sleepMs)
            }
        }

        throw RuntimeException("SSH connection failed after $retries retries", lastException)
    }
}