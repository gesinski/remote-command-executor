# Remote Command Executor

A Spring Boot application for executing shell commands on remote AWS EC2 instances with task queuing, status tracking, and PostgreSQL integration.

## Overview

The project demonstrates a complete workflow for remote command execution:

* Users submit commands to be executed with optional CPU count.
* Tasks are queued and processed asynchronously.
* ExecutionWorker picks up queued tasks and runs them on AWS EC2 instances via SSH.
* Task statuses are tracked (`QUEUED`, `IN_PROGRESS`, `FINISHED`, `FAILED`).
* Results and output are stored in PostgreSQL.
* Logging is implemented for audit and debugging.

## Technologies Used

* **Kotlin** - main programming language.
* **Spring Boot** - application framework for web and scheduled workers.
* **Spring Scheduling** - asynchronous task execution.
* **PostgreSQL** - relational database for persisting tasks and outputs.
* **Hibernate / Spring Data JPA** - ORM for database interactions.
* **AWS EC2** - remote execution environment.
* **JSch** - SSH connections to EC2 instances.
* **Gradle** - build tool.
* **SLF4J / Logback** - logging framework.

## Architecture

1. **User Input**: Command is submitted via REST API or console app.
2. **ExecutionService**: Persists the task and assigns initial status `QUEUED`.
3. **ExecutionWorker**: Scheduled component that picks up `QUEUED` tasks, updates status to `IN_PROGRESS`, and executes them.
4. **SshService**: Connects to an EC2 instance via SSH, runs the command, captures output.
5. **Database Update**: Task status and output are updated in PostgreSQL.
6. **Completion**: Status is changed to `FINISHED` or `FAILED`, output is available to user.

## Project Structure

```
└── src
    └── main
        ├── kotlin
        │   └── com
        │       └── gesinski
        │           └── remote_command_executor
        │               ├── Application.kt
        │               ├── aws
        │               │   ├── Ec2Service.kt
        │               │   └── SshService.kt
        │               ├── config
        │               │   └── AwsConfig.kt
        │               ├── ConsoleApp.kt
        │               ├── controller
        │               │   └── ExecutionController.kt
        │               ├── dto
        │               │   └── CreateExecutionRequest.kt
        │               ├── model
        │               │   └── Execution.kt
        │               ├── repository
        │               │   └── ExecutionRepository.kt
        │               ├── service
        │               │   └── ExecutionService.kt
        │               └── worker
        │                   └── ExecutionWorker.kt
        └── resources
            ├── application-cli.yaml
            ├── application.yaml
            ├── static
            └── templates
├── build.gradle.kts
├── README.md
├── settings.gradle.kts
└── .gitignore
```

## Key Concepts

* **Task Queueing**: Tasks are stored in PostgreSQL and processed asynchronously.
* **Remote Execution**: EC2 instances are created dynamically for execution and terminated after completion.
* **SSH Integration**: Commands are executed securely using JSch.
* **Logging**: All steps are logged to a file for auditing.
* **Extensibility**: Supports multiple commands, multiple tasks, and can be extended to other cloud providers.

## Requirements

* Java 17
* Gradle 8+
* Kotlin 1.9+
* PostgreSQL 15+ (local or remote)
* AWS account with EC2 access and SSH key pair

## Configuration

### PostgreSQL

1. Create a database and user:

```bash
createdb remote_exec_db
psql -U postgres
CREATE USER app_user WITH PASSWORD '<password>';
GRANT ALL PRIVILEGES ON DATABASE remote_exec_db TO app_user;
```

2. Use placeholders in `application-sample.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/remote_exec_db
    username: app_user
    password: <password>
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### AWS

1. Create an IAM User with EC2 permissions.
2. Generate Access Key and Secret Key.
3. Create an EC2 key pair (`.pem`) and set permissions:

```bash
chmod 400 mykey.pem
```

4. Use placeholders in `application-sample.yaml`:

```yaml
aws:
  access-key: ${AWS_ACCESS_KEY_ID}
  secret-key: ${AWS_SECRET_ACCESS_KEY}
  region: eu-north-1
  key-path: ${EC2_KEY_PATH}
```

5. Set environment variables locally:

```bash
export AWS_ACCESS_KEY_ID=AKIA...
export AWS_SECRET_ACCESS_KEY=...
export EC2_KEY_PATH=/home/user/mykey.pem
```

Spring Boot will read these variables automatically.

## Running the Application

### Web Application (Spring Boot + REST API)

```bash
./gradlew bootRun
```

* Default port: 8080
* REST endpoints:

| Endpoint         | Method | Description                     |
| ---------------- | ------ | ------------------------------- |
| /executions      | POST   | Create a new task               |
| /executions/{id} | GET    | Get status and output of a task |

### Console Application (CUI)

```bash
./gradlew run
```

* Enter commands one by one
* Task statuses update automatically
* Command output is displayed after completion

## Logging

* Logs are written to `logs/app.log`
* Log level can be configured in `application.yaml`:

```yaml
logging:
  file:
    name: logs/app.log
  level:
    root: INFO
```

## Testing

1. Create tasks with simple commands:

```bash
echo hello
```

2. Check logs for execution:

```
INFO ExecutionWorker: Starting task ...
INFO SshService: SSH connected to ...
```

3. After completion, output is stored in the database and printed in the console.
