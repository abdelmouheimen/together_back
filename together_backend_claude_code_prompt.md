# Prompt Claude Code — Backend "Together" (Java 25 · Spring Boot 4 · PostgreSQL · Docker)

## Contexte du projet

Tu vas développer le **backend REST** de l'application **Together** — une application de listes de tâches partagées pour couples et groupes d'amis.

Le backend expose une **API REST JSON** consommée par l'application mobile Expo (Android & iOS). Il gère les utilisateurs, les listes, les items, les commentaires, les amitiés et les événements d'activité.

---

## Stack technique

| Composant | Technologie | Version |
|---|---|---|
| Langage | Java (LTS) | **25** |
| Framework | Spring Boot | **4.0.x** |
| Framework core | Spring Framework | **7.0.x** |
| Build tool | Maven | 3.9+ |
| Base de données | PostgreSQL | 16 |
| ORM | Spring Data JPA + Hibernate | inclus Spring Boot 4 |
| Migrations BDD | Flyway | inclus Spring Boot 4 |
| Sécurité | Spring Security + JWT (jjwt) | 0.12.x |
| Documentation API | SpringDoc OpenAPI (Swagger UI) | 2.x compatible SB4 |
| Tests | JUnit 5 + Mockito + Testcontainers | inclus |
| Conteneurisation | Docker + Docker Compose | - |
| Qualité | Checkstyle + JSpecify (null safety) | inclus Spring Framework 7 |

> **Note Java 25 :** utiliser les features stables — records, pattern matching, sealed classes, virtual threads (Project Loom via Spring Boot 4), text blocks, switch expressions. Éviter les features en preview.

---

## Architecture des fichiers

```
together-backend/
├── docker-compose.yml              # PostgreSQL + app + pgAdmin
├── docker-compose.dev.yml          # override dev (hot-reload, ports exposés)
├── Dockerfile                      # image de production multi-stage
├── .env.example                    # variables d'environnement à copier en .env
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/together/
    │   │   ├── TogetherApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── JwtConfig.java
    │   │   │   └── OpenApiConfig.java
    │   │   ├── domain/
    │   │   │   ├── user/
    │   │   │   │   ├── User.java              # @Entity
    │   │   │   │   ├── UserRepository.java
    │   │   │   │   ├── UserService.java
    │   │   │   │   └── UserController.java
    │   │   │   ├── friendship/
    │   │   │   │   ├── Friendship.java
    │   │   │   │   ├── FriendshipRepository.java
    │   │   │   │   ├── FriendshipService.java
    │   │   │   │   └── FriendshipController.java
    │   │   │   ├── list/
    │   │   │   │   ├── TodoList.java
    │   │   │   │   ├── TodoListRepository.java
    │   │   │   │   ├── TodoListService.java
    │   │   │   │   └── TodoListController.java
    │   │   │   ├── item/
    │   │   │   │   ├── TodoItem.java
    │   │   │   │   ├── TodoItemRepository.java
    │   │   │   │   ├── TodoItemService.java
    │   │   │   │   └── TodoItemController.java
    │   │   │   ├── comment/
    │   │   │   │   ├── Comment.java
    │   │   │   │   ├── CommentRepository.java
    │   │   │   │   ├── CommentService.java
    │   │   │   │   └── CommentController.java
    │   │   │   └── activity/
    │   │   │       ├── ActivityEvent.java
    │   │   │       ├── ActivityEventRepository.java
    │   │   │       ├── ActivityEventService.java
    │   │   │       └── ActivityEventController.java
    │   │   ├── auth/
    │   │   │   ├── AuthController.java
    │   │   │   ├── AuthService.java
    │   │   │   ├── JwtTokenProvider.java
    │   │   │   └── JwtAuthFilter.java
    │   │   ├── dto/                           # Records Java 25 pour tous les DTOs
    │   │   │   ├── auth/
    │   │   │   ├── user/
    │   │   │   ├── list/
    │   │   │   ├── item/
    │   │   │   ├── comment/
    │   │   │   └── activity/
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── UnauthorizedException.java
    │   │   │   └── ApiError.java              # Record
    │   │   └── util/
    │   │       └── SecurityUtils.java         # récupérer l'utilisateur courant
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/migration/
    │           ├── V1__create_users.sql
    │           ├── V2__create_friendships.sql
    │           ├── V3__create_lists.sql
    │           ├── V4__create_items.sql
    │           ├── V5__create_comments.sql
    │           └── V6__create_activity_events.sql
    └── test/
        └── java/com/together/
            ├── auth/AuthControllerTest.java
            ├── list/TodoListControllerTest.java
            ├── item/TodoItemControllerTest.java
            └── AbstractIntegrationTest.java   # Testcontainers PostgreSQL
```

