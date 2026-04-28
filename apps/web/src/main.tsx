import React, { useEffect, useMemo, useRef, useState, useTransition } from "react";
import ReactDOM from "react-dom/client";
import "./styles.css";

type Severity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
type TabKey =
  | "overview"
  | "events"
  | "alerts"
  | "incidents"
  | "actors"
  | "anomalies"
  | "rules"
  | "sources"
  | "chat";

type ApiResponse<T> = { status: string; data: T };

type AlertRecord = {
  alertId: string;
  title: string;
  severity: Severity;
  reason: string;
  source: string;
  status: string;
  eventId: string | null;
  triggeredRule: string;
  supportingEvidence: string | null;
  anomalyContribution: number | null;
  relatedIncidents: string | null;
  recommendation: string | null;
  acknowledgmentState: string;
  resolutionState: string;
  createdAt: string;
};

type EventRecord = {
  eventId: string;
  eventType: string;
  sourceType: string;
  occurredAt: string;
  severity: Severity;
  sourceIp: string | null;
  destinationIp: string | null;
  destinationPort: number | null;
  username: string | null;
  serviceName: string | null;
  endpoint: string | null;
  rawMessage: string;
  normalizedMessage: string;
  tags: string;
  correlationKey: string | null;
  createdAt: string;
};

type IncidentRecord = {
  incidentId: string;
  title: string;
  severity: Severity;
  summary: string;
  actorId: string | null;
  incidentScore: number;
  status: string;
  createdAt: string;
};

type ActorRecord = {
  actorId: string;
  actorKey: string;
  actorType: string;
  displayName: string;
  riskScore: number;
  lastSeenAt: string;
  createdAt: string;
};

type AnomalyRecord = {
  anomalyId: string;
  eventId: string;
  actorId: string | null;
  incidentId: string | null;
  anomalyScore: number;
  modelName: string;
  featureSummary: string;
  createdAt: string;
};

type RuleRecord = {
  ruleId: string;
  name: string;
  source: string;
  conditionExpression: string;
  thresholdValue: number | null;
  severity: Severity;
  timeWindowMinutes: number | null;
  explanationTemplate: string;
  enabled: boolean;
  createdAt: string;
};

type SourceRecord = {
  sourceId: string;
  name: string;
  sourceType: string;
  ingestionMethod: string;
  description: string;
  enabled: boolean;
  createdAt: string;
};

type InvestigationResponse = {
  answer: string;
  mode: string;
  promptSummary: string;
  sessionId: string;
  intent: string;
  recommendations: string[];
  engine: string;
};

type HostRiskRecord = {
  hostRiskId: string;
  hostIdentifier: string;
  riskScore: number;
  reason: string;
  lastSeenAt: string;
  createdAt: string;
};

type UserRiskRecord = {
  userRiskId: string;
  username: string;
  riskScore: number;
  reason: string;
  lastSeenAt: string;
  createdAt: string;
};

type RiskScoreSummary = {
  hosts: HostRiskRecord[];
  users: UserRiskRecord[];
};

type ChatEntry = {
  id: string;
  role: "user" | "assistant";
  message: string;
  meta?: string;
  recommendations?: string[];
};

type AuthState = {
  username: string;
  password: string;
};

type DataState = {
  events: EventRecord[];
  alerts: AlertRecord[];
  incidents: IncidentRecord[];
  actors: ActorRecord[];
  anomalies: AnomalyRecord[];
  rules: RuleRecord[];
  sources: SourceRecord[];
  riskSummary: RiskScoreSummary;
};

type RuleDraft = {
  name: string;
  source: string;
  conditionExpression: string;
  thresholdValue: string;
  severity: Severity;
  timeWindowMinutes: string;
  explanationTemplate: string;
  enabled: boolean;
};

type SourceDraft = {
  name: string;
  sourceType: string;
  ingestionMethod: string;
  description: string;
  enabled: boolean;
};

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "";
const AUTH_STORAGE_KEY = "caisip-auth";
const initialChatEntries: ChatEntry[] = [
  {
    id: "assistant-welcome",
    role: "assistant",
    message: "Investigation copilot is ready. Ask why an alert fired, who is risky, or what to check next.",
    meta: "Session not started yet"
  }
];
const defaultQueuePayload = JSON.stringify(
  [
    {
      sourceType: "AUTH_LOG",
      occurredAt: "2026-04-24T07:20:00Z",
      sourceIp: "185.24.91.18",
      destinationIp: "10.0.0.10",
      destinationPort: 443,
      username: "admin",
      serviceName: "auth-service",
      endpoint: "/admin/login",
      rawMessage: "multiple failed login attempts detected for admin"
    }
  ],
  null,
  2
);

const tabs: Array<{ key: TabKey; label: string; eyebrow: string }> = [
  { key: "overview", label: "Overview", eyebrow: "Live posture" },
  { key: "events", label: "Event Stream", eyebrow: "Normalized live event feed" },
  { key: "alerts", label: "Alerts", eyebrow: "Rule-backed detections" },
  { key: "incidents", label: "Incidents", eyebrow: "Correlated attack chains" },
  { key: "actors", label: "Actors", eyebrow: "Tracked risky entities" },
  { key: "anomalies", label: "Anomalies", eyebrow: "Model-scored outliers" },
  { key: "rules", label: "Rules", eyebrow: "Detection control plane" },
  { key: "sources", label: "Sources", eyebrow: "Ingestion catalog" },
  { key: "chat", label: "AI Chat", eyebrow: "Investigation copilot" }
];

function encodeAuth(auth: AuthState): string {
  return `Basic ${btoa(`${auth.username}:${auth.password}`)}`;
}

