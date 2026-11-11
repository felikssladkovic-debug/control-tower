<script setup lang="ts">
import { onMounted, onUnmounted, provide, ref, watch } from "vue";
import { RouterLink, RouterView, useRoute } from "vue-router";
import Button from "primevue/button";
import { locale, setLocale, t, type Locale } from "./i18n";
import { systemInfoKey, useSystemInfo } from "./system";

const system = useSystemInfo();
provide(systemInfoKey, system);
const sidebarCollapsed = ref(false);
const mobileOpen = ref(false);
const route = useRoute();

watch(
  () => route.fullPath,
  () => {
    mobileOpen.value = false;
  },
);
onMounted(() => {
  document.documentElement.lang = locale.value;
  system.start();
});
onUnmounted(() => system.stop());

function changeLanguage(event: Event): void {
  setLocale((event.target as HTMLSelectElement).value as Locale);
}
</script>

<template>
  <div
    class="app-frame"
    :class="{
      'sidebar-collapsed': sidebarCollapsed,
      'mobile-open': mobileOpen,
    }"
    @keydown.esc="mobileOpen = false"
  >
    <header class="topbar">
      <div class="topbar-left">
        <Button
          class="mobile-menu-button"
          text
          icon="pi pi-bars"
          :aria-label="t('app.toggleNavigation')"
          :aria-expanded="mobileOpen"
          aria-controls="primary-sidebar"
          @click="mobileOpen = !mobileOpen"
        />
        <div class="brand-mark" aria-hidden="true">
          <span></span><span></span><span></span>
        </div>
        <span class="brand-name">PACT</span>
        <span class="topbar-divider" aria-hidden="true"></span>
        <span class="product-name">{{ t("app.title") }}</span>
      </div>
      <div class="topbar-right">
        <span class="workspace-label">{{ t("app.workspace") }}</span>
        <label class="language-picker">
          <i class="pi pi-globe" aria-hidden="true"></i>
          <span class="sr-only">{{ t("app.language") }}</span>
          <select
            :value="locale"
            :aria-label="t('app.language')"
            @change="changeLanguage"
          >
            <option value="ru">{{ t("app.ru") }}</option>
            <option value="en">{{ t("app.en") }}</option>
          </select>
          <i class="pi pi-chevron-down picker-chevron" aria-hidden="true"></i>
        </label>
      </div>
    </header>

    <button
      v-if="mobileOpen"
      class="mobile-scrim"
      type="button"
      :aria-label="t('app.closeNavigation')"
      @click="mobileOpen = false"
    ></button>

    <aside id="primary-sidebar" class="sidebar">
      <div class="sidebar-top">
        <span class="sidebar-section" v-show="!sidebarCollapsed">{{
          t("app.workspace")
        }}</span>
        <Button
          class="collapse-button"
          text
          :icon="
            sidebarCollapsed
              ? 'pi pi-angle-double-right'
              : 'pi pi-angle-double-left'
          "
          :aria-label="t('app.toggleNavigation')"
          @click="sidebarCollapsed = !sidebarCollapsed"
        />
      </div>
      <nav :aria-label="t('nav.primary')">
        <RouterLink
          to="/"
          class="nav-item"
          :class="{ active: route.name === 'overview' }"
          :aria-label="t('nav.overview')"
          :title="sidebarCollapsed ? t('nav.overview') : undefined"
        >
          <i class="pi pi-th-large" aria-hidden="true"></i>
          <span v-show="!sidebarCollapsed">{{ t("nav.overview") }}</span>
        </RouterLink>
      </nav>
      <div class="sidebar-footer">
        <span class="footer-dot"></span
        ><span v-show="!sidebarCollapsed">PACT</span>
      </div>
    </aside>

    <main class="main-content" id="main-content">
      <RouterView />
    </main>
  </div>
</template>
