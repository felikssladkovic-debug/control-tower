import { afterEach, describe, expect, it, vi } from "vitest";
import { useSystemInfo } from "./system";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("backend connection", () => {
  it("moves from loading to online and clears stale version after a network failure", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ name: "PACT", version: "0.1.0-SNAPSHOT" }),
      })
      .mockRejectedValueOnce(new TypeError("Network unavailable"));
    vi.stubGlobal("fetch", fetchMock);
    const system = useSystemInfo();

    expect(system.status.value).toBe("loading");
    await system.refresh();
    expect(system.status.value).toBe("online");
    expect(system.info.value?.version).toBe("0.1.0-SNAPSHOT");
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/system/info",
      expect.objectContaining({ headers: { Accept: "application/json" } }),
    );

    await system.refresh();
    expect(system.status.value).toBe("offline");
    expect(system.info.value).toBeNull();
  });

  it("distinguishes an invalid or failed API response from no connection and can recover", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce({ ok: false, status: 503 })
      .mockResolvedValueOnce({ ok: true, json: async () => ({ name: "PACT" }) })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ name: "PACT", version: "1.2.3" }),
      });
    vi.stubGlobal("fetch", fetchMock);
    const system = useSystemInfo();

    await system.refresh();
    expect(system.status.value).toBe("error");
    await system.refresh();
    expect(system.status.value).toBe("error");
    await system.refresh();
    expect(system.status.value).toBe("online");
    expect(system.info.value?.version).toBe("1.2.3");
  });
});
