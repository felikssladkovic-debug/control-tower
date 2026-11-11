import { createApp } from "vue";
import PrimeVue from "primevue/config";
import { definePreset } from "@primeuix/themes";
import Nora from "@primeuix/themes/nora";
import "primeicons/primeicons.css";
import "./style.css";
import App from "./App.vue";
import { router } from "./router";

const PactPreset = definePreset(Nora, {
  semantic: {
    primary: {
      50: "#eff6ff",
      100: "#dbeafe",
      200: "#bfdbfe",
      300: "#93c5fd",
      400: "#60a5fa",
      500: "#3b82f6",
      600: "#2563eb",
      700: "#1d4ed8",
      800: "#1e40af",
      900: "#1e3a8a",
      950: "#172554",
    },
  },
});

createApp(App)
  .use(router)
  .use(PrimeVue, {
    theme: {
      preset: PactPreset,
      options: { darkModeSelector: "none" },
    },
  })
  .mount("#app");
