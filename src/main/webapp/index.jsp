<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Task Manager</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
            background-color: #f4f6f9;
            color: #222;
        }

        h1 {
            text-align: center;
            margin-bottom: 10px;
        }

        .description {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
        }

        .menu {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 16px;
        }

        .card {
            display: block;
            padding: 22px;
            background-color: white;
            border: 1px solid #ddd;
            border-radius: 10px;
            text-decoration: none;
            color: #222;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        .card:hover {
            transform: translateY(-3px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .card h2 {
            margin-top: 0;
            font-size: 20px;
        }

        .card p {
            margin-bottom: 0;
            color: #666;
            font-size: 14px;
        }

        footer {
            margin-top: 35px;
            text-align: center;
            color: #777;
            font-size: 13px;
        }
    </style>
</head>
<body>

<h1>Task Manager</h1>
<p class="description">
    Manage tasks, users, and projects in one place.
</p>

<div class="menu">

    <a class="card" href="${pageContext.request.contextPath}/tasks">
        <h2>Tasks</h2>
        <p>View tasks, filter by assignee, edit, and delete tasks.</p>
    </a>

    <a class="card" href="${pageContext.request.contextPath}/tasks/add">
        <h2>Add Task</h2>
        <p>Create a new task and assign a user or project.</p>
    </a>

    <a class="card" href="${pageContext.request.contextPath}/users">
        <h2>Users</h2>
        <p>View and add users.</p>
    </a>

    <a class="card" href="${pageContext.request.contextPath}/projects">
        <h2>Projects</h2>
        <p>View and add projects.</p>
    </a>

</div>

<footer>
    Task Manager Web Application
</footer>

</body>
</html>
