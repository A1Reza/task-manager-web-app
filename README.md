# 🚀 Task Manager — Team Productivity Dashboard

A web-based task management application built with **Java Servlets, Hibernate, JPA, and PostgreSQL**, featuring a modern dashboard for organizing tasks, managing team members, and tracking projects.

The application follows a layered architecture and uses REST-style HTTP endpoints with JSON communication between the frontend and backend.

---

## ✨ Features

### 📊 Dashboard & Analytics

- Overview of tasks, users, projects, and estimated work time.
- Session visit counter powered by `HttpSession`.
- Recent task overview.
- Dynamic statistics loaded from backend APIs.

### ✅ Task Management

- Create, view, update, and delete tasks.
- Assign tasks to users.
- Associate tasks with projects.
- Track estimated completion time.
- Filter tasks by assignee and search tasks.

### 👥 User Management

- Register team members with their names and email addresses.
- View registered users.
- Enforce unique email addresses at the database level.

### 📁 Project Management

- Create projects with names and descriptions.
- View available projects.
- Associate tasks with their corresponding projects.

### 🎨 Modern User Interface

- Responsive dashboard design.
- Dark theme with a clean, modern layout.
- Statistics cards, data tables, forms, and navigation.
- Asynchronous API requests using JavaScript Fetch API.
- User-friendly feedback for operations and errors.

### 🛠️ Backend & Persistence

- Java Servlet-based HTTP endpoints.
- JSON request and response handling.
- Hibernate and JPA for object-relational mapping.
- PostgreSQL database integration.
- Session management with `HttpSession`.
- Application initialization through a Servlet context listener.
- Encoding and request logging filters.

---

## 🖥️ User Interface

The dashboard provides a centralized workspace for monitoring tasks, team members, projects, and session activity.

| Dashboard                      | Task Management                            |
| ------------------------------ | ------------------------------------------ |
| 📊 Task and project statistics | ✅ Create, update, search, and delete tasks |
| 👥 Team overview               | 🔎 Filter tasks by assignee                |
| ⏱️ Estimated workload          | 📁 Assign tasks to projects                |

> 💡 To showcase the actual interface on GitHub, add screenshots of your running application to a `docs/screenshots/` directory and embed them below.

\<!-- Add screenshots when available: ![Dashboard Preview]\(docs/screenshots/dashboard.png) ![Task Management Preview]\(docs/screenshots/tasks.png) -->

---

## 🧰 Technology Stack

| Technology                   | Purpose                                |
| ---------------------------- | -------------------------------------- |
| ☕ Java 21                    | Backend development                    |
| 🌐 Jakarta Servlets          | HTTP request handling                  |
| 🐈 Apache Tomcat 11          | Servlet container and deployment       |
| 🗃️ PostgreSQL               | Relational database                    |
| 🔗 Hibernate ORM             | Object-relational mapping              |
| 📦 Jakarta Persistence (JPA) | Persistence API                        |
| 🧩 Maven                     | Dependency management and build        |
| 🧱 HTML5                     | Frontend structure                     |
| 🎨 CSS3                      | Responsive styling                     |
| ⚡ JavaScript                 | Dynamic UI and API communication       |
| 📄 Jackson                   | JSON serialization and deserialization |
| 🏷️ Lombok                   | Reducing Java boilerplate code         |

---

## 🏗️ Architecture

The application separates HTTP handling, business logic, and persistence responsibilities.

```text
Browser
   │
   ▼
HTML / CSS / JavaScript
   │
   │ Fetch API — JSON
   ▼
Apache Tomcat
   │
   ▼
Servlets & Filters
   │
   ▼
Services
   │
   ▼
Repositories
   │
   ▼
Hibernate / JPA
   │
   ▼
PostgreSQL
```

### 📂 Project Structure

```text
task-manager-web-app/
├── src/
│   └── main/
│       ├── java/
│       │   └── ir/reza/taskmanager/
│       │       ├── entity/
│       │       ├── dto/
│       │       ├── repository/
│       │       ├── service/
│       │       ├── servlet/
│       │       ├── filter/
│       │       ├── listener/
│       │       └── util/
│       ├── resources/
│       │   └── META-INF/
│       │       └── persistence.xml
│       └── webapp/
│           ├── index.html
│           ├── css/
│           │   └── style.css
│           ├── js/
│           │   └── app.js
│           └── http-requests/
├── .gitignore
├── pom.xml
└── README.md
```

