# AutoLoc API

AutoLoc is a Spring Boot REST API for managing a vehicle rental agency. It provides CRUD operations for agencies, employees, vehicles, equipment, customers, reservations, contracts, payments, and maintenance records. The project also demonstrates JPA entity relationships and cascade behavior.

## 1. Technology stack

- Java 17
- Spring Boot 3.5.6
- Spring Web for REST endpoints
- Spring Data JPA and Hibernate for persistence
- MySQL for the application database
- Springdoc OpenAPI and Swagger UI for API documentation
- Maven for dependency management and builds
- JUnit 5, Spring Boot Test, AssertJ, and H2 for persistence tests
- Lombok for entity accessors and constructors

## 2. Project layout

The Maven project is in `autoloc-api/`.

```text
autoloc-api/
├── pom.xml
└── src/
    ├── main/java/tn/esprit/autoloc/
    │   ├── config/       OpenAPI configuration
    │   ├── controller/   HTTP routes
    │   ├── entity/       JPA database entities
    │   ├── enums/        Allowed enum values
    │   ├── repository/   Spring Data repositories
    │   ├── service/      Service interfaces and implementations
    │   └── AutolocApiApplication.java
    ├── main/resources/application.properties
    └── test/java/        JPA association and cascade tests
```

### What the Java folders do (and the parts that can be confusing)

These notes explain the responsibilities of the folders under `src/main/java/tn/esprit/autoloc`. They are documentation only; the Java source files are unchanged.

| Folder or file | Responsibility and concept to understand |
| --- | --- |
| `config/` | Defines application-wide configuration. `OpenApiConfig` adds the API title, version, description, and contact shown in the generated OpenAPI documentation. |
| `controller/` | Defines the HTTP contract: URL, method, path variables, and JSON input/output. Controllers delegate business and persistence work to services; `@RequestBody` converts incoming JSON into an entity, and `@PathVariable` reads values such as an ID from the URL. |
| `entity/` | Contains JPA entities, which Hibernate maps to database tables. Relationship annotations have an owning side (the side that writes the foreign key or join table) and sometimes an inverse side marked with `mappedBy`. Updating only the inverse side may not persist a relationship. |
| `enums/` | Restricts fields such as vehicle status and payment method to named values. Because the entities use `EnumType.STRING`, JSON must use the exact enum spelling, for example `"DISPONIBLE"`. |
| `repository/` | Provides database operations through Spring Data JPA. Extending `JpaRepository<Entity, IdType>` supplies common operations such as `save`, `findById`, and `deleteById` without handwritten SQL. A method such as `findByAgence_IdAgence` is a query derived from the property path. |
| `service/` | Holds service interfaces and their implementations. Controllers depend on the interfaces, while Spring injects the `@Service` implementation. This keeps HTTP handling separate from application and database operations. |
| `service/impl/` | Implements the service methods, usually by calling repositories. Relationship operations first load existing rows by ID, then update the owning entity and save it. In this project, CRUD update methods call `save`; if the supplied ID does not exist, JPA may insert a new row. |
| `AutolocApiApplication.java` | Main Spring Boot entry point. Its package location matters: component scanning starts here and discovers the child packages such as controllers, services, and repositories. |

The most subtle entity settings are `FetchType.LAZY` and cascade options. `LAZY` means related rows are loaded only when accessed; returning entities directly from a REST controller can therefore expose serialization or database-session issues for relationships. Cascade controls whether an operation on one entity propagates to related entities: it is not the same as a database foreign-key rule. The current tests in `src/test/java` cover several cascade and association cases.

## 3. Requirements

Install a Java 17 JDK, Maven 3.6.3 or later, and MySQL 8 (or a compatible MySQL server). Ensure `java`, `mvn`, and the MySQL client are available in your terminal. The database must be reachable at the host and port configured in `application.properties`.

## 4. Configure MySQL

The default configuration expects a database named `autoloc`, a MySQL server on `localhost:3306`, and the `root` user with an empty password. Create the database before starting the application:

```sql
CREATE DATABASE autoloc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

If your MySQL username, password, host, or port differs, edit `src/main/resources/application.properties` or provide Spring environment variables. For example, in PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/autoloc"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "your-password"
```

Do not commit real database credentials to source control.

## 5. Understand `application.properties`

The application configuration is at `autoloc-api/src/main/resources/application.properties`:

| Property | Purpose |
| --- | --- |
| `spring.application.name` | Spring application name (`autoloc-api`). |
| `server.port` | HTTP port; the API listens on `8081`. |
| `spring.datasource.url` | MySQL JDBC URL (`localhost:3306/autoloc`). |
| `spring.datasource.username` / `password` | MySQL credentials. |
| `spring.jpa.hibernate.ddl-auto=update` | Hibernate updates the schema to match entities at startup. |
| `spring.jpa.show-sql=true` | Prints generated SQL in the application log. |
| `spring.jpa.properties.hibernate.format_sql=true` | Formats SQL output for readability. |
| `springdoc.swagger-ui.path` | Swagger UI path (`/swagger-ui.html`). |
| `springdoc.api-docs.path` | OpenAPI JSON path (`/v3/api-docs`). |
| `springdoc.swagger-ui.operations-sorter` | Sorts operations by HTTP method in Swagger UI. |

`ddl-auto=update` is convenient for local development. Use a migration tool and an explicitly managed schema for production deployments.

## 6. Build and start the application

Open a terminal in the Maven project directory:

```powershell
cd autoloc-api
```

Compile and package the project:

```powershell
mvn clean package
```

Start the API in development mode:

```powershell
mvn spring-boot:run
```

Alternatively, run the packaged JAR after building:

```powershell
java -jar target/autoloc-api-0.0.1-SNAPSHOT.jar
```

The application is available at `http://localhost:8081`. Keep this terminal open while sending requests. Stop the application with `Ctrl+C`.

## 7. Explore and call the HTTP API

Open Swagger UI at [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html). Expand a controller, select an operation, choose **Try it out**, provide any required JSON, then select **Execute**. The OpenAPI specification is available at [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs).

The controllers use JSON request and response bodies. For example, list customers with PowerShell:

```powershell
curl.exe http://localhost:8081/clients
```

Create a customer by sending JSON. The fields below match the `Client` entity; generated IDs are assigned by the database:

```powershell
curl.exe -X POST http://localhost:8081/clients `
  -H "Content-Type: application/json" `
  -d '{"nom":"Ben Ali","prenom":"Sami","email":"sami@example.com","telephone":"12345678","numPermis":"TN12345","dateInscription":"2026-11-25"}'
```

Typical CRUD routes use `GET /resource` to list, `GET /resource/{id}` to retrieve one, `POST /resource` to create, `PUT /resource` to update (include the entity ID in the JSON), and `DELETE /resource/{id}` to delete. Use IDs returned by create requests when testing later operations. Some resources depend on existing related records, so create referenced agencies, customers, vehicles, reservations, or contracts first.

### REST methods to test

Use these URLs in Swagger UI, Postman, or another HTTP client. Replace `{id}` and other path variables with IDs returned by the `POST` requests. For `POST` and `PUT`, send a JSON body with the entity fields and set `Content-Type: application/json`.

| Resource | List | Get one | Create | Update | Delete |
| --- | --- | --- | --- | --- | --- |
| Agency | `GET /agences` | `GET /agences/{id}` | `POST /agences` | `PUT /agences` | `DELETE /agences/{id}` |
| Employee | `GET /employes` | `GET /employes/{id}` | `POST /employes` | `PUT /employes` | `DELETE /employes/{id}` |
| Vehicle | `GET /vehicules` | `GET /vehicules/{id}` | `POST /vehicules` | `PUT /vehicules` | `DELETE /vehicules/{id}` |
| Equipment | `GET /equipements` | `GET /equipements/{id}` | `POST /equipements` | `PUT /equipements` | `DELETE /equipements/{id}` |
| Customer | `GET /clients` | `GET /clients/{id}` | `POST /clients` | `PUT /clients` | `DELETE /clients/{id}` |
| Reservation | `GET /reservations` | `GET /reservations/{id}` | `POST /reservations` | `PUT /reservations` | `DELETE /reservations/{id}` |
| Contract | `GET /contrats` | `GET /contrats/{id}` | `POST /contrats` | `PUT /contrats` | `DELETE /contrats/{id}` |
| Payment | `GET /paiements` | `GET /paiements/{id}` | `POST /paiements` | `PUT /paiements` | `DELETE /paiements/{id}` |
| Maintenance | `GET /maintenances` | `GET /maintenances/{id}` | `POST /maintenances` | `PUT /maintenances` | `DELETE /maintenances/{id}` |

Relationship methods:

| Method | Purpose |
| --- | --- |
| `PUT /employes/{idEmploye}/affecter-agence/{idAgence}` | Assign an employee to an agency. |
| `PUT /employes/{idEmploye}/desaffecter-agence` | Remove an employee's agency assignment. |
| `PUT /vehicules/{idVehicule}/affecter-agence/{idAgence}` | Assign a vehicle to an agency. |
| `PUT /vehicules/{idVehicule}/affecter-equipement/{idEquipement}` | Assign equipment to a vehicle. |
| `PUT /vehicules/{idVehicule}/desaffecter-equipement/{idEquipement}` | Remove equipment from a vehicle. |

For an end-to-end manual check, create any related records first (for example, create an agency before assigning an employee to it). Then create a record, retrieve it by its returned ID, update it, and delete it. A delete request usually returns an empty response when successful.

### Postman CRUD examples for all resources

In Postman, select **Body → raw → JSON** for `POST` and `PUT` requests and set the `Content-Type` header to `application/json`. IDs are generated by the database: after each `POST`, copy the ID from its response and replace `{id}` in the corresponding `GET`, `PUT`, and `DELETE` URLs. The `PUT` example uses that same ID in its JSON body. The endpoint implementations use `save`, so a `PUT` with an unknown ID may create a record instead of returning an error; use an ID returned by `POST` to update the intended record.

1. **Agency** — `POST http://localhost:8081/agences`

```json
{
  "nom": "AutoLoc Tunis Centre",
  "ville": "Tunis",
  "adresse": "10 Avenue Habib Bourguiba",
  "telephone": "71123456"
}
```

2. **Employee** — `POST http://localhost:8081/employes`

```json
{
  "nom": "Ben Ali",
  "prenom": "Sami",
  "role": "AGENT"
}
```

After creating the employee and agency, assign the employee using `PUT /employes/{idEmploye}/affecter-agence/{idAgence}`.

3. **Vehicle** — `POST http://localhost:8081/vehicules`

```json
{
  "immatriculation": "123-TUN-456",
  "marque": "Toyota",
  "modele": "Yaris",
  "categorie": "CITADINE",
  "tarifJournalier": 120.00,
  "statut": "DISPONIBLE"
}
```

Assign an agency after creation with `PUT /vehicules/{idVehicule}/affecter-agence/{idAgence}`.

4. **Equipment** — `POST http://localhost:8081/equipements`

```json
{
  "libelle": "GPS"
}
```

Assign it to a vehicle after creation with `PUT /vehicules/{idVehicule}/affecter-equipement/{idEquipement}`.

5. **Customer** — `POST http://localhost:8081/clients`

```json
{
  "nom": "Trabelsi",
  "prenom": "Amira",
  "email": "amira.trabelsi@example.com",
  "telephone": "22123456",
  "numPermis": "TN-2026-12345",
  "dateInscription": "2026-11-26"
}
```

6. **Reservation** — `POST http://localhost:8081/reservations`

```json
{
  "dateDebut": "2026-12-01",
  "dateFin": "2026-12-05",
  "statut": "CONFIRMEE",
  "client": {
    "idClient": 1
  },
  "vehicule": {
    "idVehicule": 1
  }
}
```

Replace `idClient` and `idVehicule` with IDs of records already created. A reservation references an existing customer and vehicle; it does not create them.

7. **Contract** — `POST http://localhost:8081/contrats`

```json
{
  "dateSignature": "2026-12-01",
  "montantTotal": 600.00,
  "valide": true,
  "reservation": {
    "idReservation": 1
  }
}
```

Replace `idReservation` with an existing reservation ID.

8. **Payment** — `POST http://localhost:8081/paiements`

```json
{
  "montant": 600.00,
  "datePaiement": "2026-12-01",
  "modePaiement": "CARTE",
  "contrat": {
    "idContrat": 1
  }
}
```

Replace `idContrat` with an existing contract ID. Payment methods are `CARTE`, `ESPECES`, and `VIREMENT`.

9. **Maintenance** — `POST http://localhost:8081/maintenances`

```json
{
  "dateDebut": "2026-11-20",
  "dateFin": "2026-11-21",
  "description": "Vidange et contrôle général",
  "vehicule": {
    "idVehicule": 1
  }
}
```

Replace `idVehicule` with an existing vehicle ID. Dates must use the `YYYY-MM-DD` format. For all relationship examples, first create the referenced records and use their actual IDs from Postman's response.

#### Complete request sequence for each resource

For each resource above, perform these requests in order. Use the request body shown in the numbered example, changing a field for the `PUT` request to verify that the update worked.

