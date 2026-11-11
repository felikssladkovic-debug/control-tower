import { createRouter, createWebHistory } from "vue-router";
import OverviewView from "./views/OverviewView.vue";
import NotFoundView from "./views/NotFoundView.vue";

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", component: OverviewView, name: "overview" },
    { path: "/:pathMatch(.*)*", component: NotFoundView, name: "not-found" },
  ],
});
