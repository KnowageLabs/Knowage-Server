import { createI18n } from "vue-i18n";
import store from "./App.store";

const messages = {
  en_US: require("@/i18n/en_US/messages.json"),
};

function normalizeLocale(locale) {
  if (!locale) return locale;
  return locale.replace("-", "_");
}

let currentLocale = normalizeLocale(localStorage.getItem("locale") ? localStorage.getItem("locale") : store.locale);

const i18n = createI18n({
  locale: currentLocale,
  fallbackLocale: "en_US",
  messages: messages,
});

const loadedLanguages = [];

export default i18n;

function setI18nLanguage(lang) {
  i18n.locale = lang;
}

export function loadLanguageAsync(lang) {
  const normalizedLang = normalizeLocale(lang);
  return new Promise((resolve) => {
    // If the same language
    if (i18n.locale === normalizedLang) {
      resolve(setI18nLanguage(normalizedLang));
    }

    // If the language was already loaded
    if (loadedLanguages.includes(normalizedLang)) {
      resolve(setI18nLanguage(normalizedLang));
    }

    // If the language hasn't been loaded yet
    import(`./i18n/${normalizedLang}/messages.json`).then((messages) => {
      import(`./i18n/${normalizedLang}/helper-messages.json`).then((m) => {
        // eslint-disable-next-line
        // @ts-ignore
        i18n.global.setLocaleMessage(normalizedLang, messages.default);
        i18n.global.mergeLocaleMessage(normalizedLang, m.default);
        loadedLanguages.push(normalizedLang);
        resolve(setI18nLanguage(normalizedLang));
      });
    });
  });
}
