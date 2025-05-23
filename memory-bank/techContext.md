# Tech Context: Kafra

## Technologies Used

- **Language:** Java
- **Framework:** Spring Boot
- **Build Tool:** Maven
- **Persistence:** Spring Data JPA (inferred)
- **Database:** Relational Database (inferred, specific type not yet determined)
- **Logging:** Logback (based on `logback-spring.xml`)
- **AI Integration:** Gemini AI (based on `GeminiAIService.java`)
- **Discord Integration:** JDA (Java Discord API)

## Required Properties

- `spring.ai.gemini_api_key`: API key for Gemini AI integration.
- `spring.jda.token`: Token for the Discord bot.

## Development Setup

- Standard Java development environment.
- Maven installation.
- Database connection configuration (details likely in `application.properties` or similar).

## Technical Constraints

- Adherence to Java and Spring Boot best practices.
- Compatibility with chosen database system.

## Dependencies

- Managed by Maven (`pom.xml`). Key dependencies likely include Spring Boot starters, database connector, and potentially AI client libraries.

## Tool Usage Patterns

- Maven for building, testing, and running the application.
- Standard IDE for Java development.
