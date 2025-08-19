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

- Java 17+
- Spring Boot (Web, Security, Data JPA, Validation)
- Spring Security (JWT)
- H2 (test/dev), any SQL DB in production
- jjwt for JWT handling
- springdoc-openapi / Swagger UI
- JUnit 5, MockMvc for integration tests

---

## Quick start (run locally)

Requirements:

- Java 17+
- Maven or Gradle

From project root (Maven example):

```bash
./mvnw clean package
./mvnw spring-boot:run