---

## Entités JPA

### User
```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Authentification : email + password uniquement. Pas de username.
    @Column(unique = true, nullable = false)
    private String email;             // identifiant de connexion unique

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;              // nom affiché

    @Column(nullable = false, length = 2)
    private String initials;          // généré automatiquement depuis name, ex: "L"

    @Column(nullable = false)
    private String avatarColor;       // hex "#CECBF6"

    @Column(nullable = false)
    private String avatarTextColor;   // hex "#3C3489"

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;            // OWNER, PARTNER, FRIEND (rôle global par défaut)

    private Instant lastSeenAt;

    @Column(nullable = false)
    private Instant createdAt;
}

public enum UserRole { OWNER, PARTNER, FRIEND }
```

### Friendship
```java
@Entity
@Table(name = "friendships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "addressee_id"}))
public class Friendship {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private User requester;

    @ManyToOne(optional = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;   // PENDING, ACCEPTED, DECLINED, BLOCKED

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;
}

public enum FriendshipStatus { PENDING, ACCEPTED, DECLINED, BLOCKED }
```

### TodoList
```java
@Entity
@Table(name = "todo_lists")
public class TodoList {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 4)
    private String emoji;             // "🛒"

    @Column(nullable = false)
    private String accentColor;       // "#EEF2FF"

    @Column(nullable = false)
    private String progressColor;     // "#4A3ABA"

    @ManyToOne(optional = false)
    private User createdBy;

    @ManyToMany
    @JoinTable(name = "list_members")
    private Set<User> members = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;
}
```

### TodoItem
```java
@Entity
@Table(name = "todo_items")
public class TodoItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private TodoList list;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private boolean done = false;

    @ManyToOne
    private User checkedBy;           // null si pas encore coché

    private Instant checkedAt;

    @Column(nullable = false)
    private int position;             // pour l'ordre d'affichage

    @ManyToOne(optional = false)
    private User createdBy;

    @Column(nullable = false)
    private Instant createdAt;
}
```

### Comment
```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private TodoItem item;

    @ManyToOne(optional = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(nullable = false)
    private Instant createdAt;
}
```

### ActivityEvent
```java
@Entity
@Table(name = "activity_events")
public class ActivityEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;        // ITEM_CHECKED, COMMENT_ADDED, ITEM_ADDED, FRIEND_JOINED, LIST_CREATED

    @ManyToOne(optional = false)
    private User actor;               // qui a fait l'action

    @ManyToOne(optional = false)
    private TodoList list;

    @ManyToOne
    private TodoItem item;            // null pour LIST_CREATED et FRIEND_JOINED

    private String extraText;         // texte du commentaire ou nom de l'item ajouté

    @Column(nullable = false)
    private Instant createdAt;
}
```

---

## DTOs (Java Records)

Utiliser exclusivement des **records Java** pour tous les DTOs — immuables, pas de boilerplate.

```java
// Auth
// Inscription : email + password + nom affiché + couleur d'avatar
public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String name,
    @NotBlank String avatarColor    // hex choisi par l'utilisateur dans l'UI, ex: "#CECBF6"
) {}

// Connexion : email + password uniquement
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}

public record AuthResponse(
    String accessToken,
    String refreshToken,
    UserDto user
) {}

// User
public record UserDto(
    UUID id,
    String email,
    String name,
    String initials,
    String avatarColor,
    String avatarTextColor,
    UserRole role,
    String lastSeenAt   // formaté "2h ago", "yesterday", etc.
) {}

// List
public record TodoListDto(
    UUID id,
    String name,
    String emoji,
    String accentColor,
    String progressColor,
    List<UserDto> members,
    int totalItems,
    int doneItems,
    double progress,       // 0.0 - 1.0
    Instant createdAt
) {}

public record CreateListRequest(
    @NotBlank String name,
    @NotBlank String emoji,
    @NotBlank String accentColor,
    @NotBlank String progressColor,
    @NotEmpty List<UUID> memberIds
) {}

// Item
public record TodoItemDto(
    UUID id,
    String text,
    boolean done,
    UserDto checkedBy,
    Instant checkedAt,
    int position,
    List<CommentDto> comments,
    Instant createdAt
) {}

public record CreateItemRequest(
    @NotBlank String text
) {}

public record ToggleItemRequest(
    boolean done
) {}

// Comment
public record CommentDto(
    UUID id,
    UserDto author,
    String text,
    Instant createdAt
) {}

public record CreateCommentRequest(
    @NotBlank @Size(max = 1000) String text
) {}

// Activity
public record ActivityEventDto(
    UUID id,
    ActivityType type,
    UserDto actor,
    UUID listId,
    String listName,
    UUID itemId,
    String itemText,
    String extraText,
    Instant createdAt
) {}

// Pagination générique
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext
) {}
```