| Resource | POST (create) | GET (read) | PUT (update) | DELETE (remove) |
| --- | --- | --- | --- | --- |
| Agency | `POST /agences` | `GET /agences/{idAgence}` | `PUT /agences` — body includes `"idAgence": {idAgence}` | `DELETE /agences/{idAgence}` |
| Employee | `POST /employes` | `GET /employes/{idEmploye}` | `PUT /employes` — body includes `"idEmploye": {idEmploye}` | `DELETE /employes/{idEmploye}` |
| Vehicle | `POST /vehicules` | `GET /vehicules/{idVehicule}` | `PUT /vehicules` — body includes `"idVehicule": {idVehicule}` | `DELETE /vehicules/{idVehicule}` |
| Equipment | `POST /equipements` | `GET /equipements/{idEquipement}` | `PUT /equipements` — body includes `"idEquipement": {idEquipement}` | `DELETE /equipements/{idEquipement}` |
| Customer | `POST /clients` | `GET /clients/{idClient}` | `PUT /clients` — body includes `"idClient": {idClient}` | `DELETE /clients/{idClient}` |
| Reservation | `POST /reservations` | `GET /reservations/{idReservation}` | `PUT /reservations` — body includes `"idReservation": {idReservation}` | `DELETE /reservations/{idReservation}` |
| Contract | `POST /contrats` | `GET /contrats/{idContrat}` | `PUT /contrats` — body includes `"idContrat": {idContrat}` | `DELETE /contrats/{idContrat}` |
| Payment | `POST /paiements` | `GET /paiements/{idPaiement}` | `PUT /paiements` — body includes `"idPaiement": {idPaiement}` | `DELETE /paiements/{idPaiement}` |
| Maintenance | `POST /maintenances` | `GET /maintenances/{idMaintenance}` | `PUT /maintenances` — body includes `"idMaintenance": {idMaintenance}` | `DELETE /maintenances/{idMaintenance}` |

Example update body for an agency (replace `1` with the ID returned by `POST`):

```json
{
  "idAgence": 1,
  "nom": "AutoLoc Tunis Centre - Updated",
  "ville": "Tunis",
  "adresse": "10 Avenue Habib Bourguiba",
  "telephone": "71123456"
}
```

The `GET` request for one record returns its JSON representation. The `DELETE` request normally succeeds with an empty response. Delete dependent records before their referenced records if MySQL reports a foreign-key constraint error.

## 8. Data model and relationships

The main entities are `Agence`, `Employe`, `Vehicule`, `Equipement`, `Client`, `Reservation`, `Contrat`, `Paiement`, and `Maintenance`. JPA maps them to relational tables. IDs are generated by the database.

- An agency can have employees and vehicles; employees and vehicles hold the agency reference.
- A vehicle can have multiple equipment items, and an equipment item can be shared by multiple vehicles.
- A customer can have reservations. Saving a customer also persists new reservations (`PERSIST` cascade).
- A reservation can reference a customer and a vehicle, and can own a contract (`ALL` cascade from reservation to contract).
- A contract can have payments. Saving or deleting a contract cascades to its payments; removing a payment from the contract collection deletes it (`orphanRemoval`).
- A vehicle can have maintenance records. Saving a vehicle persists new maintenance records (`PERSIST` cascade).

Enum values accepted in JSON are:

- Vehicle category: `CITADINE`, `BERLINE`, `SUV`, `UTILITAIRE`
- Vehicle status: `DISPONIBLE`, `LOUE`, `MAINTENANCE`
- Employee role: `AGENT`, `MANAGER`
- Reservation status: `EN_ATTENTE`, `CONFIRMEE`, `ANNULEE`, `TERMINEE`
- Payment method: `CARTE`, `ESPECES`, `VIREMENT`

## 9. Run tests

Run the automated tests from the Maven project directory:

```powershell
mvn test
```

The current tests use `@DataJpaTest` and the test-scoped H2 database. They check JPA associations, join-table persistence, and cascade/orphan-removal behavior. They do not send HTTP requests to the controllers; use Swagger UI or an HTTP client for manual endpoint checks.

## 10. Common problems

- **Cannot connect to MySQL:** make sure MySQL is running, the `autoloc` database exists, and the URL and credentials are correct.
- **Port 8081 is already in use:** stop the other process or change `server.port` in `application.properties`.
- **A request returns 404:** confirm the application started and the URL uses one of the route paths above.
- **A request returns 400:** check JSON syntax, property names, date format (`YYYY-MM-DD`), and enum spelling.
- **A related record is missing:** create the referenced entity first and use its generated ID.

## License

No license is currently specified for this project.
