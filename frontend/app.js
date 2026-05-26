const API_URL = window.REVISA_API_URL || "http://localhost:8080";

const state = {
  token: localStorage.getItem("revisa.token"),
  theme: localStorage.getItem("revisa.theme") || "light",
  user: null,
  dashboard: null,
  materias: [],
  conteudos: [],
  pendentes: [],
  conteudoFilters: {
    dominio: "",
    inicio: "",
    fim: "",
  },
  dominioModalConteudoId: null,
  currentView: "dashboard",
  selectedCalendarDate: todayIsoDate(),
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

function applyTheme() {
  const isDark = state.theme === "dark";
  document.body.classList.toggle("dark-theme", isDark);
  const toggle = $("#themeToggle");
  if (toggle) {
    toggle.title = isDark ? "Modo claro" : "Modo escuro";
    toggle.setAttribute("aria-label", toggle.title);
  }

  const icon = $("#themeIcon");
  if (icon) {
    icon.textContent = isDark ? "☾" : "☀";
  }
}

function setMessage(target, text, type = "") {
  const el = $(target);
  if (!el) return;
  el.textContent = text || "";
  el.className = `message ${type}`.trim();
}

function clearSession(message = "") {
  localStorage.removeItem("revisa.token");
  state.token = null;
  state.user = null;
  state.dashboard = null;
  state.materias = [];
  state.conteudos = [];
  state.pendentes = [];
  state.dominioModalConteudoId = null;
  showAuth();
  closeDominioModal();
  setMessage("#appMessage", "");
  if (message) {
    setMessage("#authMessage", message, "error");
  }
}

function setButtonLoading(button, isLoading, loadingText = "Aguarde...") {
  if (!button) return;

  if (isLoading) {
    button.dataset.originalText = button.textContent;
    button.textContent = loadingText;
    button.disabled = true;
    button.classList.add("is-loading");
    return;
  }

  button.textContent = button.dataset.originalText || button.textContent;
  button.disabled = false;
  button.classList.remove("is-loading");
  delete button.dataset.originalText;
}

async function withButtonLoading(button, loadingText, action) {
  setButtonLoading(button, true, loadingText);
  try {
    return await action();
  } finally {
    setButtonLoading(button, false);
  }
}

function normalizeErrorMessage(message) {
  if (!message) return "Erro na requisição";

  return message
    .split("; ")
    .map((part) => part.replace(/^[a-zA-Z][a-zA-Z0-9_]*:\s*/, ""))
    .join("; ");
}

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json; charset=utf-8",
    ...(options.headers || {}),
  };

  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }

  let response;

  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers,
    });
  } catch {
    throw new Error("Não foi possível conectar à API. Verifique se o backend está rodando.");
  }

  const contentType = response.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const body = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    if (response.status === 401) {
      clearSession("Sua sessão expirou. Entre novamente para continuar.");
    }

    if (response.status === 403) {
      setMessage("#appMessage", "Você não tem permissão para esta ação.", "error");
    }

    const message = typeof body === "string" ? body : body.mensagem || "Erro na requisição";
    throw new Error(normalizeErrorMessage(message));
  }

  return body;
}

function asList(response) {
  if (Array.isArray(response)) return response;
  if (Array.isArray(response?.content)) return response.content;
  return [];
}

function showAuth() {
  $("#authScreen")?.classList.remove("hidden");
  $("#appShell")?.classList.add("hidden");
}

function showApp() {
  $("#authScreen")?.classList.add("hidden");
  $("#appShell")?.classList.remove("hidden");
}

function activateView(view) {
  const targetView = $(`#${view}View`);
  if (!targetView) return;

  setMessage("#appMessage", "");
  state.currentView = view;
  $$(".nav-item").forEach((item) => item.classList.toggle("active", item.dataset.view === view));
  $$(".view").forEach((section) => section.classList.remove("active-view"));
  targetView.classList.add("active-view");

  const titles = {
    dashboard: "Início",
    materias: "Matérias",
    conteudos: "Conteúdos",
    revisoes: "Revisões",
    perfil: "Perfil",
  };

  const title = $("#viewTitle");
  if (title) title.textContent = titles[view] || "Revisa";
}

