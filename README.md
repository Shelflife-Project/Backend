# ShelfLife

**Team**: Sohonyai Tibor, Remete Bence Dávid

## Project Overview

ShelfLife is a modern web application designed to help people efficiently manage their household inventory. It tracks stored items, monitors expiration dates, generates shopping lists, and sends notifications to reduce food waste and improve household organization.

**Key Features**:
- Create and manage multiple storage locations
- Invite family members or roommates to collaborate on storages
- Add products by barcode or manual entry
- Get alerts for items about to expire or running low
- Automatically generated shopping lists
- Secure authentication with JWT tokens
- Product images and icons support
- User authentication and role-based access

## Tech Stack

- **Backend Framework**: Spring Boot 4.0.3
- **Java Version**: Java 21
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Database Migrations**: Flyway
- **Security**: Spring Security + JWT Authentication
- **Containerization**: Docker & Docker Compose
- **ORM/JDBC**: Spring JDBC
- **Additional Libraries**: Lombok, OpenAPI/Swagger

### Tables

#### `users`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| email | VARCHAR(255) | Unique email address |
| username | VARCHAR(255) | User's display name |
| password | VARCHAR(255) | Bcrypt hashed password |
| is_admin | BIT(1) | Admin role flag |

#### `storages`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| name | VARCHAR(255) | Storage location name |
| owner_id | BIGINT | FK to users (creator) |

#### `products`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| name | VARCHAR(255) | Product name |
| barcode | VARCHAR(255) | Unique product barcode |
| category | VARCHAR(255) | Product category |
| expiration_days_delta | INT | Default days until expiration |
| owner_id | BIGINT | FK to users (creator) |

#### `storage_items`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| storage_id | BIGINT | FK to storages |
| product_id | BIGINT | FK to products |
| expires_at | DATE | Item's expiration date |
| created_at | DATETIME(6) | Creation timestamp |

#### `storage_members`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| storage_id | BIGINT | FK to storages |
| user_id | BIGINT | FK to users |
| is_accepted | BIT(1) | Invitation acceptance status |

#### `running_low_settings`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| storage_id | BIGINT | FK to storages |
| product_id | BIGINT | FK to products |
| running_low | INT | Threshold quantity for alerts |

#### `invalidjwts`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| token | VARCHAR(255) | Blacklisted JWT token |
| created_at | DATETIME(6) | Blacklist timestamp |

#### `images`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| filename | VARCHAR(255) | Image filename |
| mimetype | VARCHAR(255) | MIME type |

If you want to seed the database run this command:
```bash
mvn clean spring-boot:run -D spring-boot.run.arguments=--seed
```

---

## Getting Started

### Prerequisites

- Docker & Docker Compose installed
- Or: Java 21 + Maven + MySQL 8.0 (for local development)

### Installation & Running

#### Option 1: Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd Backend
   ```

2. **Create environment file**
   ```bash
   cp template.env .env
   ```
   
   Edit `.env` with your configuration:
   ```properties
   spring.datasource.url=jdbc:mysql://db:3306/shelflife
   spring.datasource.username=root
   spring.datasource.password=root
   jwt.secret=your_secret_key_here
   images.path=./images
   ```

3. **Start the application**
   ```bash
   docker-compose up --build
   ```

   The application will:
   - Start MySQL database on port `3306`
   - Start Spring Boot application on port `8080`
   - Automatically run Flyway migrations
   - Create database tables

4. **Verify the application**
   ```bash
   curl http://localhost:8080/api/auth/me
   ```

#### Option 2: Local Development

1. **Prerequisites**
   - Java 21 installed
   - Maven installed
   - MySQL 8.0 running locally

2. **Create `.env` file**
   ```bash
   cp template.env .env
   ```

3. **Build the project**
   ```bash
   ./mvnw clean install
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access the application**
   ```
   http://localhost:8080
   ```

## API Endpoints

For interactive API documentation, visit: [Swagger UI](http://localhost:8080/swagger-ui/index.html#/)

## Testing

Run the test suite:

```bash
./mvnw test
```