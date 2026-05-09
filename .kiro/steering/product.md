# CarteiraClientes — Product Overview

A REST API backend for real estate brokers to manage their client portfolio. The system tracks:

- **Clientes** (clients) — personal info plus property interest profile (rooms, bathrooms, parking spots, area, max budget)
- **Visitas** (property visits) — records of visits made for a client, including date, notes, and satisfaction flag
- **Users** — brokers/agents responsible for visits, with role-based access
- **Roles** — access control roles assigned to users

The primary use case is matching clients to available properties based on their interest profile and tracking visit history. The project was built as a TCC (undergraduate thesis) study project.

API documentation is available via Swagger UI at `http://localhost:8080/swagger-ui.html` when running locally.