function formatDate(date) {
  if (!date) return "Sem data";
  const [year, month, day] = date.split("-");
  return `${day}/${month}/${year}`;
}

function todayIsoDate() {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function safeColor(value) {
  return /^#[0-9a-fA-F]{6}$/.test(value || "") ? value : "#6b7280";
}

function applyDateLimits() {
  const today = todayIsoDate();
  const dataEstudo = $("#conteudoData");

  if (dataEstudo) {
    dataEstudo.max = today;
  }
}

function monthLabel(date) {
  return date.toLocaleDateString("pt-BR", { month: "long", year: "numeric" });
}

function emptyCard(text) {
  return emptyState("Nada por aqui ainda", text);
}

function emptyState(title, text, actionLabel = "", actionView = "") {
  return `
    <article class="empty-state">
      <div>
        <strong>${escapeHtml(title)}</strong>
        <p>${escapeHtml(text)}</p>
      </div>
      ${actionLabel && actionView ? `<button class="secondary-action" type="button" data-empty-view="${actionView}">${escapeHtml(actionLabel)}</button>` : ""}
    </article>
  `;
}

function getFilteredConteudos() {
  return state.conteudos.filter((conteudo) => {
    if (state.conteudoFilters.dominio && String(conteudo.nivelDominio) !== state.conteudoFilters.dominio) {
      return false;
    }

    if (state.conteudoFilters.inicio && conteudo.dataEstudo < state.conteudoFilters.inicio) {
      return false;
    }

    if (state.conteudoFilters.fim && conteudo.dataEstudo > state.conteudoFilters.fim) {
      return false;
    }

    return true;
  });
}

function renderMateriaCard(materia) {
  return `
    <article class="item-card">
      <div class="item-row">
        <div>
          <div class="item-title">
            <span class="color-dot" style="background:${safeColor(materia.cor)}"></span>
            ${escapeHtml(materia.nome)}
          </div>
          <p class="item-meta">${escapeHtml(materia.descricao || "Sem descrição")}</p>
        </div>
        <div class="actions-row">
          <button class="item-action" data-edit-materia="${materia.id}">Editar</button>
          <button class="item-action danger" data-delete-materia="${materia.id}">Excluir</button>
        </div>
      </div>
    </article>
  `;
}

function renderConteudoCard(conteudo, includeReviewActions = true) {
  const concluido = conteudo.concluido ? "Concluído" : "Em andamento";
  const dominio = conteudo.nivelDominio ? `Domínio ${conteudo.nivelDominio}` : "Domínio não informado";
  const revisionStatus = getRevisionStatus(conteudo);
  const statusAction = conteudo.concluido
    ? `<button class="item-action" data-reativar="${conteudo.id}">Reativar</button>`
    : `<button class="item-action" data-concluir="${conteudo.id}">Concluir</button>`;

  return `
    <article class="item-card">
      <div class="item-row">
        <div>
          <div class="item-title">
            ${escapeHtml(conteudo.titulo)}
            ${revisionStatus ? `<span class="status-badge ${revisionStatus.type}">${revisionStatus.label}</span>` : ""}
          </div>
          <p class="item-meta">${escapeHtml(conteudo.descricao || "Sem descrição")}</p>
          <p class="item-meta">
            ${escapeHtml(conteudo.materiaNome || "Matéria")} · estudo em ${formatDate(conteudo.dataEstudo)}
            · próxima revisão ${formatDate(conteudo.proximaRevisao)}
            · ${conteudo.quantidadeRevisoes ?? 0} revisões
            · ${dominio}
            · ${concluido}
          </p>
          ${conteudo.observacaoEvolucao ? `<p class="item-meta">Observação: ${escapeHtml(conteudo.observacaoEvolucao)}</p>` : ""}
        </div>
        ${includeReviewActions ? `
          <div class="actions-row">
            <button class="item-action" data-revisar="${conteudo.id}">Revisar</button>
            <button class="item-action" data-evolucao="${conteudo.id}">Domínio</button>
            <button class="item-action" data-edit-conteudo="${conteudo.id}">Editar</button>
            <button class="item-action danger" data-delete-conteudo="${conteudo.id}">Excluir</button>
            ${statusAction}
          </div>
        ` : ""}
      </div>
    </article>
  `;
}

function sortByReviewDate(items) {
  return [...items].sort((a, b) => String(a.proximaRevisao || "").localeCompare(String(b.proximaRevisao || "")));
}

function getRevisionGroups() {
  const today = todayIsoDate();
  const activeWithReview = state.conteudos.filter((conteudo) => !conteudo.concluido && conteudo.proximaRevisao);

  return {
    atrasadas: sortByReviewDate(activeWithReview.filter((conteudo) => conteudo.proximaRevisao < today)),
    hoje: sortByReviewDate(activeWithReview.filter((conteudo) => conteudo.proximaRevisao === today)),
    proximas: sortByReviewDate(activeWithReview.filter((conteudo) => conteudo.proximaRevisao > today)).slice(0, 8),
  };
}

function getRevisionStatus(conteudo) {
  if (conteudo.concluido || !conteudo.proximaRevisao) return null;

  const today = todayIsoDate();
  if (conteudo.proximaRevisao < today) return { type: "late", label: "Atrasada" };
  if (conteudo.proximaRevisao === today) return { type: "today", label: "Hoje" };
  return { type: "upcoming", label: "Próxima" };
}

function renderRevisionGroup(selector, items, emptyText) {
  const target = $(selector);
  if (!target) return;
  target.innerHTML = items.length
    ? items.map((conteudo) => renderConteudoCard(conteudo)).join("")
    : emptyCard(emptyText);
}

function renderDashboardFocus(groups) {
  const totalUrgent = groups.atrasadas.length + groups.hoje.length;
  const nextReview = groups.proximas[0];

  $("#metricAtrasadas").textContent = groups.atrasadas.length;
  $("#metricHoje").textContent = groups.hoje.length;
  $("#metricProximas").textContent = groups.proximas.length;
  $("#revisionLateCount").textContent = groups.atrasadas.length;
  $("#revisionTodayCount").textContent = groups.hoje.length;
  $("#revisionUpcomingCount").textContent = groups.proximas.length;

  if (groups.atrasadas.length) {
    $("#dashboardStatusText").textContent = `Você tem ${groups.atrasadas.length} revisão${groups.atrasadas.length > 1 ? "ões" : ""} atrasada${groups.atrasadas.length > 1 ? "s" : ""}.`;
  } else if (groups.hoje.length) {
    $("#dashboardStatusText").textContent = `Você tem ${groups.hoje.length} revisão${groups.hoje.length > 1 ? "ões" : ""} para hoje.`;
  } else {
    $("#dashboardStatusText").textContent = "Você está em dia com as revisões.";
  }

  if (nextReview) {
    $("#dashboardNextReview").textContent = `Próxima revisão: ${nextReview.titulo} em ${formatDate(nextReview.proximaRevisao)}.`;
  } else if (totalUrgent) {
    $("#dashboardNextReview").textContent = "Conclua as pendências para liberar o próximo ciclo de estudos.";
  } else {
    $("#dashboardNextReview").textContent = "Nenhuma próxima revisão cadastrada no momento.";
  }
}

function renderMateriasChart() {
  const target = $("#materiasChart");
  if (!target) return;

  const rows = state.materias.map((materia) => {
    const total = state.conteudos.filter((conteudo) => String(conteudo.materiaId) === String(materia.id)).length;
    return {
      nome: materia.nome,
      cor: safeColor(materia.cor),
      total,
    };
  }).filter((row) => row.total > 0);

  const max = Math.max(...rows.map((row) => row.total), 1);

  target.innerHTML = rows.length
    ? rows.map((row) => `
      <div class="bar-row">
        <span class="bar-label" title="${escapeHtml(row.nome)}">${escapeHtml(row.nome)}</span>
        <span class="bar-track">
          <span class="bar-fill" style="--bar-width:${Math.max(8, (row.total / max) * 100)}%; --bar-color:${row.cor}"></span>
        </span>
        <strong>${row.total}</strong>
      </div>
    `).join("")
    : emptyCard("Cadastre conteúdos para visualizar o gráfico.");
}

function renderDominioChart() {
  const target = $("#dominioChart");
  if (!target) return;

  const counts = [1, 2, 3, 4, 5].map((nivel) => ({
    nivel,
    total: state.conteudos.filter((conteudo) => Number(conteudo.nivelDominio) === nivel).length,
  }));
  const max = Math.max(...counts.map((item) => item.total), 1);
  const hasData = counts.some((item) => item.total > 0);

  target.innerHTML = hasData
    ? counts.map((item) => `
      <div class="domain-row">
        <span>Domínio ${item.nivel}</span>
        <span class="bar-track">
          <span class="bar-fill" style="--bar-width:${item.total ? Math.max(8, (item.total / max) * 100) : 0}%"></span>
        </span>
        <strong>${item.total}</strong>
      </div>
    `).join("")
    : emptyCard("Registre domínio nos conteúdos para gerar este gráfico.");
}

function renderRevisionCalendar() {
  const target = $("#revisionCalendar");
  const title = $("#calendarTitle");
  if (!target || !title) return;

  const today = todayIsoDate();
  const [year, month] = today.split("-").map(Number);
  const firstDay = new Date(year, month - 1, 1);
  const daysInMonth = new Date(year, month, 0).getDate();
  const startsAt = firstDay.getDay();
  const activeReviews = state.conteudos.filter((conteudo) => !conteudo.concluido && conteudo.proximaRevisao);
  const reviewsByDate = activeReviews.reduce((acc, conteudo) => {
    acc[conteudo.proximaRevisao] = acc[conteudo.proximaRevisao] || [];
    acc[conteudo.proximaRevisao].push(conteudo);
    return acc;
  }, {});

  title.textContent = `Calendário de revisões - ${monthLabel(firstDay)}`;

  const emptyDays = Array.from({ length: startsAt }, () => `<div class="calendar-day is-empty"></div>`);
  const days = Array.from({ length: daysInMonth }, (_, index) => {
    const day = index + 1;
    const date = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    const reviews = reviewsByDate[date] || [];
    const status = date < today ? "late" : date === today ? "today" : "upcoming";
    const titleText = reviews.map((item) => item.titulo).join(", ");

    return `
      <button class="calendar-day ${reviews.length ? `has-review ${status}` : ""} ${date === today ? "is-today" : ""} ${date === state.selectedCalendarDate ? "is-selected" : ""}" type="button" data-calendar-date="${date}" title="${escapeHtml(titleText || "Sem revisões")}">
        <span class="calendar-day-number">${day}</span>
        ${reviews.length ? `<span class="calendar-day-count">${reviews.length} rev.</span>` : ""}
      </button>
    `;
  });

  target.innerHTML = [...emptyDays, ...days].join("");
  renderCalendarDayDetails(reviewsByDate);
}

function renderCalendarDayDetails(reviewsByDate = null) {
  const target = $("#calendarDayDetails");
  if (!target) return;

  const map = reviewsByDate || state.conteudos
    .filter((conteudo) => !conteudo.concluido && conteudo.proximaRevisao)
    .reduce((acc, conteudo) => {
      acc[conteudo.proximaRevisao] = acc[conteudo.proximaRevisao] || [];
      acc[conteudo.proximaRevisao].push(conteudo);
      return acc;
    }, {});

  const selectedDate = state.selectedCalendarDate || todayIsoDate();
  const reviews = sortByReviewDate(map[selectedDate] || []);

  target.innerHTML = `
    <div class="calendar-details-header">
      <strong>${formatDate(selectedDate)}</strong>
      <span>${reviews.length ? `${reviews.length} revisão${reviews.length > 1 ? "ões" : ""}` : "Sem revisão"}</span>
    </div>
    <div class="item-list">
      ${reviews.length
        ? reviews.map((conteudo) => renderConteudoCard(conteudo)).join("")
        : emptyState("Dia livre", "Nenhuma revisão agendada para este dia.", "Ver conteúdos", "conteudos")}
    </div>
  `;
}

function renderDashboardCharts() {
  renderMateriasChart();
  renderDominioChart();
}

function renderAll() {
  const filteredConteudos = getFilteredConteudos();
  const revisionGroups = getRevisionGroups();
  const priorityReviews = [...revisionGroups.atrasadas, ...revisionGroups.hoje].slice(0, 4);

  $("#userEmail").textContent = state.user?.email || "";
  $("#metricMaterias").textContent = state.dashboard?.totalMaterias ?? state.materias.length;
  $("#metricConteudos").textContent = state.dashboard?.totalConteudos ?? state.conteudos.length;
  $("#metricPendentes").textContent = state.dashboard?.conteudosPendentes ?? state.pendentes.length;
  $("#metricAtivos").textContent = state.dashboard?.conteudosAtivos ?? state.conteudos.filter((item) => !item.concluido).length;
  $("#metricConcluidos").textContent = state.dashboard?.conteudosConcluidos ?? state.conteudos.filter((item) => item.concluido).length;
  $("#metricDominio").textContent = Number(state.dashboard?.mediaDominio ?? 0).toFixed(1);
  renderDashboardFocus(revisionGroups);
  renderDashboardCharts();
  renderRevisionCalendar();

  $("#materiasList").innerHTML = state.materias.length
    ? state.materias.map(renderMateriaCard).join("")
    : emptyState("Nenhuma matéria cadastrada", "Crie uma matéria para organizar seus conteúdos de estudo.", "Criar matéria", "materias");

  $("#dashboardMaterias").innerHTML = state.materias.length
    ? state.materias.slice(0, 4).map(renderMateriaCard).join("")
    : emptyState("Comece por uma matéria", "Depois dela você já consegue cadastrar conteúdos e gerar revisões.", "Criar matéria", "materias");

  $("#conteudosList").innerHTML = filteredConteudos.length
    ? filteredConteudos.map((conteudo) => renderConteudoCard(conteudo)).join("")
    : emptyState("Nenhum conteúdo encontrado", state.materias.length ? "Cadastre um conteúdo ou ajuste os filtros aplicados." : "Cadastre uma matéria antes de adicionar conteúdos.", state.materias.length ? "Cadastrar conteúdo" : "Criar matéria", state.materias.length ? "conteudos" : "materias");

  renderRevisionGroup("#revisoesAtrasadas", revisionGroups.atrasadas, "Nenhuma revisão atrasada. Bom sinal.");
  renderRevisionGroup("#revisoesHoje", revisionGroups.hoje, "Nenhuma revisão para hoje.");
  renderRevisionGroup("#revisoesProximas", revisionGroups.proximas, "Nenhuma próxima revisão encontrada.");

  $("#dashboardPendentes").innerHTML = priorityReviews.length
    ? priorityReviews.map((conteudo) => renderConteudoCard(conteudo)).join("")
    : emptyState("Tudo em dia", "Quando houver revisões atrasadas ou para hoje, elas aparecem aqui.", "Ver calendário", "revisoes");

  $("#conteudoMateria").innerHTML = state.materias.length
    ? state.materias.map((materia) => `<option value="${materia.id}">${escapeHtml(materia.nome)}</option>`).join("")
    : `<option value="">Cadastre uma matéria primeiro</option>`;

  $("#perfilNome").value = state.user?.nome || "";
}

function resetConteudoForm() {
  $("#conteudoForm").reset();
  $("#conteudoId").value = "";
  $("#saveConteudoButton").textContent = "Cadastrar conteúdo";
  $("#cancelConteudoEdit").classList.add("hidden");
}

function openDominioModal(conteudoId) {
  const conteudo = state.conteudos.find((item) => String(item.id) === String(conteudoId));
  if (!conteudo) return;

  state.dominioModalConteudoId = conteudo.id;
  $("#dominioConteudoId").value = conteudo.id;
  $("#dominioNivel").value = conteudo.nivelDominio ? String(conteudo.nivelDominio) : "";
  $("#dominioObservacao").value = conteudo.observacaoEvolucao || "";
  $("#dominioModalTitle").textContent = `Registrar domínio - ${conteudo.titulo}`;
  setMessage("#dominioMessage", "");
  $("#dominioModal").classList.remove("hidden");
  $("#dominioNivel").focus();
}

function closeDominioModal() {
  state.dominioModalConteudoId = null;
  $("#dominioForm").reset();
  $("#dominioModal").classList.add("hidden");
  setMessage("#dominioMessage", "");
}

async function loadData() {
  state.user = await request("/users/me");
  state.dashboard = await request("/dashboard").catch(() => null);
  state.materias = asList(await request("/materias"));

  try {
    state.conteudos = asList(await request("/conteudos"));
  } catch {
    const nested = await Promise.all(
      state.materias.map((materia) => request(`/materias/${materia.id}/conteudos`).then(asList).catch(() => []))
    );
    state.conteudos = nested.flat();
  }

  state.pendentes = asList(await request("/conteudos/pendentes").catch(() => []));
  renderAll();
}

async function bootstrap() {
  if (!state.token) {
    showAuth();
    return;
  }

  try {
    await loadData();
    showApp();
  } catch (error) {
    clearSession("Sua sessão expirou. Entre novamente para continuar.");
  }
}

function bindAuth() {
  $$(".tab").forEach((tab) => {
    tab.addEventListener("click", () => {
      $$(".tab").forEach((item) => item.classList.remove("active"));
      tab.classList.add("active");
      const isLogin = tab.dataset.authTab === "login";
      $("#loginForm").classList.toggle("hidden", !isLogin);
      $("#registerForm").classList.toggle("hidden", isLogin);
      setMessage("#authMessage", "");
    });
  });

  $("#loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.submitter;
    setMessage("#authMessage", "");

    try {
      const response = await withButtonLoading(button, "Entrando...", () => {
        return request("/auth/login", {
          method: "POST",
          body: JSON.stringify({
            email: $("#loginEmail").value,
            senha: $("#loginSenha").value,
          }),
        });
      });

      state.token = response.token;
      localStorage.setItem("revisa.token", response.token);
      showApp();
      await loadData();
      setMessage("#appMessage", "Login realizado.", "success");
    } catch (error) {
      setMessage("#authMessage", error.message, "error");
    }
  });

  $("#registerForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.submitter;
    setMessage("#authMessage", "");

    try {
      await withButtonLoading(button, "Criando...", () => {
        return request("/users", {
          method: "POST",
          body: JSON.stringify({
            nome: $("#registerNome").value,
            email: $("#registerEmail").value,
            senha: $("#registerSenha").value,
          }),
        });
      });

      $("#loginEmail").value = $("#registerEmail").value;
      $("#loginSenha").value = $("#registerSenha").value;
      $("[data-auth-tab='login']").click();
      setMessage("#authMessage", "Conta criada. Agora entre.", "success");
    } catch (error) {
      setMessage("#authMessage", error.message, "error");
    }
  });
}

