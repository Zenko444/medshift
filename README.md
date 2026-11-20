# MedShift

Medical staff scheduling and timesheet management system, developed for the Modelling and Development of Systems course (SDBIS master, FEAA Iași). The pilot scenario is Arcadia Hospital Iași.

The application covers the monthly shift schedule of a hospital department, employee preferences, leave requests, shift swaps between colleagues, and the generation of timesheets and the payroll export.

## Technologies

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA (Hibernate)
- H2 in-memory database
- JUnit 5
- Maven

## Running the project

Requirements: JDK 21 and Maven (or IntelliJ IDEA).

    mvn clean test
    mvn spring-boot:run

The database is created at startup from the entity classes and dropped on shutdown. Demo data is loaded automatically: three departments (ICU, Cardiology, General Surgery), the ICU staff and one monthly schedule.

## Project structure

The project follows a Domain-Driven Design layered architecture:

    org.medshift
    ├── domain        entities, value objects, business rules
    ├── services      repositories, factories, domain services, scheduling strategies
    ├── application   workflow services (use cases)
    └── config        Spring configuration

The domain is split into three bounded contexts:

- **planificare** (scheduling): GraficLunar, Tura, Asignare, MatriceAcoperire
- **personal** (staff): Angajat, DoleanteLunare, Concediu, CerereSchimbTura
- **pontaj** (timesheets): FisaPontaj, LiniePontaj, BorderouSalarial

Shared classes such as Sectie, Perioada, LunaCalendaristica, GradMedical and TipTura are in `domain/shared`.

## Main workflows

**Scheduling:** create the monthly schedule, collect preferences, generate the schedule using a selected strategy (fairness, preferences or optimization), publish it, confirm shifts, handle changes and finalize it.

**Staff:** register employees, submit preferences, request and approve leave, request shift swaps (approved automatically between employees of the same grade, otherwise by the head of department).

**Timesheets:** generate timesheets from the finalized schedule, check them against legal rules, approve them in HR, generate and export the payroll file (CSV or XML).

## Sprints

- **Sprint 1** – domain model: entities, value objects, aggregates and business rules
- **Sprint 2** – repositories, factories, domain services, scheduling strategies and workflow services
- **Sprint 3** – persistence with JPA: entity mappings, Spring Data JPA repositories, foreign key references between entities, entity listeners and JPA tests


## Tests

Tests are grouped by layer in `src/test/java`: domain tests without Spring, repository and service tests, and workflow tests. Spring tests run inside a transaction that is rolled back after each test.

    mvn test

## Team

- Name: Andrei Maria-Teodora
- Name – Lungu Christian
- Name – Tăbăcaru Răzvan-Ștefan