---

## Endpoints REST

### Auth — `/api/auth`
```
POST   /api/auth/register        → AuthResponse        (public)
POST   /api/auth/login           → AuthResponse        (public)
POST   /api/auth/refresh         → AuthResponse        (public, body: { refreshToken })
POST   /api/auth/logout          → 204                 (authentifié)
```

### Users — `/api/users`
```
GET    /api/users/me             → UserDto             (authentifié)
PUT    /api/users/me             → UserDto             (authentifié)
GET    /api/users/search?q=      → List<UserDto>       (authentifié, recherche par nom ou email)
```

### Friendships — `/api/friendships`
```
GET    /api/friendships          → List<FriendshipDto> (mes amis acceptés)
POST   /api/friendships          → FriendshipDto       (body: { addresseeId })
PUT    /api/friendships/{id}     → FriendshipDto       (accepter/refuser, body: { status })
DELETE /api/friendships/{id}     → 204                 (supprimer un ami)
GET    /api/friendships/requests → List<FriendshipDto> (demandes reçues en attente)
```

### Lists — `/api/lists`
```
GET    /api/lists                → List<TodoListDto>   (mes listes)
POST   /api/lists                → TodoListDto
GET    /api/lists/{id}           → TodoListDto
PUT    /api/lists/{id}           → TodoListDto         (membres de la liste seulement)
DELETE /api/lists/{id}           → 204                 (créateur seulement)
POST   /api/lists/{id}/members   → TodoListDto         (body: { userId })
DELETE /api/lists/{id}/members/{userId} → 204
```

### Items — `/api/lists/{listId}/items`
```
GET    /api/lists/{listId}/items           → List<TodoItemDto>
POST   /api/lists/{listId}/items           → TodoItemDto
PUT    /api/lists/{listId}/items/{id}      → TodoItemDto   (modifier le texte)
PATCH  /api/lists/{listId}/items/{id}/toggle → TodoItemDto (cocher/décocher)
DELETE /api/lists/{listId}/items/{id}      → 204
PATCH  /api/lists/{listId}/items/reorder   → 204           (body: [{ id, position }])
```

### Comments — `/api/lists/{listId}/items/{itemId}/comments`
```
GET    /api/lists/{listId}/items/{itemId}/comments      → List<CommentDto>
POST   /api/lists/{listId}/items/{itemId}/comments      → CommentDto
DELETE /api/lists/{listId}/items/{itemId}/comments/{id} → 204 (auteur seulement)
```

### Activity — `/api/activity`
```
GET    /api/activity             → PageResponse<ActivityEventDto>  (?page=0&size=20)
GET    /api/activity/lists/{listId} → PageResponse<ActivityEventDto>
```

---

## Sécurité JWT

```java
// JwtTokenProvider — méthodes principales
String generateAccessToken(User user);   // expiration : 15 minutes
String generateRefreshToken(User user);  // expiration : 30 jours
UUID extractUserId(String token);
boolean isTokenValid(String token);

// JwtAuthFilter
// - Lit le header "Authorization: Bearer <token>"
// - Valide le token et injecte SecurityContext
// - Ignore les routes publiques : /api/auth/**, /swagger-ui/**, /v3/api-docs/**

// SecurityConfig
// - STATELESS session management
// - CORS : autoriser http://localhost:8081 (Expo dev) + domaine de prod
// - CSRF : désactivé (API REST stateless)
// - Toutes les routes /api/** nécessitent une authentification sauf /api/auth/**
```

