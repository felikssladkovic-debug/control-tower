<script setup lang="ts">
import { computed, inject } from "vue";
import Button from "primevue/button";
import { t } from "../i18n";
import { systemInfoKey } from "../system";

const system = inject(systemInfoKey);
if (!system) throw new Error("System info provider is missing");
const { status, info, checking, refresh } = system;

const statusLabel = computed(() => t(`overview.${status.value}`));
</script>

<template>
  <div class="page-shell">
    <div class="breadcrumb">
      <span>PACT</span><i class="pi pi-angle-right" aria-hidden="true"></i
      ><span>{{ t("nav.overview") }}</span>
    </div>
    <div class="page-heading">
      <p class="eyebrow">{{ t("overview.eyebrow") }}</p>
      <h1>{{ t("overview.title") }}</h1>
      <p class="page-description">{{ t("overview.description") }}</p>
    </div>

    <section class="panel system-panel" :aria-label="t('overview.system')">
      <div class="panel-header">
        <div>
          <h2>{{ t("overview.system") }}</h2>
          <p>GET /api/system/info</p>
        </div>
        <Button
          class="retry-button"
          outlined
          size="small"
          icon="pi pi-refresh"
          :label="checking ? t('overview.checking') : t('overview.retry')"
          :disabled="checking"
          @click="refresh"
        />
      </div>
      <div class="system-grid">
        <div class="system-cell">
          <span class="field-label">{{ t("overview.backend") }}</span>
          <div class="status-value" role="status" aria-live="polite">
            <span class="status-dot" :class="status"></span>
            <span>{{ statusLabel }}</span>
          </div>
        </div>
        <div class="system-cell">
          <span class="field-label">{{ t("overview.version") }}</span>
          <strong class="version-value">{{
            status === "online" && info ? info.version : t("overview.noVersion")
          }}</strong>
        </div>
      </div>
    </section>
  </div>
</template>