*The structure above illustrates the main application layers; individual filenames may vary depending on the current implementation.*

---

## ⚙️ Getting Started

### 1. Prerequisites

Install and configure the following:

- Java Development Kit (JDK) 21
- Apache Maven
- PostgreSQL
- Apache Tomcat 11
- IntelliJ IDEA or another Java IDE

### 2. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/task-manager-web-app.git
cd task-manager-web-app
```

Replace `YOUR_USERNAME` with your GitHub username.

### 3. Create the Database

Open PostgreSQL and execute:

```sql
CREATE DATABASE task_manager;
```

### 4. Configure Database Access

Update `src/main/resources/META-INF/persistence.xml` with your PostgreSQL connection settings.

Configure the database URL, username, and password for your local environment.

**Security note:** Never commit real database passwords, production credentials, or other secrets to a public repository.

### 5. Build the Application

From the project root, run:

```bash
mvn clean package
```

Maven will compile the application and package the WAR artifact if the build succeeds.

### 6. Deploy to Tomcat

Deploy the generated WAR file from the `target/` directory to Apache Tomcat 11.

Start Tomcat and open the application using the context path configured for your deployment.

For example, if the deployed context path is `task_manager_war`:

```text
http://localhost:8080/task_manager_war/
```

---

## 🔌 API Endpoints

The application exposes HTTP endpoints for managing tasks, users, projects, and session visit statistics.

| Method   | Endpoint                 | Description                                  |
| -------- | ------------------------ | -------------------------------------------- |
| `GET`    | `/tasks`                 | Retrieve all tasks                           |
| `GET`    | `/tasks?id={id}`         | Retrieve a task by ID                        |
| `GET`    | `/tasks?assigneeId={id}` | Filter tasks by assignee                     |
| `POST`   | `/tasks/add`             | Create a task                                |
| `PUT`    | `/tasks/update?id={id}`  | Update a task                                |
| `DELETE` | `/tasks/delete?id={id}`  | Delete a task                                |
| `GET`    | `/users`                 | Retrieve users                               |
| `POST`   | `/users`                 | Create a user                                |
| `GET`    | `/projects`              | Retrieve projects                            |
| `POST`   | `/projects`              | Create a project                             |
| `GET`    | `/visits`                | Retrieve and update session visit statistics |

All endpoints should be accessed relative to the deployed application context path.

### 📄 Example JSON — Create a Task

```json
{
  "title": "Implement dashboard",
  "estimatedMinutes": 120,
  "assigneeId": 1,
  "projectId": 1
}
```

### 📄 Example JSON — Create a User

```json
{
  "name": "Alex Johnson",
  "email": "alex@example.com"
}
```

*Request fields and response formats should match the DTOs implemented by the application.*

---

## 🧪 Testing

The project includes an `http-requests/` directory for storing HTTP request examples that can be executed from a compatible IDE or HTTP client.

Recommended test scenarios:

- 📋 Retrieve tasks, users, and projects.
- ➕ Create a task and assign it to a user and project.
- ✏️ Update an existing task.
- 🗑️ Delete a task.
- 🔍 Filter tasks by assignee.
- 📈 Verify that session visit statistics update correctly.
- ⚠️ Test invalid IDs and malformed JSON requests.

---

## 🔐 Important Notes

- Keep database credentials outside version control.
- Verify that the database is running before starting the application.
- Ensure the Tomcat version supports the Jakarta Servlet APIs used by the project.
- Configure the correct application context path after deployment.
- Session visit statistics are associated with the current HTTP session, rather than being a global visitor counter.

---

## 🎯 Project Goals

This project demonstrates practical experience with:

- Java web application development.
- Servlet lifecycle and HTTP request handling.
- Layered application architecture.
- REST-style API design and JSON processing.
- Hibernate, JPA, and relational database integration.
- Session management and servlet filters.
- Dynamic frontend development.
- Maven-based builds and WAR deployment.

---

## 👨‍💻 Author

**Reza Asadipour**

Computer Engineering | Java Backend Development

---

⭐ If you find this project useful, consider giving the repository a star!
