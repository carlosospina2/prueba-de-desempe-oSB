(function () {
  "use strict";

  const authPanel = document.getElementById("authPanel");
  const appPanel = document.getElementById("appPanel");
  const loginForm = document.getElementById("loginForm");
  const registerForm = document.getElementById("registerForm");
  const authMessage = document.getElementById("authMessage");
  const appMessage = document.getElementById("appMessage");
  const ticketList = document.getElementById("ticketList");
  const emptyState = document.getElementById("emptyState");
  const ticketForm = document.getElementById("ticketForm");
  const ticketLookupForm = document.getElementById("ticketLookupForm");
  const ticketIdInput = document.getElementById("ticketIdInput");
  const allStatuses = ["ABIERTO", "ASIGNADO", "EN_PROGRESO", "RESUELTO", "CERRADO", "CANCELADO"];
  let session = null;
  let currentTickets = [];

  function showMessage(element, message, kind) {
    element.textContent = message || "";
    element.className = "message" + (element === appMessage ? " banner" : "");
    if (kind) element.classList.add(kind);
  }

  function readableError(status, detail) {
    if (status === 401) return "Tu sesión no es válida o expiró. Inicia sesión nuevamente.";
    if (status === 403) return "Tu rol no tiene permiso para realizar esta acción.";
    if (status === 404) return detail || "No encontramos el recurso solicitado.";
    if (status === 400) return detail || "Revisa los datos ingresados.";
    if (status === 422) return detail || "La solicitud no cumple una regla del negocio.";
    return detail || "Ocurrió un error al comunicarse con HelpFlow.";
  }

  async function apiRequest(path, options) {
    const requestOptions = options || {};
    const headers = Object.assign({ Accept: "application/json" }, requestOptions.headers || {});
    if (requestOptions.body) headers["Content-Type"] = "application/json";
    if (session && session.token && requestOptions.auth !== false) {
      headers.Authorization = "Bearer " + session.token;
    }
    delete requestOptions.auth;
    requestOptions.headers = headers;

    let response;
    try {
      response = await fetch(path, requestOptions);
    } catch (error) {
      throw new Error("No se pudo conectar con la API. Verifica que support-ticket-service esté iniciado.");
    }

    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
      ? await response.json()
      : await response.text();

    if (!response.ok) {
      const detail = payload && typeof payload === "object" ? payload.detail : "";
      throw new Error(readableError(response.status, detail));
    }
    return payload;
  }

  function formValues(form) {
    return Object.fromEntries(new FormData(form).entries());
  }

  function setBusy(form, busy, label) {
    const button = form.querySelector('button[type="submit"]');
    if (!button) return;
    if (busy) {
      button.dataset.originalText = button.textContent;
      button.textContent = label || "Procesando...";
      button.disabled = true;
    } else {
      button.textContent = button.dataset.originalText || button.textContent;
      button.disabled = false;
    }
  }

  function activateSession(authResponse) {
    session = {
      token: authResponse.token,
      email: authResponse.email,
      role: authResponse.role
    };
    authPanel.classList.add("hidden");
    appPanel.classList.remove("hidden");
    document.getElementById("userName").textContent = session.email;
    document.getElementById("roleBadge").textContent = session.role.replace("ROLE_", "");
    document.getElementById("createTicketSection").classList.toggle(
      "hidden",
      session.role !== "ROLE_USUARIO" && session.role !== "ROLE_ADMIN"
    );

    const isTechnician = session.role === "ROLE_TECNICO";
    document.getElementById("ticketListTitle").textContent =
      session.role === "ROLE_ADMIN" ? "Todos los tickets" :
      isTechnician ? "Buscar ticket asignado" : "Mis tickets";
    document.getElementById("ticketHelp").textContent = isTechnician
      ? "Como técnico, consulta un ticket por su número para gestionarlo."
      : "Consulta el estado y los datos de tus solicitudes.";
    ticketLookupForm.classList.toggle("hidden", !isTechnician);
    showMessage(appMessage, "Sesión iniciada correctamente.", "success");
    loadTickets();
  }

  function clearSession() {
    session = null;
    currentTickets = [];
    appPanel.classList.add("hidden");
    authPanel.classList.remove("hidden");
    ticketList.replaceChildren();
    showMessage(authMessage, "Sesión cerrada.", "success");
  }

  function addPill(container, text, className) {
    const pill = document.createElement("span");
    pill.className = "pill" + (className ? " " + className : "");
    pill.textContent = text;
    container.appendChild(pill);
  }

  function makeTicketCard(ticket) {
    const card = document.createElement("article");
    card.className = "ticket-card";

    const top = document.createElement("div");
    top.className = "ticket-card-top";
    const headingGroup = document.createElement("div");
    const title = document.createElement("h3");
    title.textContent = ticket.title || "Ticket sin título";
    const description = document.createElement("p");
    description.className = "ticket-description";
    description.textContent = ticket.description || "";
    headingGroup.append(title, description);

    const id = document.createElement("span");
    id.className = "ticket-id";
    id.textContent = "#" + ticket.id;
    top.append(headingGroup, id);
    card.appendChild(top);

    const meta = document.createElement("div");
    meta.className = "ticket-meta";
    addPill(meta, ticket.status || "SIN ESTADO", "status");
    addPill(meta, ticket.category || "SIN CATEGORÍA");
    addPill(meta, ticket.priority || "SIN PRIORIDAD");
    if (ticket.sede) addPill(meta, ticket.sede);
    if (ticket.assignedTechnicianId) addPill(meta, "Técnico #" + ticket.assignedTechnicianId);
    if (ticket.slaDeadline) addPill(meta, "SLA: " + formatDate(ticket.slaDeadline));
    card.appendChild(meta);

    if (session.role === "ROLE_ADMIN" || session.role === "ROLE_TECNICO") {
      const actions = document.createElement("div");
      actions.className = "ticket-actions";

      const statusLabel = document.createElement("label");
      statusLabel.textContent = "Cambiar estado";
      const statusSelect = document.createElement("select");
      statusSelect.setAttribute("aria-label", "Nuevo estado del ticket " + ticket.id);
      allStatuses.forEach(function (status) {
        const option = document.createElement("option");
        option.value = status;
        option.textContent = status.replaceAll("_", " ");
        option.selected = status === ticket.status;
        statusSelect.appendChild(option);
      });
      statusLabel.appendChild(statusSelect);

      const updateButton = document.createElement("button");
      updateButton.type = "button";
      updateButton.className = "button secondary";
      updateButton.textContent = "Actualizar";
      updateButton.addEventListener("click", function () {
        updateStatus(ticket.id, statusSelect.value);
      });
      actions.append(statusLabel, updateButton);

      if (session.role === "ROLE_ADMIN") {
        const assignButton = document.createElement("button");
        assignButton.type = "button";
        assignButton.className = "button secondary";
        assignButton.textContent = "Reasignar técnico";
        assignButton.addEventListener("click", function () {
          reassignTicket(ticket.id);
        });
        actions.appendChild(assignButton);
      }
      card.appendChild(actions);
    }
    return card;
  }

  function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString("es-CO");
  }

  function renderTickets(tickets) {
    currentTickets = Array.isArray(tickets) ? tickets : [tickets];
    ticketList.replaceChildren();
    emptyState.classList.toggle("hidden", currentTickets.length > 0);
    currentTickets.forEach(function (ticket) {
      ticketList.appendChild(makeTicketCard(ticket));
    });
  }

  async function loadTickets() {
    if (!session) return;
    showMessage(appMessage, "Cargando tickets...");
    try {
      if (session.role === "ROLE_TECNICO") {
        renderTickets([]);
        showMessage(appMessage, "Escribe el número de ticket para consultarlo.", "");
        return;
      }
      const path = session.role === "ROLE_ADMIN" ? "/api/tickets" : "/api/tickets/my";
      const tickets = await apiRequest(path, { method: "GET" });
      renderTickets(tickets);
      showMessage(appMessage, tickets.length + " ticket(s) cargados.", "success");
    } catch (error) {
      showMessage(appMessage, error.message, "error");
    }
  }

  async function lookupTicket(ticketId) {
    try {
      const ticket = await apiRequest("/api/tickets/" + encodeURIComponent(ticketId), { method: "GET" });
      renderTickets([ticket]);
      showMessage(appMessage, "Ticket #" + ticket.id + " encontrado.", "success");
    } catch (error) {
      showMessage(appMessage, error.message, "error");
    }
  }

  async function updateStatus(ticketId, newStatus) {
    const observation = window.prompt("Agrega una observación para el historial:", "");
    if (observation === null) return;
    try {
      await apiRequest("/api/tickets/" + ticketId + "/status", {
        method: "PATCH",
        body: JSON.stringify({ newStatus: newStatus, observation: observation })
      });
      showMessage(appMessage, "Estado del ticket actualizado.", "success");
      if (session.role === "ROLE_TECNICO") await lookupTicket(ticketId);
      else await loadTickets();
    } catch (error) {
      showMessage(appMessage, error.message, "error");
    }
  }

  async function reassignTicket(ticketId) {
    if (!window.confirm("¿Quieres solicitar la reasignación del ticket #" + ticketId + "?")) return;
    try {
      await apiRequest("/api/tickets/" + ticketId + "/assign", { method: "POST" });
      showMessage(appMessage, "Solicitud de reasignación procesada.", "success");
      await loadTickets();
    } catch (error) {
      showMessage(appMessage, error.message, "error");
    }
  }

  document.getElementById("loginTab").addEventListener("click", function () {
    loginForm.classList.remove("hidden");
    registerForm.classList.add("hidden");
    this.classList.add("active");
    document.getElementById("registerTab").classList.remove("active");
    this.setAttribute("aria-selected", "true");
    document.getElementById("registerTab").setAttribute("aria-selected", "false");
    showMessage(authMessage, "");
  });

  document.getElementById("registerTab").addEventListener("click", function () {
    registerForm.classList.remove("hidden");
    loginForm.classList.add("hidden");
    this.classList.add("active");
    document.getElementById("loginTab").classList.remove("active");
    this.setAttribute("aria-selected", "true");
    document.getElementById("loginTab").setAttribute("aria-selected", "false");
    showMessage(authMessage, "");
  });

  loginForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    setBusy(loginForm, true, "Ingresando...");
    showMessage(authMessage, "Validando tus datos...");
    try {
      const response = await apiRequest("/auth/login", {
        method: "POST",
        auth: false,
        body: JSON.stringify(formValues(loginForm))
      });
      activateSession(response);
      loginForm.reset();
    } catch (error) {
      showMessage(authMessage, error.message, "error");
    } finally {
      setBusy(loginForm, false);
    }
  });

  registerForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    setBusy(registerForm, true, "Creando cuenta...");
    showMessage(authMessage, "Registrando usuario...");
    try {
      const body = formValues(registerForm);
      const response = await apiRequest("/auth/register", {
        method: "POST",
        auth: false,
        body: JSON.stringify(body)
      });
      activateSession(response);
      registerForm.reset();
    } catch (error) {
      showMessage(authMessage, error.message, "error");
    } finally {
      setBusy(registerForm, false);
    }
  });

  ticketForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    setBusy(ticketForm, true, "Enviando...");
    try {
      const body = formValues(ticketForm);
      if (!body.affectedAsset) delete body.affectedAsset;
      const ticket = await apiRequest("/api/tickets", {
        method: "POST",
        body: JSON.stringify(body)
      });
      ticketForm.reset();
      showMessage(appMessage, "Ticket #" + ticket.id + " creado correctamente.", "success");
      await loadTickets();
    } catch (error) {
      showMessage(appMessage, error.message, "error");
    } finally {
      setBusy(ticketForm, false);
    }
  });

  ticketLookupForm.addEventListener("submit", function (event) {
    event.preventDefault();
    lookupTicket(ticketIdInput.value);
  });

  document.getElementById("refreshButton").addEventListener("click", loadTickets);
  document.getElementById("logoutButton").addEventListener("click", clearSession);
}());