async function fetchJson<T>(path: string, auth: AuthState): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      Authorization: encodeAuth(auth)
    }
  });

  if (!response.ok) {
    throw new Error(`${path} failed with ${response.status}`);
  }

  const payload = (await response.json()) as ApiResponse<T>;
  return payload.data;
}

async function sendJson<T>(path: string, method: "POST" | "PUT", body: unknown, auth: AuthState): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      Authorization: encodeAuth(auth),
      "Content-Type": "application/json"
    },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    throw new Error(`${path} failed with ${response.status}`);
  }

  const payload = (await response.json()) as ApiResponse<T>;
  return payload.data;
}

async function uploadScenario(file: File, auth: AuthState): Promise<void> {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`${API_BASE}/api/ingestion/events/upload`, {
    method: "POST",
    headers: {
      Authorization: encodeAuth(auth)
    },
    body: formData
  });

  if (!response.ok) {
    throw new Error(`Scenario upload failed with ${response.status}`);
  }
}

function formatRelativeDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

function severityTone(severity: Severity): string {
  switch (severity) {
    case "CRITICAL":
      return "critical";
    case "HIGH":
      return "high";
    case "MEDIUM":
      return "medium";
    default:
      return "low";
  }
}

function useStoredAuth(): [AuthState | null, (next: AuthState | null) => void] {
  const [auth, setAuthState] = useState<AuthState | null>(() => {
    const stored = window.localStorage.getItem(AUTH_STORAGE_KEY);
    return stored ? (JSON.parse(stored) as AuthState) : null;
  });

  function setAuth(next: AuthState | null) {
    setAuthState(next);
    if (next) {
      window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(next));
    } else {
      window.localStorage.removeItem(AUTH_STORAGE_KEY);
    }
  }

  return [auth, setAuth];
}

