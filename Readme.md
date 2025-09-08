# StudyBuddy

StudyBuddy is a Spring Boot REST API for managing courses, quizzes and questions with role-based access control and JWT authentication. This repository contains the backend server, OpenAPI (Swagger) documentation and integration tests.

---

## Table of contents

- [What is this](#what-is-this)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Quick start (run locally)](#quick-start-run-locally)

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


- **Language & Build:** Java 17, Maven  
- **Core framework:** Spring Boot (Web, Data JPA, Security)  
- **Database:** PostgreSQL  
- **Security:** JWT (JSON Web Tokens)  
- **ORM:** Hibernate (via Spring Data JPA)  
- **API docs:** Springdoc OpenAPI (Swagger UI)  
- **Mapping:** MapStruct  
- **Testing:** JUnit 5, Mockito, Spring Boot Test  
- **CI/CD:** GitHub Actions  
- **Version control:** Git, GitHub
