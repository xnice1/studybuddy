# StudyBuddy

StudyBuddy is a Spring Boot REST API for managing courses, quizzes and questions with role-based access control and JWT authentication. This repository contains the backend server, OpenAPI (Swagger) documentation and integration tests.

---

## Table of contents

- [What is this](#what-is-this)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Quick start (run locally)](#quick-start-run-locally)
- [Configuration / environment variables](#configuration--environment-variables)
- [Authentication & Swagger (how to login from the UI)](#authentication--swagger-how-to-login-from-the-ui)
- [API: important endpoints](#api-important-endpoints)
- [Testing](#testing)
- [Developer notes & troubleshooting](#developer-notes--troubleshooting)
- [Optional: auto-login in Swagger UI (advanced)](#optional-auto-login-in-swagger-ui-advanced)
- [License](#license)

---

## What is this

A small backend service for quizzes and courses with the following goals:

- Simple REST API to manage Users, Courses, Quizzes and Questions
- Role-based authorization (ADMIN, INSTRUCTOR, STUDENT)
- JWT authentication issued by a local `/api/auth/login` endpoint
- OpenAPI/Swagger UI for interactive API exploration
- Integration tests using an embedded H2 database

---

## Features

- Register and login (username + password)
- JWT token issuance and validation
- CRUD for Courses, Quizzes, Questions and Users
- Integration tests using MockMvc
- OpenAPI (springdoc) configuration with UI

---

## Tech stack


- Language & Build: Java 17, Maven
- Core Framework: Spring Boot (Web, Data JPA, Security)
- Database: PostgreSQL (HikariCP connection pool) + optionally H2 for tests
- Security: JWT (jjwt or Spring Security OAuth2 Resource Server)
- ORM: Hibernate (via Spring Data JPA)
- Documentation: Springdoc OpenAPI (Swagger UI)
- Mapping: MapStruct (or manual DTOs)
- Testing: JUnit 5, Mockito, Spring Boot Test
- Migration (optional): Flyway or Liquibase
- CI/CD: GitHub Actions (build, test, deploy)
- Version Control: Git + GitHub

---

## Quick start (run locally)

Requirements:

- Java 17+
- Maven or Gradle

From project root (Maven example):

```bash
./mvnw clean package
./mvnw spring-boot:run
