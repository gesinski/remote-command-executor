package com.gesinski.remote_command_executor.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.Ec2Client

@Configuration
class AwsConfig(
    @Value("\${aws.access-key}") private val accessKey: String,
    @Value("\${aws.secret-key}") private val secretKey: String,
    @Value("\${aws.region}") private val region: String
) {

    @Bean
    fun ec2Client(): Ec2Client {
        return Ec2Client.builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .build()
    }
}