---

## Configuration `application.yml`

```yaml
spring:
  application:
    name: together-backend

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/together}
    username: ${DB_USERNAME:together}
    password: ${DB_PASSWORD:together}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

  jpa:
    hibernate:
      ddl-auto: validate          # Flyway gère le schéma, Hibernate valide seulement
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        default_schema: public

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false

  threads:
    virtual:
      enabled: true               # Virtual Threads Java 25 / Project Loom via Spring Boot 4

server:
  port: ${PORT:8080}

jwt:
  secret: ${JWT_SECRET:change-me-in-production-min-256-bits}
  access-token-expiration: 900      # 15 minutes en secondes
  refresh-token-expiration: 2592000 # 30 jours en secondes

springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs

logging:
  level:
    com.together: INFO
    org.springframework.security: WARN
```

```yaml
# application-dev.yml
spring:
  jpa:
    show-sql: true
  flyway:
    clean-on-validation-error: true  # UNIQUEMENT en dev — recrée le schéma si migration échoue

logging:
  level:
    com.together: DEBUG
```

---

## Docker

### `Dockerfile` (multi-stage)
```dockerfile
# Stage 1 — build
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw -q package -DskipTests

# Stage 2 — runtime
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S together && adduser -S together -G together
USER together
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### `docker-compose.yml` (production-like)
```yaml
version: '3.9'

services:
  db:
    image: postgres:16-alpine
    container_name: together-db
    environment:
      POSTGRES_DB: together
      POSTGRES_USER: ${DB_USERNAME:-together}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-together}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U together"]
      interval: 5s
      timeout: 5s
      retries: 5

  app:
    build: .
    container_name: together-app
    depends_on:
      db:
        condition: service_healthy
    environment:
      DB_URL: jdbc:postgresql://db:5432/together
      DB_USERNAME: ${DB_USERNAME:-together}
      DB_PASSWORD: ${DB_PASSWORD:-together}
      JWT_SECRET: ${JWT_SECRET}
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8080:8080"
    restart: unless-stopped

  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: together-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@together.dev
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"
    depends_on:
      - db

volumes:
  postgres_data:
```

### `docker-compose.dev.yml` (override développement)
```yaml
# Usage : docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
version: '3.9'

services:
  app:
    build:
      context: .
      target: builder          # utilise le stage builder pour hot-reload
    volumes:
      - ./src:/app/src          # hot-reload avec Spring DevTools
      - ./pom.xml:/app/pom.xml
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DEVTOOLS_RESTART_ENABLED: "true"
    ports:
      - "8080:8080"
      - "5005:5005"             # port debug JVM
    command: ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]
```

### `.env.example`
```env
DB_USERNAME=together
DB_PASSWORD=together_secret
DB_URL=jdbc:postgresql://localhost:5432/together
JWT_SECRET=change-me-to-a-random-256-bit-secret-string-for-production
```

---

## Migrations Flyway

```sql
-- V1__create_users.sql
-- Authentification : email + password uniquement, pas de username
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    initials CHAR(2) NOT NULL,           -- généré auto depuis name (première lettre)
    avatar_color CHAR(7) NOT NULL,       -- choisi à l'inscription
    avatar_text_color CHAR(7) NOT NULL,  -- calculé pour contraste
    role VARCHAR(20) NOT NULL DEFAULT 'FRIEND',
    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email ON users(email);

-- V2__create_friendships.sql
CREATE TABLE friendships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    addressee_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    UNIQUE(requester_id, addressee_id)
);
CREATE INDEX idx_friendships_requester ON friendships(requester_id);
CREATE INDEX idx_friendships_addressee ON friendships(addressee_id);

-- V3__create_lists.sql
CREATE TABLE todo_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    emoji VARCHAR(8) NOT NULL,
    accent_color CHAR(7) NOT NULL,
    progress_color CHAR(7) NOT NULL,
    created_by_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE list_members (
    list_id UUID NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY(list_id, user_id)
);
CREATE INDEX idx_list_members_user ON list_members(user_id);

-- V4__create_items.sql
CREATE TABLE todo_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id UUID NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
    text VARCHAR(500) NOT NULL,
    done BOOLEAN NOT NULL DEFAULT false,
    checked_by_id UUID REFERENCES users(id),
    checked_at TIMESTAMPTZ,
    position INT NOT NULL DEFAULT 0,
    created_by_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_items_list ON todo_items(list_id);

