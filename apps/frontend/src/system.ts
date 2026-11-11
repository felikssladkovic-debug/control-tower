import { ref, type InjectionKey } from "vue";

export type ConnectionStatus = "loading" | "online" | "offline" | "error";
export interface SystemInfo {
  name: string;
  version: string;
}

export function useSystemInfo() {
  const status = ref<ConnectionStatus>("loading");
  const info = ref<SystemInfo | null>(null);
  const checking = ref(false);
  let timer: ReturnType<typeof setInterval> | undefined;
  let controller: AbortController | undefined;
  let requestId = 0;

  async function refresh(): Promise<void> {
    if (checking.value) return;
    checking.value = true;
    const currentRequest = ++requestId;
    controller = new AbortController();
    const timeout = setTimeout(() => controller?.abort(), 5000);
    try {
      const response = await fetch("/api/system/info", {
        signal: controller.signal,
        headers: { Accept: "application/json" },
      });
      if (!response.ok) {
        status.value = "error";
        info.value = null;
        return;
      }
      const data: unknown = await response.json();
      if (
        typeof data !== "object" ||
        data === null ||
        !("name" in data) ||
        typeof data.name !== "string" ||
        data.name.trim() === "" ||
        !("version" in data) ||
        typeof data.version !== "string" ||
        data.version.trim() === ""
      ) {
        status.value = "error";
        info.value = null;
        return;
      }
      info.value = { name: data.name, version: data.version };
      status.value = "online";
    } catch {
      status.value = "offline";
      info.value = null;
    } finally {
      clearTimeout(timeout);
      if (currentRequest === requestId) checking.value = false;
    }
  }

  function start(): void {
    void refresh();
    timer = setInterval(() => void refresh(), 30_000);
  }

  function stop(): void {
    if (timer) clearInterval(timer);
    requestId++;
    controller?.abort();
  }

  return { status, info, checking, refresh, start, stop };
}

export const systemInfoKey: InjectionKey<ReturnType<typeof useSystemInfo>> =
  Symbol("systemInfo");
