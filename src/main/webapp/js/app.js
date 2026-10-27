"use strict";

// Automatically detect the deployed application context path.
const contextPath = window.location.pathname
    .replace(/\/index\.html$/, "")
    .replace(/\/$/, "");

const API = {
    tasks: `${contextPath}/tasks`,
    addTask: `${contextPath}/tasks/add`,
    updateTask: `${contextPath}/tasks/update`,
    deleteTask: `${contextPath}/tasks/delete`,
    users: `${contextPath}/users`,
    projects: `${contextPath}/projects`
};

const state = {
    tasks: [],
    users: [],
    projects: [],
    editingTaskId: null
};

let noticeTimer;

// -------------------------
// Shared helpers
// -------------------------

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

function initials(name) {
    const parts = String(name || "?").trim().split(/\s+/);
    return parts.slice(0, 2).map(part => part.charAt(0)).join("").toUpperCase();
}

function formatMinutes(value) {
    const minutes = Number(value) || 0;
    return `${minutes.toLocaleString()} min`;
}

function showNotice(message, isError = false) {
    const notice = document.getElementById("notice");

    notice.textContent = message;
    notice.classList.toggle("error", isError);
    notice.classList.remove("hidden");

    clearTimeout(noticeTimer);
    noticeTimer = setTimeout(() => {
        notice.classList.add("hidden");
    }, 4500);
}

async function apiRequest(url, options = {}) {
    const headers = {
        Accept: "application/json",
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers
    };

    let response;

    try {
        response = await fetch(url, {
            ...options,
            headers
        });
    } catch {
        throw new Error(
            "Cannot connect to the server. Check that Tomcat is running."
        );
    }

    const responseText = await response.text();
    let data = null;

    if (responseText.trim()) {
        try {
            data = JSON.parse(responseText);
        } catch {
            if (!response.ok) {
                throw new Error(`Request failed with HTTP ${response.status}.`);
            }

            throw new Error(
                "The server returned a non-JSON response. Check the servlet URL and deployment."
            );
        }
    }

    if (!response.ok) {
        const message = data?.message || data?.error ||
            `Request failed with HTTP ${response.status}.`;

        throw new Error(message);
    }

    return data;
}

function normalizeList(data, propertyName) {
    if (Array.isArray(data)) {
        return data;
    }

    if (Array.isArray(data?.[propertyName])) {
        return data[propertyName];
    }

    if (Array.isArray(data?.items)) {
        return data.items;
    }

    return [];
}

function setButtonLoading(button, loading, loadingText = "Please wait…") {
    if (loading) {
        button.dataset.originalText = button.textContent;
        button.textContent = loadingText;
        button.disabled = true;
    } else {
        button.textContent = button.dataset.originalText || button.textContent;
        button.disabled = false;
    }
}

// -------------------------
// Navigation
// -------------------------

const pageTitles = {
    dashboard: "Dashboard",
    tasks: "Tasks",
    users: "Users",
    projects: "Projects"
};

function navigateTo(page) {
    if (!pageTitles[page]) {
        page = "dashboard";
    }

    document.querySelectorAll(".page-section").forEach(section => {
        section.classList.toggle("hidden", section.id !== `page-${page}`);
    });

    document.querySelectorAll(".nav-link").forEach(link => {
        link.classList.toggle("active", link.dataset.page === page);
    });

    document.getElementById("page-title").textContent = pageTitles[page];
    document.getElementById("breadcrumb").textContent = page.toUpperCase();

    if (window.location.hash !== `#${page}`) {
        window.history.replaceState(null, "", `#${page}`);
    }
}

document.querySelectorAll(".nav-link").forEach(link => {
    link.addEventListener("click", event => {
        event.preventDefault();
        navigateTo(link.dataset.page);
    });
});

document.querySelectorAll("[data-go]").forEach(button => {
    button.addEventListener("click", () => {
        navigateTo(button.dataset.go);
    });
});

window.addEventListener("hashchange", () => {
    navigateTo(window.location.hash.slice(1));
});

document.getElementById("dashboard-add-task").addEventListener("click", () => {
    navigateTo("tasks");
    resetTaskForm();
    document.getElementById("task-title").focus();
});

// -------------------------
// Tasks
// -------------------------