-- V5__create_comments.sql
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id UUID NOT NULL REFERENCES todo_items(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    text VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_comments_item ON comments(item_id);

-- V6__create_activity_events.sql
CREATE TABLE activity_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(30) NOT NULL,
    actor_id UUID NOT NULL REFERENCES users(id),
    list_id UUID NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
    item_id UUID REFERENCES todo_items(id) ON DELETE SET NULL,
    extra_text VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_activity_list ON activity_events(list_id);
CREATE INDEX idx_activity_actor ON activity_events(actor_id);
CREATE INDEX idx_activity_created ON activity_events(created_at DESC);
```

---

## Gestion des erreurs

```java
// ApiError record — réponse d'erreur uniforme
public record ApiError(
    int status,
    String error,
    String message,
    Instant timestamp,
    String path
) {}

// GlobalExceptionHandler — @RestControllerAdvice
// Gérer :
// - ResourceNotFoundException      → 404
// - UnauthorizedException          → 403
// - MethodArgumentNotValidException → 400 (validation Bean)
// - UsernameNotFoundException       → 401
// - Exception générique             → 500 (masquer le détail en prod)

// Exemple de réponse 404 :
// {
//   "status": 404,
//   "error": "Not Found",
//   "message": "List not found: 123e4567-e89b-12d3-a456-426614174000",
//   "timestamp": "2025-09-16T10:30:00Z",
//   "path": "/api/lists/123e4567-e89b-12d3-a456-426614174000"
// }
```

---

## Logique d'authentification (email + password)

### Inscription — `POST /api/auth/register`
```
Body : { email, password, name, avatarColor }

1. Vérifier que email n'existe pas déjà → 409 Conflict si doublon
2. Générer initials automatiquement :
   - Prendre la première lettre de chaque mot du name, majuscule, max 2 lettres
   - Ex: "Léa" → "L", "Marc Dupont" → "MD"
3. Calculer avatarTextColor pour contraste :
   - Si avatarColor est clair (luminance > 0.5) → textColor = "#1A1A1A"
   - Si avatarColor est sombre → textColor = "#FFFFFF"
4. Hasher le password avec BCrypt (strength 12)
5. Créer l'utilisateur avec role = FRIEND par défaut
6. Générer accessToken + refreshToken
7. Retourner AuthResponse { accessToken, refreshToken, UserDto }
```

### Connexion — `POST /api/auth/login`
```
Body : { email, password }

1. Chercher l'utilisateur par email → 401 si non trouvé
   (NE PAS dire "email inconnu" — toujours "email ou mot de passe incorrect")
2. Vérifier le mot de passe avec BCrypt.matches() → 401 si incorrect
3. Mettre à jour lastSeenAt = now()
4. Générer accessToken + refreshToken
5. Retourner AuthResponse { accessToken, refreshToken, UserDto }
```

### Refresh token — `POST /api/auth/refresh`
```
Body : { refreshToken }

1. Valider le refreshToken (signature + expiration)
2. Extraire userId, charger l'utilisateur
3. Générer un nouvel accessToken (le refreshToken reste valide)
4. Retourner AuthResponse { accessToken, refreshToken (inchangé), UserDto }
```

### Logout — `POST /api/auth/logout`
```
Header : Authorization: Bearer <accessToken>

- Stateless JWT → pas de blacklist pour l'instant
- Simplement retourner 204 No Content
- Le client supprime ses tokens côté mobile
```

### Règles de sécurité générales
- **BCrypt strength 12** pour tous les hashages de mot de passe
- **Messages d'erreur génériques** : toujours "Email ou mot de passe incorrect" (jamais distinguer email inconnu vs mauvais mot de passe)
- **Rate limiting** : pas pour ce socle initial, à ajouter plus tard
- **Validation** : email format RFC5322, password min 8 caractères (Bean Validation)

---

## Logique métier importante

### Autorisation sur les listes
- Seuls les **membres** d'une liste peuvent voir/modifier ses items et commentaires
- Seul le **créateur** peut supprimer une liste ou en modifier les métadonnées
- Vérification systématique dans les Services (pas dans les Controllers)

### Toggle item
- Quand `done` passe à `true` : enregistrer `checkedBy = currentUser`, `checkedAt = now()`
- Quand `done` passe à `false` : remettre `checkedBy = null`, `checkedAt = null`
- Créer un `ActivityEvent` de type `ITEM_CHECKED` dans tous les cas

### Activity auto-générée
Créer automatiquement un `ActivityEvent` dans le service concerné pour :
- `ITEM_CHECKED` → dans `TodoItemService.toggle()`
- `COMMENT_ADDED` → dans `CommentService.create()`
- `ITEM_ADDED` → dans `TodoItemService.create()`
- `LIST_CREATED` → dans `TodoListService.create()`
- `FRIEND_JOINED` → dans `FriendshipService.accept()`

### Virtual Threads (Java 25 / Loom)
- Activés via `spring.threads.virtual.enabled=true` dans application.yml
- Spring Boot 4 les utilise automatiquement pour Tomcat — pas de code spécifique requis
- Bénéfice : meilleure scalabilité pour les opérations I/O (requêtes DB, etc.)

---

## Tests

### AbstractIntegrationTest (Testcontainers)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("together_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### Tests à écrire
1. `AuthControllerTest` — register, login, token invalide → 401
2. `TodoListControllerTest` — CRUD liste, accès refusé si non-membre → 403
3. `TodoItemControllerTest` — toggle item, vérifier ActivityEvent créé
4. `CommentControllerTest` — ajouter/supprimer commentaire
5. `FriendshipControllerTest` — envoyer/accepter/refuser une demande

---

## Points d'attention

1. **UUIDs partout** — jamais de Long auto-increment comme ID exposé en API
2. **Jamais de mot de passe** dans les réponses — `passwordHash` exclue de tous les DTOs
3. **Pagination** obligatoire sur `/api/activity` et `/api/users/search`
4. **CORS** configuré pour accepter les requêtes depuis l'app Expo en dev (`localhost:8081`, `exp://…`)
5. **Flyway** gère le schéma — `ddl-auto: validate` en prod, jamais `create` ou `update`
6. **Records Java 25** pour tous les DTOs — immuables, pas de Lombok nécessaire pour les DTOs
7. **JSpecify** `@Nullable` / `@NonNull` sur les méthodes de service (Spring Framework 7 les supporte nativement)
8. **Logs** : ne jamais logger de données sensibles (mots de passe, tokens JWT complets)
9. **Health check** : exposer `/actuator/health` (Spring Boot Actuator) pour Docker healthcheck
10. **`.env`** : ne jamais committer `.env` — seulement `.env.example`

---

## Commandes de démarrage

```bash
# Cloner et configurer
git clone ...
cd together-backend
cp .env.example .env
# Editer .env avec des vraies valeurs

# Lancer uniquement la DB en dev (l'app tourne en local)
docker-compose up db pgadmin -d

# Lancer l'app localement (hot-reload)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Lancer tout en Docker (prod-like)
docker-compose up --build

# Lancer tout avec hot-reload Docker
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build

# Tests
./mvnw test

# Accès
# API :        http://localhost:8080
# Swagger UI : http://localhost:8080/swagger-ui.html
# pgAdmin :    http://localhost:5050  (admin@together.dev / admin)
```

---

## Ordre de développement recommandé

1. `pom.xml` — dépendances Java 25 + Spring Boot 4 + toutes les libs
2. `docker-compose.yml` + `.env.example` → `docker-compose up db -d`
3. `application.yml` + profils dev/prod
4. Migrations Flyway V1 à V6 — vérifier que le schéma se crée proprement
5. Entités JPA + Repositories (sans logique)
6. Auth : `User`, `AuthService`, `JwtTokenProvider`, `JwtAuthFilter`, `SecurityConfig`, `AuthController`
7. Tester register + login via Swagger UI
8. DTOs (records) pour toutes les entités
9. `GlobalExceptionHandler` + classes d'exception
10. `UserService` + `UserController`
11. `FriendshipService` + `FriendshipController`
12. `TodoListService` + `TodoListController`
13. `TodoItemService` + `TodoItemController` (avec toggle + ActivityEvent)
14. `CommentService` + `CommentController`
15. `ActivityEventService` + `ActivityEventController`
16. Tests d'intégration (Testcontainers)
17. `Dockerfile` multi-stage + test `docker-compose up --build`