function App() {
  const [auth, setAuth] = useStoredAuth();
  const [activeTab, setActiveTab] = useState<TabKey>("overview");
  const [data, setData] = useState<DataState>({
    alerts: [],
    events: [],
    incidents: [],
    actors: [],
    anomalies: [],
    rules: [],
    sources: [],
    riskSummary: { hosts: [], users: [] }
  });
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [workspaceNote, setWorkspaceNote] = useState<string | null>(null);
  const [chatEntries, setChatEntries] = useState<ChatEntry[]>(initialChatEntries);
  const [message, setMessage] = useState("");
  const [mode, setMode] = useState<"technical" | "executive">("technical");
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [queuePayload, setQueuePayload] = useState(defaultQueuePayload);
  const [queueBusy, setQueueBusy] = useState(false);
  const [uploadBusy, setUploadBusy] = useState(false);
  const [ruleDraft, setRuleDraft] = useState<RuleDraft>({
    name: "CUSTOM_REVIEW_RULE",
    source: "manual",
    conditionExpression: "review suspicious admin behavior",
    thresholdValue: "10",
    severity: "MEDIUM",
    timeWindowMinutes: "15",
    explanationTemplate: "Manual analyst-authored review rule triggered.",
    enabled: true
  });
  const [sourceDraft, setSourceDraft] = useState<SourceDraft>({
    name: "Demo Uploaded Source",
    sourceType: "APP_LOG",
    ingestionMethod: "file-upload",
    description: "Analyst-created source definition for replayed demo data.",
    enabled: true
  });
  const [isPending, startTransition] = useTransition();

  const isAdmin = auth?.username === "admin";
  const roleName = isAdmin ? "Administrator" : "Analyst";
  const roleSummary = isAdmin
    ? "Full control: investigate threats, replay data, and manage rules and sources."
    : "Investigation access: dashboards, evidence, AI chat, and read-only control plane.";
  const roleCapability = isAdmin ? "Write controls enabled" : "Write controls locked";

  function resetInvestigationChat() {
    setChatEntries(initialChatEntries);
    setSessionId(null);
    setMessage("");
  }

  async function loadWorkspace(currentAuth: AuthState) {
    setLoading(true);
    try {
      const [events, alerts, incidents, actors, anomalies, rules, sources, riskSummary] = await Promise.all([
        fetchJson<EventRecord[]>("/api/events", currentAuth),
        fetchJson<AlertRecord[]>("/api/alerts", currentAuth),
        fetchJson<IncidentRecord[]>("/api/incidents", currentAuth),
        fetchJson<ActorRecord[]>("/api/actors", currentAuth),
        fetchJson<AnomalyRecord[]>("/api/anomalies", currentAuth),
        fetchJson<RuleRecord[]>("/api/rules", currentAuth),
        fetchJson<SourceRecord[]>("/api/sources", currentAuth),
        fetchJson<RiskScoreSummary>("/api/risk-scores", currentAuth)
      ]);

      setData({ events, alerts, incidents, actors, anomalies, rules, sources, riskSummary });
      setLoadError(null);
    } catch (error) {
      setLoadError(error instanceof Error ? error.message : "Workspace loading failed");
      throw error;
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!auth) {
      return;
    }

    void loadWorkspace(auth);
  }, [auth]);

  const overview = useMemo(() => {
    const topActor = [...data.actors].sort((left, right) => right.riskScore - left.riskScore)[0];
    const topIncident = [...data.incidents].sort((left, right) => right.incidentScore - left.incidentScore)[0];
    const topAnomaly = [...data.anomalies].sort((left, right) => right.anomalyScore - left.anomalyScore)[0];

    return {
      alertCount: data.alerts.length,
      openIncidentCount: data.incidents.filter((incident) => incident.status === "OPEN").length,
      ruleCount: data.rules.length,
      sourceCount: data.sources.length,
      topHost: [...data.riskSummary.hosts].sort((left, right) => right.riskScore - left.riskScore)[0],
      topUser: [...data.riskSummary.users].sort((left, right) => right.riskScore - left.riskScore)[0],
      topActor,
      topIncident,
      topAnomaly
    };
  }, [data]);

  function sendPrompt(prompt: string, nextTab: TabKey = "chat") {
    setActiveTab(nextTab);
    setMessage(prompt);
  }

  async function submitLogin(nextAuth: AuthState) {
    await loadWorkspace(nextAuth);
    resetInvestigationChat();
    setAuth(nextAuth);
    setWorkspaceNote(`Signed in as ${nextAuth.username}.`);
  }

  async function refreshWorkspace() {
    if (!auth) {
      return;
    }
    await loadWorkspace(auth);
    setWorkspaceNote("Workspace refreshed.");
  }

  async function submitQueueIngestion() {
    if (!auth) {
      return;
    }

    setQueueBusy(true);
    try {
      const parsed = JSON.parse(queuePayload);
      await sendJson("/api/ingestion/events/queue", "POST", parsed, auth);
      await loadWorkspace(auth);
      setWorkspaceNote("Queued events were ingested and the dashboard was refreshed.");
    } catch (error) {
      setWorkspaceNote(error instanceof Error ? error.message : "Queue ingestion failed.");
    } finally {
      setQueueBusy(false);
    }
  }

  async function handleScenarioUpload(file: File | null) {
    if (!file || !auth) {
      return;
    }

    setUploadBusy(true);
    try {
      await uploadScenario(file, auth);
      await loadWorkspace(auth);
      setWorkspaceNote(`Uploaded ${file.name} and replayed its events.`);
    } catch (error) {
      setWorkspaceNote(error instanceof Error ? error.message : "Scenario upload failed.");
    } finally {
      setUploadBusy(false);
    }
  }

  async function createRule() {
    if (!auth || !isAdmin) {
      return;
    }

    await sendJson<RuleRecord>("/api/rules", "POST", {
      ...ruleDraft,
      thresholdValue: ruleDraft.thresholdValue ? Number(ruleDraft.thresholdValue) : null,
      timeWindowMinutes: ruleDraft.timeWindowMinutes ? Number(ruleDraft.timeWindowMinutes) : null
    }, auth);
    await loadWorkspace(auth);
    setWorkspaceNote(`Rule ${ruleDraft.name} created.`);
  }

  async function toggleRule(rule: RuleRecord) {
    if (!auth || !isAdmin) {
      return;
    }

    await sendJson<RuleRecord>(`/api/rules/${rule.ruleId}`, "PUT", {
      name: rule.name,
      source: rule.source,
      conditionExpression: rule.conditionExpression,
      thresholdValue: rule.thresholdValue,
      severity: rule.severity,
      timeWindowMinutes: rule.timeWindowMinutes,
      explanationTemplate: rule.explanationTemplate,
      enabled: !rule.enabled
    }, auth);
    await loadWorkspace(auth);
    setWorkspaceNote(`Rule ${rule.name} ${rule.enabled ? "disabled" : "enabled"}.`);
  }

  async function createSource() {
    if (!auth || !isAdmin) {
      return;
    }

    await sendJson<SourceRecord>("/api/sources", "POST", sourceDraft, auth);
    await loadWorkspace(auth);
    setWorkspaceNote(`Source ${sourceDraft.name} created.`);
  }

  async function toggleSource(source: SourceRecord) {
    if (!auth || !isAdmin) {
      return;
    }

    await sendJson<SourceRecord>(`/api/sources/${source.sourceId}`, "PUT", {
      name: source.name,
      sourceType: source.sourceType,
      ingestionMethod: source.ingestionMethod,
      description: source.description,
      enabled: !source.enabled
    }, auth);
    await loadWorkspace(auth);
    setWorkspaceNote(`Source ${source.name} ${source.enabled ? "disabled" : "enabled"}.`);
  }

  function submitChat(customMessage?: string) {
    if (!auth) {
      return;
    }

    const finalMessage = (customMessage ?? message).trim();
    if (!finalMessage) {
      return;
    }

    const userEntry: ChatEntry = {
      id: `user-${Date.now()}`,
      role: "user",
      message: finalMessage,
      meta: mode === "technical" ? "Technical mode" : "Plain mode"
    };

    setChatEntries((current) => [...current, userEntry]);
    setMessage("");

    startTransition(() => {
      void (async () => {
        try {
          const response = await fetch(`${API_BASE}/api/investigations/messages`, {
            method: "POST",
            headers: {
              Authorization: encodeAuth(auth),
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              message: finalMessage,
              sessionId,
              mode
            })
          });

          if (!response.ok) {
            throw new Error(`Investigation request failed with ${response.status}`);
          }

          const payload = (await response.json()) as ApiResponse<InvestigationResponse>;
          const result = payload.data;
          setSessionId(result.sessionId);
          setChatEntries((current) => [
            ...current,
            {
              id: `assistant-${Date.now()}`,
              role: "assistant",
              message: result.answer,
              meta: `${result.engine} • ${result.intent} • ${result.mode} • session ${result.sessionId}`,
              recommendations: result.recommendations
            }
          ]);
        } catch (error) {
          setChatEntries((current) => [
            ...current,
            {
              id: `assistant-error-${Date.now()}`,
              role: "assistant",
              message: error instanceof Error ? error.message : "Investigation request failed.",
              meta: "Request error"
            }
          ]);
        }
      })();
    });
  }

  if (!auth) {
    return <LoginScreen onSubmit={submitLogin} />;
  }

  const currentTab = tabs.find((tab) => tab.key === activeTab) ?? tabs[0];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <p className="brand-kicker">CAISIP</p>
          <h1>Security Investigation Workspace</h1>
          <p className="brand-copy">
            AI-first monitoring, explanation, correlation, and analyst-guided investigation in one place.
          </p>
        </div>

        <section className="sidebar-panel">
          <span className="sidebar-label">Session</span>
          <div className="role-card">
            <div>
              <p className="sidebar-meta">Signed in as <strong>{auth.username}</strong></p>
              <strong>{roleName}</strong>
            </div>
            <span className={isAdmin ? "role-badge admin" : "role-badge analyst"}>{roleCapability}</span>
          </div>
          <p className="sidebar-meta">{roleSummary}</p>
          <div className="hero-actions">
            <button type="button" className="action-button primary" onClick={() => void refreshWorkspace()}>
              Refresh
            </button>
            <button
              type="button"
              className="action-button"
              onClick={() => {
                resetInvestigationChat();
                setAuth(null);
                setLoadError(null);
                setWorkspaceNote("Signed out.");
              }}
            >
              Sign Out
            </button>
          </div>
        </section>

        <nav className="sidebar-nav">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              className={tab.key === activeTab ? "nav-pill active" : "nav-pill"}
              onClick={() => setActiveTab(tab.key)}
              type="button"
            >
              <span>{tab.label}</span>
              <small>{tab.eyebrow}</small>
            </button>
          ))}
        </nav>

        <section className="sidebar-panel">
          <span className="sidebar-label">AI Mode</span>
          <div className="mode-toggle">
            <button
              type="button"
              className={mode === "technical" ? "mode-button active" : "mode-button"}
              onClick={() => setMode("technical")}
            >
              Technical
            </button>
            <button
              type="button"
              className={mode === "executive" ? "mode-button active" : "mode-button"}
              onClick={() => setMode("executive")}
            >
              Plain
            </button>
          </div>
          <p className="sidebar-meta">
            {sessionId ? `Active session: ${sessionId.slice(0, 8)}...` : "Start a chat to create a session."}
          </p>
        </section>
      </aside>

      <main className="content">
        <header className="page-header">
          <div>
            <p className="eyebrow">{currentTab.eyebrow}</p>
            <h2>{currentTab.label}</h2>
          </div>
          <div className="status-group">
            {workspaceNote ? <p className="status-badge">{workspaceNote}</p> : null}
            {loadError ? <p className="status-badge error">{loadError}</p> : null}
            {loading ? <p className="status-badge">Refreshing live security state...</p> : null}
          </div>
        </header>

        {activeTab === "overview" ? (
          <OverviewPanel
            overview={overview}
            events={data.events}
            alerts={data.alerts}
            incidents={data.incidents}
            actors={data.actors}
            rules={data.rules}
            sources={data.sources}
            riskSummary={data.riskSummary}
            onPrompt={sendPrompt}
            queuePayload={queuePayload}
            queueBusy={queueBusy}
            uploadBusy={uploadBusy}
            onQueuePayloadChange={setQueuePayload}
            onQueueSubmit={() => void submitQueueIngestion()}
            onFilePicked={(file) => void handleScenarioUpload(file)}
          />
        ) : null}

        {activeTab === "events" ? <EventStreamPanel events={data.events} onPrompt={sendPrompt} /> : null}
        {activeTab === "alerts" ? <AlertsPanel alerts={data.alerts} onPrompt={sendPrompt} /> : null}
        {activeTab === "incidents" ? <IncidentsPanel incidents={data.incidents} onPrompt={sendPrompt} /> : null}
        {activeTab === "actors" ? <ActorsPanel actors={data.actors} onPrompt={sendPrompt} /> : null}
        {activeTab === "anomalies" ? <AnomaliesPanel anomalies={data.anomalies} /> : null}
        {activeTab === "rules" ? (
          <RulesPanel
            rules={data.rules}
            draft={ruleDraft}
            isAdmin={isAdmin}
            onDraftChange={setRuleDraft}
            onCreate={() => void createRule()}
            onToggle={(rule) => void toggleRule(rule)}
          />
        ) : null}
        {activeTab === "sources" ? (
          <SourcesPanel
            sources={data.sources}
            draft={sourceDraft}
            isAdmin={isAdmin}
            onDraftChange={setSourceDraft}
            onCreate={() => void createSource()}
            onToggle={(source) => void toggleSource(source)}
          />
        ) : null}
        {activeTab === "chat" ? (
          <ChatPanel
            chatEntries={chatEntries}
            isPending={isPending}
            message={message}
            mode={mode}
            onMessageChange={setMessage}
            onSubmit={() => submitChat()}
            onSuggestion={submitChat}
          />
        ) : null}
      </main>
    </div>
  );
}