async function loadTasks() {
    const data = await apiRequest(API.tasks);
    state.tasks = normalizeList(data, "tasks");

    renderTasks();
    renderRecentTasks();
    updateStatistics();
}

function renderTaskRows(tasks) {
    if (tasks.length === 0) {
        return `<tr><td colspan="5" class="empty-cell">No tasks found.</td></tr>`;
    }

    return tasks.map(task => {
        const assignee = task.assigneeName || "Unknown user";
        const project = task.projectName || "Unknown project";

        return `
            <tr>
                <td>
                    <span class="task-name">${escapeHtml(task.title)}</span>
                    <span class="task-id">TASK-${escapeHtml(task.id)}</span>
                </td>
                <td>
                    <div class="person-cell">
                        <span class="mini-avatar">${escapeHtml(initials(assignee))}</span>
                        <span>${escapeHtml(assignee)}</span>
                    </div>
                </td>
                <td>
                    <span class="project-name" title="${escapeHtml(project)}">
                        ${escapeHtml(project)}
                    </span>
                </td>
                <td><span class="time-value">${formatMinutes(task.estimatedMinutes)}</span></td>
                <td>
                    <div class="action-group">
                        <button class="icon-button" type="button"
                                data-edit-task="${escapeHtml(task.id)}"
                                aria-label="Edit ${escapeHtml(task.title)}"
                                title="Edit task">✎</button>
                        <button class="icon-button danger" type="button"
                                data-delete-task="${escapeHtml(task.id)}"
                                aria-label="Delete ${escapeHtml(task.title)}"
                                title="Delete task">×</button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");
}

function renderTasks() {
    const search = document.getElementById("task-search").value
        .trim().toLowerCase();

    const selectedUserId = document.getElementById("task-filter-user").value;

    const filteredTasks = state.tasks.filter(task => {
        const matchesSearch = [
            task.title,
            task.assigneeName,
            task.projectName,
            task.id
        ].some(value => String(value ?? "").toLowerCase().includes(search));

        const matchesUser = !selectedUserId ||
            String(task.assigneeId) === selectedUserId;

        return matchesSearch && matchesUser;
    });

    document.getElementById("task-count").textContent = filteredTasks.length;
    document.getElementById("tasks-body").innerHTML = renderTaskRows(filteredTasks);
}

function renderRecentTasks() {
    const recentTasks = [...state.tasks]
        .sort((a, b) => Number(b.id) - Number(a.id))
        .slice(0, 5);

    document.getElementById("recent-tasks-body").innerHTML =
        renderTaskRows(recentTasks);
}

function updateStatistics() {
    document.getElementById("stat-tasks").textContent =
        state.tasks.length.toLocaleString();

    document.getElementById("stat-users").textContent =
        state.users.length.toLocaleString();

    document.getElementById("stat-projects").textContent =
        state.projects.length.toLocaleString();

    const totalMinutes = state.tasks.reduce(
        (total, task) => total + (Number(task.estimatedMinutes) || 0),
        0
    );

    document.getElementById("stat-minutes").textContent =
        totalMinutes.toLocaleString();
}

function populateTaskSelects() {
    const assigneeSelect = document.getElementById("task-assignee");
    const filterSelect = document.getElementById("task-filter-user");
    const projectSelect = document.getElementById("task-project");

    const currentAssignee = assigneeSelect.value;
    const currentFilter = filterSelect.value;
    const currentProject = projectSelect.value;

    assigneeSelect.innerHTML = `
        <option value="">Select a user</option>
        ${state.users.map(user => `
            <option value="${escapeHtml(user.id)}">${escapeHtml(user.name)}</option>
        `).join("")}
    `;

    filterSelect.innerHTML = `
        <option value="">All users</option>
        ${state.users.map(user => `
            <option value="${escapeHtml(user.id)}">${escapeHtml(user.name)}</option>
        `).join("")}
    `;

    projectSelect.innerHTML = `
        <option value="">Select a project</option>
        ${state.projects.map(project => `
            <option value="${escapeHtml(project.id)}">${escapeHtml(project.name)}</option>
        `).join("")}
    `;

    if (state.users.some(user => String(user.id) === currentAssignee)) {
        assigneeSelect.value = currentAssignee;
    }

    if (state.users.some(user => String(user.id) === currentFilter)) {
        filterSelect.value = currentFilter;
    }

    if (state.projects.some(project => String(project.id) === currentProject)) {
        projectSelect.value = currentProject;
    }
}

function resetTaskForm() {
    state.editingTaskId = null;

    document.getElementById("task-form").reset();
    document.getElementById("task-form-title").textContent = "Create a task";
    document.getElementById("task-submit").textContent = "Create task";
    document.getElementById("task-cancel-edit").classList.add("hidden");
}

function startTaskEdit(taskId) {
    const task = state.tasks.find(item => String(item.id) === String(taskId));

    if (!task) {
        showNotice("The selected task could not be found.", true);
        return;
    }

    state.editingTaskId = task.id;

    document.getElementById("task-title").value = task.title || "";
    document.getElementById("task-minutes").value = task.estimatedMinutes ?? "";
    document.getElementById("task-assignee").value = task.assigneeId ?? "";
    document.getElementById("task-project").value = task.projectId ?? "";

    document.getElementById("task-form-title").textContent = "Edit task";
    document.getElementById("task-submit").textContent = "Save changes";
    document.getElementById("task-cancel-edit").classList.remove("hidden");

    navigateTo("tasks");
    document.getElementById("task-form-panel").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

document.getElementById("task-form").addEventListener("submit", async event => {
    event.preventDefault();

    const form = event.currentTarget;
    const submitButton = document.getElementById("task-submit");

    const title = document.getElementById("task-title").value.trim();
    const estimatedMinutes = Number(document.getElementById("task-minutes").value);
    const assigneeId = Number(document.getElementById("task-assignee").value);
    const projectId = Number(document.getElementById("task-project").value);

    if (!title || !Number.isInteger(estimatedMinutes) || estimatedMinutes <= 0 ||
        !Number.isInteger(assigneeId) || assigneeId <= 0 ||
        !Number.isInteger(projectId) || projectId <= 0) {
        showNotice("Enter a title, positive estimated minutes, user, and project.", true);
        return;
    }

    const payload = {
        title,
        estimatedMinutes,
        assigneeId,
        projectId
    };

    const isEditing = state.editingTaskId !== null;

    const url = isEditing
        ? `${API.updateTask}?id=${encodeURIComponent(state.editingTaskId)}`
        : API.addTask;

    setButtonLoading(submitButton, true, isEditing ? "Saving…" : "Creating…");

    try {
        await apiRequest(url, {
            method: isEditing ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });

        showNotice(isEditing ? "Task updated successfully." : "Task created successfully.");
        resetTaskForm();
        await loadTasks();
    } catch (error) {
        showNotice(error.message, true);
    } finally {
        setButtonLoading(submitButton, false);
    }
});

document.getElementById("task-cancel-edit").addEventListener("click", resetTaskForm);

document.getElementById("tasks-body").addEventListener("click", async event => {
    const editButton = event.target.closest("[data-edit-task]");
    const deleteButton = event.target.closest("[data-delete-task]");

    if (editButton) {
        startTaskEdit(editButton.dataset.editTask);
        return;
    }

    if (deleteButton) {
        await deleteTask(deleteButton.dataset.deleteTask);
    }
});

document.getElementById("recent-tasks-body").addEventListener("click", async event => {
    const editButton = event.target.closest("[data-edit-task]");
    const deleteButton = event.target.closest("[data-delete-task]");

    if (editButton) {
        startTaskEdit(editButton.dataset.editTask);
    } else if (deleteButton) {
        await deleteTask(deleteButton.dataset.deleteTask);
    }
});

async function deleteTask(taskId) {
    const task = state.tasks.find(item => String(item.id) === String(taskId));

    if (!task) {
        showNotice("The selected task could not be found.", true);
        return;
    }

    if (!window.confirm(`Delete "${task.title}"? This action cannot be undone.`)) {
        return;
    }

    try {
        await apiRequest(
            `${API.deleteTask}?id=${encodeURIComponent(taskId)}`,
            { method: "DELETE" }
        );

        if (String(state.editingTaskId) === String(taskId)) {
            resetTaskForm();
        }

        showNotice("Task deleted successfully.");
        await loadTasks();
    } catch (error) {
        showNotice(error.message, true);
    }
}

document.getElementById("task-search").addEventListener("input", renderTasks);
document.getElementById("task-filter-user").addEventListener("change", renderTasks);

document.getElementById("refresh-tasks").addEventListener("click", async () => {
    try {
        await loadTasks();
        showNotice("Tasks refreshed.");
    } catch (error) {
        showNotice(error.message, true);
    }
});

// -------------------------
// Users
// -------------------------

async function loadUsers() {
    const data = await apiRequest(API.users);
    state.users = normalizeList(data, "users");

    renderUsers();
    populateTaskSelects();
    updateStatistics();
}

function renderUsers() {
    const container = document.getElementById("users-list");

    if (state.users.length === 0) {
        container.innerHTML = `<div class="empty-state">No users found.</div>`;
        return;
    }

    container.innerHTML = state.users.map(user => `
        <article class="entity-card">
            <div class="entity-avatar">${escapeHtml(initials(user.name))}</div>
            <div class="entity-details">
                <strong>${escapeHtml(user.name)}</strong>
                <span>${escapeHtml(user.email)}</span>
            </div>
            <div class="entity-meta">#${escapeHtml(user.id)}</div>
        </article>
    `).join("");
}

document.getElementById("user-form").addEventListener("submit", async event => {
    event.preventDefault();

    const form = event.currentTarget;
    const button = form.querySelector('button[type="submit"]');

    const name = document.getElementById("user-name").value.trim();
    const email = document.getElementById("user-email").value.trim();

    if (!name || !email) {
        showNotice("Enter the user's name and email.", true);
        return;
    }

    setButtonLoading(button, true, "Adding…");

    try {
        await apiRequest(API.users, {
            method: "POST",
            body: JSON.stringify({ name, email })
        });

        form.reset();
        await loadUsers();
        showNotice("User added successfully.");
    } catch (error) {
        showNotice(error.message, true);
    } finally {
        setButtonLoading(button, false);
    }
});

document.getElementById("refresh-users").addEventListener("click", async () => {
    try {
        await loadUsers();
        showNotice("Users refreshed.");
    } catch (error) {
        showNotice(error.message, true);
    }
});

// -------------------------
// Projects
// -------------------------

async function loadProjects() {
    const data = await apiRequest(API.projects);
    state.projects = normalizeList(data, "projects");

    renderProjects();
    populateTaskSelects();
    updateStatistics();
}

function renderProjects() {
    const container = document.getElementById("projects-list");

    if (state.projects.length === 0) {
        container.innerHTML = `<div class="empty-state">No projects found.</div>`;
        return;
    }

    container.innerHTML = state.projects.map(project => `
        <article class="entity-card">
            <div class="entity-avatar">▣</div>
            <div class="entity-details">
                <strong>${escapeHtml(project.name)}</strong>
                <p class="entity-description">
                    ${escapeHtml(project.description || "No description provided.")}
                </p>
            </div>
            <div class="entity-meta">#${escapeHtml(project.id)}</div>
        </article>
    `).join("");
}

document.getElementById("project-form").addEventListener("submit", async event => {
    event.preventDefault();

    const form = event.currentTarget;
    const button = form.querySelector('button[type="submit"]');

    const name = document.getElementById("project-name").value.trim();
    const description = document.getElementById("project-description").value.trim();

    if (!name) {
        showNotice("Enter a project name.", true);
        return;
    }

    setButtonLoading(button, true, "Creating…");

    try {
        await apiRequest(API.projects, {
            method: "POST",
            body: JSON.stringify({ name, description })
        });

        form.reset();
        await loadProjects();
        showNotice("Project created successfully.");
    } catch (error) {
        showNotice(error.message, true);
    } finally {
        setButtonLoading(button, false);
    }
});

document.getElementById("refresh-projects").addEventListener("click", async () => {
    try {
        await loadProjects();
        showNotice("Projects refreshed.");
    } catch (error) {
        showNotice(error.message, true);
    }
});

// -------------------------
// Initial loading
// -------------------------

async function initializeApp() {
    navigateTo(window.location.hash.slice(1) || "dashboard");

    const results = await Promise.allSettled([
        loadUsers(),
        loadProjects(),
        loadTasks()
    ]);

    const failedRequests = results.filter(result => result.status === "rejected");

    if (failedRequests.length > 0) {
        const firstError = failedRequests[0].reason;
        showNotice(firstError.message || "Some data could not be loaded.", true);
    }
}

initializeApp();
