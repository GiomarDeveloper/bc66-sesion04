# 🏦 BankX - Transactions Service

## 📋 Descripción

Microservicio reactivo para gestión de movimientos bancarios y evaluación de riesgo, construido con **Spring WebFlux** y
arquitectura reactiva.

## 🚀 Características Principales

- ✅ **API Reactiva** con Spring WebFlux y Netty
- ✅ **Persistencia Reactiva** en MongoDB
- ✅ **Módulo Legacy de Riesgo** con JPA + H2 (bloqueante)
- ✅ **Stream en Tiempo Real** con Server-Sent Events (SSE)
- ✅ **Manejo de Errores** consistente con `@RestControllerAdvice`
- ✅ **Operaciones Bloqueantes** aisladas con `Schedulers.boundedElastic()`

## 🛠️ Tecnologías

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring WebFlux** (Reactivo)
- **MongoDB Reactive**
- **Spring Data JPA** + H2 (Legacy)
- **Project Reactor**
- **Lombok**
- **Validation API**

## ⚙️ Configuración

### Prerrequisitos

- **JDK 17**
- **Maven 3.9+**
- **MongoDB**
- **Postman** (para pruebas)