function LoginScreen({ onSubmit }: { onSubmit: (auth: AuthState) => Promise<void> }) {
  const [auth, setAuth] = useState<AuthState>({ username: "", password: "" });
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleSubmit() {
    setBusy(true);
    setError(null);
    try {
      await onSubmit(auth);
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : "Login failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-shell">
      <section className="panel login-panel">
        <p className="brand-kicker">CAISIP</p>
        <h1>Conversational AI Security Investigation Platform</h1>
        <p className="brand-copy">
          Sign in with your platform account to open the security investigation workspace.
        </p>

        <div className="form-grid login-form-grid">
          <label className="field-label">
            Username
            <input
              className="text-input"
              autoComplete="username"
              value={auth.username}
              onChange={(event) => setAuth((current) => ({ ...current, username: event.target.value }))}
            />
          </label>
          <label className="field-label">
            Password
            <input
              className="text-input"
              type="password"
              autoComplete="current-password"
              value={auth.password}
              onChange={(event) => setAuth((current) => ({ ...current, password: event.target.value }))}
            />
          </label>
        </div>

        <div className="hero-actions login-actions">
          <button type="button" className="action-button primary" onClick={() => void handleSubmit()} disabled={busy || !auth.username || !auth.password}>
            {busy ? "Signing In..." : "Sign In"}
          </button>
        </div>
        {error ? <p className="status-badge error">{error}</p> : null}
      </section>
    </div>
  );
}

function OverviewPanel({
  overview,
  events,
  alerts,
  incidents,
  actors,
  rules,
  sources,
  riskSummary,
  onPrompt,
  queuePayload,
  queueBusy,
  uploadBusy,
  onQueuePayloadChange,
  onQueueSubmit,
  onFilePicked
}: {
  overview: {
    alertCount: number;
    openIncidentCount: number;
    ruleCount: number;
    sourceCount: number;
    topHost?: HostRiskRecord;
    topUser?: UserRiskRecord;
    topActor?: ActorRecord;
    topIncident?: IncidentRecord;
    topAnomaly?: AnomalyRecord;
  };
  events: EventRecord[];
  alerts: AlertRecord[];
  incidents: IncidentRecord[];
  actors: ActorRecord[];
  rules: RuleRecord[];
  sources: SourceRecord[];
  riskSummary: RiskScoreSummary;
  onPrompt: (prompt: string) => void;
  queuePayload: string;
  queueBusy: boolean;
  uploadBusy: boolean;
  onQueuePayloadChange: (value: string) => void;
  onQueueSubmit: () => void;
  onFilePicked: (file: File | null) => void;
}) {
  return (
    <>
      <section className="hero panel">
        <p className="hero-lead">
          This workspace keeps the investigation centered on what matters now: risky actors, correlated incidents,
          explainable alerts, managed detection rules, known sources, and AI-guided next steps.
        </p>
        <div className="hero-actions">
          <button type="button" className="action-button primary" onClick={() => onPrompt("Which IP is the riskiest right now?")}>
            Ask For Top Actor
          </button>
          <button type="button" className="action-button" onClick={() => onPrompt("Why does the latest incident matter? Keep it brief.")}>
            Summarize Latest Incident
          </button>
        </div>
      </section>

      <section className="metric-grid">
        <article className="metric-card ember">
          <span>Active Alerts</span>
          <strong>{overview.alertCount}</strong>
          <small>{alerts[0]?.title ?? "No alerts yet"}</small>
        </article>
        <article className="metric-card frost">
          <span>Open Incidents</span>
          <strong>{overview.openIncidentCount}</strong>
          <small>{overview.topIncident?.title ?? "No incidents yet"}</small>
        </article>
        <article className="metric-card moss">
          <span>Top Risky Actor</span>
          <strong>{overview.topActor?.displayName ?? "N/A"}</strong>
          <small>Risk {overview.topActor?.riskScore ?? 0}</small>
        </article>
        <article className="metric-card dusk">
          <span>Control Plane</span>
          <strong>{overview.ruleCount + overview.sourceCount}</strong>
          <small>{rules.length} rules • {sources.length} sources</small>
        </article>
      </section>

      <section className="split-layout">
        <article className="panel">
          <div className="panel-head">
            <h3>Host Risk</h3>
            <span>{riskSummary.hosts.length} hosts scored</span>
          </div>
          <div className="list-stack">
            {riskSummary.hosts.slice(0, 3).map((host) => (
              <button key={host.hostRiskId} type="button" className="list-row" onClick={() => onPrompt(`Why is host ${host.hostIdentifier} risky?`)}>
                <div>
                  <strong>{host.hostIdentifier}</strong>
                  <small>{host.reason}</small>
                </div>
                <span className="score-pill">{host.riskScore}</span>
              </button>
            ))}
          </div>
        </article>

        <article className="panel">
          <div className="panel-head">
            <h3>User Risk</h3>
            <span>{riskSummary.users.length} users scored</span>
          </div>
          <div className="list-stack">
            {riskSummary.users.slice(0, 3).map((user) => (
              <button key={user.userRiskId} type="button" className="list-row" onClick={() => onPrompt(`Why is user ${user.username} risky?`)}>
                <div>
                  <strong>{user.username}</strong>
                  <small>{user.reason}</small>
                </div>
                <span className="score-pill">{user.riskScore}</span>
              </button>
            ))}
          </div>
        </article>
      </section>

      <section className="split-layout">
        <article className="panel">
          <div className="panel-head">
            <h3>Priority Queue</h3>
            <span>{incidents.length} incidents</span>
          </div>
          <div className="timeline">
            {incidents.slice(0, 4).map((incident) => (
              <button
                key={incident.incidentId}
                type="button"
                className="timeline-item"
                onClick={() => onPrompt(`Why does incident ${incident.incidentId} matter?`)}
              >
                <div>
                  <span className={`severity-chip ${severityTone(incident.severity)}`}>{incident.severity}</span>
                  <strong>{incident.title}</strong>
                </div>
                <small>Score {incident.incidentScore}</small>
              </button>
            ))}
          </div>
        </article>

        <article className="panel">
          <div className="panel-head">
            <h3>Hot Actors</h3>
            <span>{actors.length} tracked</span>
          </div>
          <div className="list-stack">
            {actors.slice(0, 4).map((actor) => (
              <button
                key={actor.actorId}
                type="button"
                className="list-row"
                onClick={() => onPrompt(`Why is ${actor.displayName} risky?`)}
              >
                <div>
                  <strong>{actor.displayName}</strong>
                  <small>{actor.actorType}</small>
                </div>
                <span className="score-pill">{actor.riskScore}</span>
              </button>
            ))}
          </div>
        </article>
      </section>

      <section className="split-layout">
        <article className="panel">
          <div className="panel-head">
            <h3>Queue Replay</h3>
            <span>Paste event array JSON</span>
          </div>
          <textarea className="chat-input compact" value={queuePayload} onChange={(event) => onQueuePayloadChange(event.target.value)} />
          <div className="composer-actions">
            <button type="button" className="action-button primary" onClick={onQueueSubmit} disabled={queueBusy}>
              {queueBusy ? "Replaying..." : "Replay Queue Batch"}
            </button>
          </div>
        </article>

        <article className="panel">
          <div className="panel-head">
            <h3>Scenario Upload</h3>
            <span>Replay bundled or custom JSON scenario files</span>
          </div>
          <label className="field-label">
            Upload scenario JSON
            <input
              className="text-input"
              type="file"
              accept="application/json"
              onChange={(event) => onFilePicked(event.target.files?.[0] ?? null)}
              disabled={uploadBusy}
            />
          </label>
          <p className="sidebar-meta">Use the bundled files in `datasets/demo-scenarios` or upload your own event batch file.</p>
        </article>
      </section>

      <section className="panel">
        <div className="panel-head">
          <h3>Latest Event Stream</h3>
          <span>{events.length} normalized events</span>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Type</th>
                <th>Source</th>
                <th>Actor</th>
                <th>Target</th>
                <th>Tags</th>
              </tr>
            </thead>
            <tbody>
              {events.slice(0, 5).map((event) => (
                <tr key={event.eventId}>
                  <td>{event.eventType}</td>
                  <td>{event.sourceType}</td>
                  <td>{event.username ?? event.sourceIp ?? "N/A"}</td>
                  <td>{event.destinationIp ?? event.endpoint ?? "N/A"}</td>
                  <td>{event.tags}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

function EventStreamPanel({ events, onPrompt }: { events: EventRecord[]; onPrompt: (prompt: string) => void }) {
  return (
    <section className="panel">
      <div className="panel-head">
        <h3>Event Stream</h3>
        <span>{events.length} normalized security events</span>
      </div>
      <div className="card-list">
        {events.map((event) => (
          <article className="record-card" key={event.eventId}>
            <div className="record-head">
              <div>
                <span className={`severity-chip ${severityTone(event.severity)}`}>{event.severity}</span>
                <h4>{event.eventType}</h4>
              </div>
              <small>{formatRelativeDate(event.occurredAt)}</small>
            </div>
            <p>{event.normalizedMessage}</p>
            <div className="record-meta">
              <span>{event.sourceType}</span>
              <span>{event.username ?? event.sourceIp ?? "N/A"}</span>
              <span>{event.destinationIp ?? event.endpoint ?? "N/A"}</span>
              <span>{event.tags}</span>
            </div>
            <div className="record-actions">
              <button type="button" className="action-button primary" onClick={() => onPrompt(`Summarize event ${event.eventId} technically.`)}>
                Explain Event
              </button>
              <button type="button" className="action-button" onClick={() => onPrompt(`Is ${event.username ?? event.sourceIp ?? event.destinationIp ?? event.eventId} connected to earlier activity?`)}>
                Trace Context
              </button>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

function AlertsPanel({ alerts, onPrompt }: { alerts: AlertRecord[]; onPrompt: (prompt: string) => void }) {
  return (
    <section className="panel">
      <div className="panel-head">
        <h3>Alerts</h3>
        <span>{alerts.length} open investigation entries</span>
      </div>
      <div className="card-list">
        {alerts.map((alert) => (
          <article className="record-card" key={alert.alertId}>
            <div className="record-head">
              <div>
                <span className={`severity-chip ${severityTone(alert.severity)}`}>{alert.severity}</span>
                <h4>{alert.title}</h4>
              </div>
              <small>{formatRelativeDate(alert.createdAt)}</small>
            </div>
            <p>{alert.reason}</p>
            <div className="record-meta">
              <span>{alert.triggeredRule}</span>
              <span>{alert.source}</span>
              <span>{alert.status}</span>
              <span>{alert.acknowledgmentState}</span>
              <span>{alert.resolutionState}</span>
            </div>
            <p><strong>Evidence:</strong> {alert.supportingEvidence ?? "N/A"}</p>
            <p><strong>Anomaly:</strong> {alert.anomalyContribution?.toFixed(2) ?? "0.00"} • <strong>Incident:</strong> {alert.relatedIncidents ?? "N/A"}</p>
            <p><strong>Recommendation:</strong> {alert.recommendation ?? "N/A"}</p>
            <div className="record-actions">
              <button type="button" className="action-button primary" onClick={() => onPrompt(`Why did alert ${alert.alertId} trigger?`)}>
                Explain Alert
              </button>
              <button type="button" className="action-button" onClick={() => onPrompt(`What should I check for alert ${alert.alertId}?`)}>
                Next Checks
              </button>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

function IncidentsPanel({ incidents, onPrompt }: { incidents: IncidentRecord[]; onPrompt: (prompt: string) => void }) {
  return (
    <section className="panel">
      <div className="panel-head">
        <h3>Incidents</h3>
        <span>{incidents.length} correlated chains</span>
      </div>
      <div className="card-list">
        {incidents.map((incident) => (
          <article className="record-card" key={incident.incidentId}>
            <div className="record-head">
              <div>
                <span className={`severity-chip ${severityTone(incident.severity)}`}>{incident.severity}</span>
                <h4>{incident.title}</h4>
              </div>
              <span className="score-pill">Score {incident.incidentScore}</span>
            </div>
            <p>{incident.summary}</p>
            <div className="record-meta">
              <span>{incident.status}</span>
              <span>{formatRelativeDate(incident.createdAt)}</span>
            </div>
            <div className="record-actions">
              <button type="button" className="action-button primary" onClick={() => onPrompt(`Summarize incident ${incident.incidentId}.`)}>
                Summarize
              </button>
              <button type="button" className="action-button" onClick={() => onPrompt(`Is incident ${incident.incidentId} connected to earlier activity?`)}>
                Trace Chain
              </button>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

function ActorsPanel({ actors, onPrompt }: { actors: ActorRecord[]; onPrompt: (prompt: string) => void }) {
  return (
    <section className="panel">
      <div className="panel-head">
        <h3>Actors</h3>
        <span>{actors.length} tracked entities</span>
      </div>
      <div className="card-list">
        {actors.map((actor) => (
          <article className="record-card" key={actor.actorId}>
            <div className="record-head">
              <div>
                <span className="type-chip">{actor.actorType}</span>
                <h4>{actor.displayName}</h4>
              </div>
              <span className="score-pill">Risk {actor.riskScore}</span>
            </div>
            <div className="record-meta">
              <span>{actor.actorKey}</span>
              <span>Last seen {formatRelativeDate(actor.lastSeenAt)}</span>
            </div>
            <div className="record-actions">
              <button type="button" className="action-button primary" onClick={() => onPrompt(`Why is ${actor.displayName} risky?`)}>
                Investigate Actor
              </button>
              <button type="button" className="action-button" onClick={() => onPrompt(`What should I check next for ${actor.displayName}?`)}>
                Ask Next Steps
              </button>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

function AnomaliesPanel({ anomalies }: { anomalies: AnomalyRecord[] }) {
  return (
    <section className="panel">
      <div className="panel-head">
        <h3>Anomaly Scores</h3>
        <span>{anomalies.length} model outputs</span>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Score</th>
              <th>Model</th>
              <th>Feature Summary</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            {anomalies.map((anomaly) => (
              <tr key={anomaly.anomalyId}>
                <td><span className="score-pill">{anomaly.anomalyScore.toFixed(2)}</span></td>
                <td>{anomaly.modelName}</td>
                <td>{anomaly.featureSummary}</td>
                <td>{formatRelativeDate(anomaly.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function RulesPanel({
  rules,
  draft,
  isAdmin,
  onDraftChange,
  onCreate,
  onToggle
}: {
  rules: RuleRecord[];
  draft: RuleDraft;
  isAdmin: boolean;
  onDraftChange: (draft: RuleDraft) => void;
  onCreate: () => void;
  onToggle: (rule: RuleRecord) => void;
}) {
  return (
    <section className="split-layout">
      <article className="panel">
        <div className="panel-head">
          <h3>Managed Rules</h3>
          <span>{rules.length} definitions</span>
        </div>
        <div className="card-list">
          {rules.map((rule) => (
            <article className="record-card" key={rule.ruleId}>
              <div className="record-head">
                <div>
                  <span className={`severity-chip ${severityTone(rule.severity)}`}>{rule.severity}</span>
                  <h4>{rule.name}</h4>
                </div>
                <span className="score-pill">{rule.enabled ? "ENABLED" : "DISABLED"}</span>
              </div>
              <p>{rule.conditionExpression}</p>
              <div className="record-meta">
                <span>{rule.source}</span>
                <span>{rule.timeWindowMinutes ?? 0} min</span>
                <span>{formatRelativeDate(rule.createdAt)}</span>
              </div>
              {isAdmin ? (
                <div className="record-actions">
                  <button type="button" className="action-button primary" onClick={() => onToggle(rule)}>
                    {rule.enabled ? "Disable" : "Enable"}
                  </button>
                </div>
              ) : null}
            </article>
          ))}
        </div>
      </article>

      <article className="panel">
        <div className="panel-head">
          <h3>Create Rule</h3>
          <span>{isAdmin ? "Admin write access" : "Read-only as analyst"}</span>
        </div>
        {!isAdmin ? <p className="permission-note">Analysts can inspect detection logic, but only administrators can create or toggle rules.</p> : null}
        <div className="form-grid">
          <label className="field-label">
            Name
            <input className="text-input" value={draft.name} onChange={(event) => onDraftChange({ ...draft, name: event.target.value })} />
          </label>
          <label className="field-label">
            Source
            <input className="text-input" value={draft.source} onChange={(event) => onDraftChange({ ...draft, source: event.target.value })} />
          </label>
          <label className="field-label">
            Severity
            <select className="text-input" value={draft.severity} onChange={(event) => onDraftChange({ ...draft, severity: event.target.value as Severity })}>
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
              <option value="CRITICAL">CRITICAL</option>
            </select>
          </label>
          <label className="field-label">
            Threshold
            <input className="text-input" value={draft.thresholdValue} onChange={(event) => onDraftChange({ ...draft, thresholdValue: event.target.value })} />
          </label>
          <label className="field-label">
            Time Window
            <input className="text-input" value={draft.timeWindowMinutes} onChange={(event) => onDraftChange({ ...draft, timeWindowMinutes: event.target.value })} />
          </label>
          <label className="field-label full-span">
            Condition
            <input className="text-input" value={draft.conditionExpression} onChange={(event) => onDraftChange({ ...draft, conditionExpression: event.target.value })} />
          </label>
          <label className="field-label full-span">
            Explanation
            <textarea className="chat-input compact" value={draft.explanationTemplate} onChange={(event) => onDraftChange({ ...draft, explanationTemplate: event.target.value })} />
          </label>
        </div>
        <div className="composer-actions">
          <button type="button" className="action-button primary" onClick={onCreate} disabled={!isAdmin}>
            Create Rule
          </button>
        </div>
      </article>
    </section>
  );
}

function SourcesPanel({
  sources,
  draft,
  isAdmin,
  onDraftChange,
  onCreate,
  onToggle
}: {
  sources: SourceRecord[];
  draft: SourceDraft;
  isAdmin: boolean;
  onDraftChange: (draft: SourceDraft) => void;
  onCreate: () => void;
  onToggle: (source: SourceRecord) => void;
}) {
  return (
    <section className="split-layout">
      <article className="panel">
        <div className="panel-head">
          <h3>Sources</h3>
          <span>{sources.length} registered feeds</span>
        </div>
        <div className="card-list">
          {sources.map((source) => (
            <article className="record-card" key={source.sourceId}>
              <div className="record-head">
                <div>
                  <span className="type-chip">{source.sourceType}</span>
                  <h4>{source.name}</h4>
                </div>
                <span className="score-pill">{source.enabled ? "ENABLED" : "DISABLED"}</span>
              </div>
              <p>{source.description}</p>
              <div className="record-meta">
                <span>{source.ingestionMethod}</span>
                <span>{formatRelativeDate(source.createdAt)}</span>
              </div>
              {isAdmin ? (
                <div className="record-actions">
                  <button type="button" className="action-button primary" onClick={() => onToggle(source)}>
                    {source.enabled ? "Disable" : "Enable"}
                  </button>
                </div>
              ) : null}
            </article>
          ))}
        </div>
      </article>

      <article className="panel">
        <div className="panel-head">
          <h3>Create Source</h3>
          <span>{isAdmin ? "Admin write access" : "Read-only as analyst"}</span>
        </div>
        {!isAdmin ? <p className="permission-note">Analysts can review registered feeds, but only administrators can add or disable sources.</p> : null}
        <div className="form-grid">
          <label className="field-label">
            Name
            <input className="text-input" value={draft.name} onChange={(event) => onDraftChange({ ...draft, name: event.target.value })} />
          </label>
          <label className="field-label">
            Source Type
            <select className="text-input" value={draft.sourceType} onChange={(event) => onDraftChange({ ...draft, sourceType: event.target.value })}>
              <option value="TRAFFIC">TRAFFIC</option>
              <option value="AUTH_LOG">AUTH_LOG</option>
              <option value="APP_LOG">APP_LOG</option>
              <option value="ACCESS_LOG">ACCESS_LOG</option>
              <option value="SYSTEM_EVENT">SYSTEM_EVENT</option>
              <option value="SIMULATED_ATTACK">SIMULATED_ATTACK</option>
            </select>
          </label>
          <label className="field-label">
            Ingestion Method
            <input className="text-input" value={draft.ingestionMethod} onChange={(event) => onDraftChange({ ...draft, ingestionMethod: event.target.value })} />
          </label>
          <label className="field-label full-span">
            Description
            <textarea className="chat-input compact" value={draft.description} onChange={(event) => onDraftChange({ ...draft, description: event.target.value })} />
          </label>
        </div>
        <div className="composer-actions">
          <button type="button" className="action-button primary" onClick={onCreate} disabled={!isAdmin}>
            Create Source
          </button>
        </div>
      </article>
    </section>
  );
}

function ChatPanel({
  chatEntries,
  isPending,
  message,
  mode,
  onMessageChange,
  onSubmit,
  onSuggestion
}: {
  chatEntries: ChatEntry[];
  isPending: boolean;
  message: string;
  mode: string;
  onMessageChange: (value: string) => void;
  onSubmit: () => void;
  onSuggestion: (prompt: string) => void;
}) {
  const streamRef = useRef<HTMLDivElement | null>(null);
  const suggestions = [
    "Which IP is the riskiest right now?",
    "Why does the latest incident matter?",
    "What should I check for this alert?",
    "Write a short non-technical summary."
  ];

  useEffect(() => {
    streamRef.current?.scrollTo({ top: streamRef.current.scrollHeight, behavior: "smooth" });
  }, [chatEntries, isPending]);

  return (
    <section className="chat-layout">
      <article className="panel chat-panel conversation-panel">
        <div className="conversation-head">
          <div>
            <p className="eyebrow">Investigation copilot</p>
            <h3>AI Investigation Chat</h3>
          </div>
          <span className="mode-badge">{mode === "technical" ? "Technical mode" : "Plain mode"}</span>
        </div>
        <div className="chat-stream conversation-stream" ref={streamRef}>
          {chatEntries.map((entry) => (
            <div key={entry.id} className={`message-row ${entry.role}`}>
              <div className="message-avatar">{entry.role === "assistant" ? "AI" : "AN"}</div>
              <div className="message-stack">
                <div className={entry.role === "assistant" ? "message-bubble assistant" : "message-bubble user"}>
                  <div className="message-author">{entry.role === "assistant" ? "Assistant" : "Analyst"}</div>
                  <p>{entry.message}</p>
                  {entry.meta ? <small>{entry.meta}</small> : null}
                </div>
                {entry.recommendations?.length ? (
                  <div className="recommendation-list">
                    {entry.recommendations.map((recommendation) => (
                      <button
                        key={recommendation}
                        type="button"
                        className="recommendation-chip"
                        onClick={() => onSuggestion(recommendation)}
                      >
                        {recommendation}
                      </button>
                    ))}
                  </div>
                ) : null}
              </div>
            </div>
          ))}
          {isPending ? (
            <div className="message-row assistant">
              <div className="message-avatar">AI</div>
              <div className="message-stack">
                <div className="message-bubble assistant typing">
                  <div className="message-author">Assistant</div>
                  <p>Building grounded investigation context...</p>
                </div>
              </div>
            </div>
          ) : null}
        </div>
        <div className="conversation-composer">
          <div className="suggestion-grid">
            {suggestions.map((suggestion) => (
              <button key={suggestion} type="button" className="suggestion-chip" onClick={() => onSuggestion(suggestion)}>
                {suggestion}
              </button>
            ))}
          </div>
          <div className="composer-box">
            <textarea
              className="chat-input"
              placeholder="Ask about an alert, incident, actor, or what to do next..."
              value={message}
              onChange={(event) => onMessageChange(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter" && !event.shiftKey) {
                  event.preventDefault();
                  onSubmit();
                }
              }}
            />
            <button type="button" className="action-button primary send-button" onClick={onSubmit} disabled={isPending}>
              Send
            </button>
          </div>
        </div>
      </article>
    </section>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
