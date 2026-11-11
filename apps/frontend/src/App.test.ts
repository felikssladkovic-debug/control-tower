import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { flushPromises, mount, type VueWrapper } from "@vue/test-utils";
import PrimeVue from "primevue/config";
import App from "./App.vue";
import { router } from "./router";
import { setLocale } from "./i18n";

let wrapper: VueWrapper | undefined;

beforeEach(async () => {
  localStorage.clear();
  setLocale("ru");
  vi.stubGlobal(
    "fetch",
    vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ name: "PACT", version: "0.1.0-SNAPSHOT" }),
    }),
  );
  await router.push("/");
  await router.isReady();
});

afterEach(() => {
  wrapper?.unmount();
  wrapper = undefined;
  vi.unstubAllGlobals();
});

function renderApp() {
  wrapper = mount(App, { global: { plugins: [router, PrimeVue] } });
  return wrapper;
}

describe("workspace shell", () => {
  it("shows Overview and backend version, then switches the full UI to English", async () => {
    const app = renderApp();
    await flushPromises();
    expect(app.text()).toContain("Добро пожаловать в PACT");
    expect(app.text()).toContain("API доступен");
    expect(app.text()).toContain("0.1.0-SNAPSHOT");

    await app.find("select").setValue("en");
    expect(app.text()).toContain("Welcome to PACT");
    expect(app.text()).toContain("API available");
    expect(localStorage.getItem("pact.locale")).toBe("en");
    expect(document.documentElement.lang).toBe("en");
  });

  it("keeps the only navigation destination available when the sidebar is collapsed", async () => {
    const app = renderApp();
    const nav = app.get("nav");
    expect(nav.findAll("a")).toHaveLength(1);
    await app.get(".collapse-button").trigger("click");
    expect(app.classes()).toContain("sidebar-collapsed");
    expect(nav.get("a").attributes("aria-label")).toBe("Обзор");
    expect(nav.get("a").attributes("href")).toBe("/");
  });

  it("renders Not Found for an unknown route and returns to Overview", async () => {
    const app = renderApp();
    await router.push("/unknown/path");
    await flushPromises();
    expect(app.text()).toContain("Страница не найдена");
    await app.get(".back-link").trigger("click");
    await flushPromises();
    expect(app.text()).toContain("Добро пожаловать в PACT");
  });
});
