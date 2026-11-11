import { ref } from "vue";

export type Locale = "ru" | "en";

const messages = {
  ru: {
    "app.title": "Control Tower",
    "app.workspace": "Рабочая область",
    "app.toggleNavigation": "Переключить навигацию",
    "app.closeNavigation": "Закрыть навигацию",
    "app.language": "Язык интерфейса",
    "app.ru": "Русский",
    "app.en": "English",
    "nav.overview": "Обзор",
    "nav.primary": "Основная навигация",
    "overview.eyebrow": "РАБОЧАЯ ОБЛАСТЬ / ОБЗОР",
    "overview.title": "Добро пожаловать в PACT",
    "overview.description":
      "Техническая основа Control Tower. Предметные данные и показатели появятся только после проверки источников и сценариев.",
    "overview.system": "Подключение к системе",
    "overview.backend": "Backend API",
    "overview.version": "Версия backend",
    "overview.noVersion": "Версия пока недоступна",
    "overview.loading": "Проверяем подключение",
    "overview.online": "API доступен",
    "overview.offline": "Нет соединения с API",
    "overview.error": "Не удалось получить сведения об API",
    "overview.retry": "Повторить",
    "overview.checking": "Проверяем",
    "notFound.eyebrow": "ОШИБКА / 404",
    "notFound.title": "Страница не найдена",
    "notFound.description": "Проверьте адрес или вернитесь в рабочую область.",
    "notFound.back": "Вернуться к обзору",
  },
  en: {
    "app.title": "Control Tower",
    "app.workspace": "Workspace",
    "app.toggleNavigation": "Toggle navigation",
    "app.closeNavigation": "Close navigation",
    "app.language": "Interface language",
    "app.ru": "Русский",
    "app.en": "English",
    "nav.overview": "Overview",
    "nav.primary": "Primary navigation",
    "overview.eyebrow": "WORKSPACE / OVERVIEW",
    "overview.title": "Welcome to PACT",
    "overview.description":
      "The technical foundation of Control Tower. Domain data and metrics will appear only after sources and use cases are validated.",
    "overview.system": "System connection",
    "overview.backend": "Backend API",
    "overview.version": "Backend version",
    "overview.noVersion": "Version is not available yet",
    "overview.loading": "Checking connection",
    "overview.online": "API available",
    "overview.offline": "Cannot connect to API",
    "overview.error": "Could not load API information",
    "overview.retry": "Retry",
    "overview.checking": "Checking",
    "notFound.eyebrow": "ERROR / 404",
    "notFound.title": "Page not found",
    "notFound.description": "Check the address or return to the workspace.",
    "notFound.back": "Back to overview",
  },
} as const;

export type MessageKey = keyof (typeof messages)["ru"];

function storedLocale(): Locale {
  try {
    return localStorage.getItem("pact.locale") === "en" ? "en" : "ru";
  } catch {
    return "ru";
  }
}

export const locale = ref<Locale>(storedLocale());

export function setLocale(value: Locale): void {
  locale.value = value;
  document.documentElement.lang = value;
  try {
    localStorage.setItem("pact.locale", value);
  } catch {
    // The language still changes when browser storage is unavailable.
  }
}

export function t(key: MessageKey): string {
  return messages[locale.value][key];
}
