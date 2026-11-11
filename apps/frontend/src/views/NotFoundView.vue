<script setup lang="ts">
import { RouterLink } from "vue-router";
import { t } from "../i18n";
</script>

<template>
  <div class="page-shell">
    <div class="breadcrumb">
      <span>PACT</span><i class="pi pi-angle-right" aria-hidden="true"></i
      ><span>404</span>
    </div>
    <div class="not-found">
      <p class="eyebrow">{{ t("notFound.eyebrow") }}</p>
      <h1>{{ t("notFound.title") }}</h1>
      <p class="page-description">{{ t("notFound.description") }}</p>
      <RouterLink class="back-link" to="/"
        ><i class="pi pi-arrow-left" aria-hidden="true"></i
        >{{ t("notFound.back") }}</RouterLink
      >
    </div>
  </div>
</template>