function bindApp() {
  $$(".nav-item").forEach((item) => {
    item.addEventListener("click", () => activateView(item.dataset.view));
  });

  $("#refreshButton").addEventListener("click", async () => {
    setMessage("#appMessage", "");
    try {
      await withButtonLoading($("#refreshButton"), "Atualizando...", loadData);
      setMessage("#appMessage", "Dados atualizados.", "success");
    } catch (error) {
      setMessage("#appMessage", error.message, "error");
    }
  });

  $("#logoutButton").addEventListener("click", () => {
    localStorage.removeItem("revisa.token");
    state.token = null;
    showAuth();
  });

  $("#themeToggle").addEventListener("click", () => {
    state.theme = state.theme === "dark" ? "light" : "dark";
    localStorage.setItem("revisa.theme", state.theme);
    applyTheme();
  });

  $("#materiaForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.submitter;
    const id = $("#materiaId").value;
    const payload = {
      nome: $("#materiaNome").value,
      descricao: $("#materiaDescricao").value,
      cor: $("#materiaCor").value,
    };

    try {
      await withButtonLoading(button, "Salvando...", () => {
        return request(id ? `/materias/${id}` : "/materias", {
          method: id ? "PUT" : "POST",
          body: JSON.stringify(payload),
        });
      });
      event.target.reset();
      $("#materiaId").value = "";
      $("#materiaCor").value = "#3b82f6";
      $("#cancelMateriaEdit").classList.add("hidden");
      await loadData();
      setMessage("#appMessage", "Matéria salva.", "success");
    } catch (error) {
      setMessage("#appMessage", error.message, "error");
    }
  });

  $("#cancelMateriaEdit").addEventListener("click", () => {
    $("#materiaForm").reset();
    $("#materiaId").value = "";
    $("#materiaCor").value = "#3b82f6";
    $("#cancelMateriaEdit").classList.add("hidden");
  });

  $("#conteudoForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.submitter;
    const materiaId = $("#conteudoMateria").value;
    const conteudoId = $("#conteudoId").value;

    if (!materiaId) {
      setMessage("#appMessage", "Cadastre uma matéria antes.", "error");
      return;
    }

    try {
      await withButtonLoading(button, "Salvando...", () => {
        return request(conteudoId ? `/conteudos/${conteudoId}` : `/materias/${materiaId}/conteudos`, {
          method: conteudoId ? "PUT" : "POST",
          body: JSON.stringify({
            titulo: $("#conteudoTitulo").value,
            descricao: $("#conteudoDescricao").value,
            dataEstudo: $("#conteudoData").value,
          }),
        });
      });
      resetConteudoForm();
      await loadData();
      setMessage("#appMessage", conteudoId ? "Conteúdo atualizado." : "Conteúdo cadastrado.", "success");
    } catch (error) {
      setMessage("#appMessage", error.message, "error");
    }
  });

  $("#cancelConteudoEdit").addEventListener("click", resetConteudoForm);

  ["#filterDominio", "#filterInicio", "#filterFim"].forEach((selector) => {
    $(selector).addEventListener("input", () => {
      state.conteudoFilters = {
        dominio: $("#filterDominio").value,
        inicio: $("#filterInicio").value,
        fim: $("#filterFim").value,
      };
      renderAll();
    });
  });

  $("#clearConteudoFilters").addEventListener("click", () => {
    $("#filterDominio").value = "";
    $("#filterInicio").value = "";
    $("#filterFim").value = "";
    state.conteudoFilters = { dominio: "", inicio: "", fim: "" };
    renderAll();
  });

  $("#perfilForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.submitter;
    try {
      await withButtonLoading(button, "Atualizando...", () => {
        return request("/users/me", {
          method: "PUT",
          body: JSON.stringify({ nome: $("#perfilNome").value }),
        });
      });
      await loadData();
      setMessage("#appMessage", "Perfil atualizado.", "success");
    } catch (error) {
      setMessage("#appMessage", error.message, "error");
    }
  });

  $("#senhaForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.submitter;
    try {
      await withButtonLoading(button, "Trocando...", () => {
        return request("/users/me/password", {
          method: "PUT",
          body: JSON.stringify({
            senhaAtual: $("#senhaAtual").value,
            novaSenha: $("#novaSenha").value,
          }),
        });
      });
      event.target.reset();
      setMessage("#appMessage", "Senha atualizada.", "success");
    } catch (error) {
      setMessage("#appMessage", error.message, "error");
    }
  });

  $("#closeDominioModal").addEventListener("click", closeDominioModal);
  $("#cancelDominioModal").addEventListener("click", closeDominioModal);
  $("#dominioModal").addEventListener("click", (event) => {
    if (event.target.id === "dominioModal") closeDominioModal();
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !$("#dominioModal").classList.contains("hidden")) {
      closeDominioModal();
    }
  });

  $("#dominioForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.submitter;
    const conteudoId = $("#dominioConteudoId").value || state.dominioModalConteudoId;
    const nivelDominio = Number($("#dominioNivel").value);

    if (!Number.isInteger(nivelDominio) || nivelDominio < 1 || nivelDominio > 5) {
      setMessage("#dominioMessage", "Nível de domínio deve estar entre 1 e 5.", "error");
      return;
    }

    try {
      setMessage("#dominioMessage", "");
      await withButtonLoading(button, "Salvando...", () => {
        return request(`/conteudos/${conteudoId}/evolucao`, {
          method: "PATCH",
          body: JSON.stringify({
            nivelDominio,
            observacaoEvolucao: $("#dominioObservacao").value,
          }),
        });
      });
      closeDominioModal();
      await loadData();
      setMessage("#appMessage", "Domínio atualizado.", "success");
    } catch (error) {
      setMessage("#dominioMessage", error.message, "error");
    }
  });

  document.body.addEventListener("click", async (event) => {
    const editId = event.target.dataset.editMateria;
    const deleteId = event.target.dataset.deleteMateria;
    const editConteudoId = event.target.dataset.editConteudo;
    const deleteConteudoId = event.target.dataset.deleteConteudo;
    const revisarId = event.target.dataset.revisar;
    const concluirId = event.target.dataset.concluir;
    const reativarId = event.target.dataset.reativar;
    const evolucaoId = event.target.dataset.evolucao;
    const emptyView = event.target.dataset.emptyView;

    if (emptyView) {
      activateView(emptyView);
      return;
    }

    try {
      if (editId) {
        const materia = state.materias.find((item) => String(item.id) === String(editId));
        if (!materia) return;
        $("#materiaId").value = materia.id;
        $("#materiaNome").value = materia.nome;
        $("#materiaDescricao").value = materia.descricao || "";
        $("#materiaCor").value = materia.cor || "#3b82f6";
        $("#cancelMateriaEdit").classList.remove("hidden");
        activateView("materias");
      }

      if (deleteId) {
        const shouldDelete = window.confirm("Excluir esta matéria? Os conteúdos vinculados também podem ser afetados.");
        if (!shouldDelete) return;
        await withButtonLoading(event.target, "Excluindo...", () => request(`/materias/${deleteId}`, { method: "DELETE" }));
        await loadData();
        setMessage("#appMessage", "Matéria excluída.", "success");
      }

      if (editConteudoId) {
        const conteudo = state.conteudos.find((item) => String(item.id) === String(editConteudoId));
        if (!conteudo) return;
        $("#conteudoId").value = conteudo.id;
        $("#conteudoMateria").value = conteudo.materiaId;
        $("#conteudoTitulo").value = conteudo.titulo;
        $("#conteudoDescricao").value = conteudo.descricao || "";
        $("#conteudoData").value = conteudo.dataEstudo || "";
        $("#saveConteudoButton").textContent = "Salvar conteúdo";
        $("#cancelConteudoEdit").classList.remove("hidden");
        activateView("conteudos");
      }

      if (deleteConteudoId) {
        const shouldDelete = window.confirm("Excluir este conteúdo?");
        if (!shouldDelete) return;
        await withButtonLoading(event.target, "Excluindo...", () => request(`/conteudos/${deleteConteudoId}`, { method: "DELETE" }));
        await loadData();
        setMessage("#appMessage", "Conteúdo excluído.", "success");
      }

      if (revisarId) {
        await withButtonLoading(event.target, "Revisando...", () => request(`/conteudos/${revisarId}/revisar`, { method: "POST" }));
        await loadData();
        setMessage("#appMessage", "Revisão registrada.", "success");
      }

      if (concluirId) {
        await withButtonLoading(event.target, "Concluindo...", () => request(`/conteudos/${concluirId}/concluir`, { method: "PATCH" }));
        await loadData();
        setMessage("#appMessage", "Conteúdo concluído.", "success");
      }

      if (reativarId) {
        await withButtonLoading(event.target, "Reativando...", () => request(`/conteudos/${reativarId}/reativar`, { method: "PATCH" }));
        await loadData();
        setMessage("#appMessage", "Conteúdo reativado.", "success");
      }

      if (evolucaoId) {
        openDominioModal(evolucaoId);
      }

      const calendarDate = event.target.closest("[data-calendar-date]")?.dataset.calendarDate;
      if (calendarDate) {
        state.selectedCalendarDate = calendarDate;
        renderRevisionCalendar();
      }
    } catch (error) {
      setMessage("#appMessage", error.message, "error");
    }
  });
}

bindAuth();
bindApp();
applyTheme();
applyDateLimits();
bootstrap();
