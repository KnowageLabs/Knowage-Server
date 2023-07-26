import { createI18n } from 'vue-i18n'
import store from './App.store'

const messages = {
    en_US: require('@/i18n/en_US/messages.json')
}

let currentLocale = localStorage.getItem('locale') ? localStorage.getItem('locale') : store.locale

const i18n = createI18n({
    locale: currentLocale,
    fallbackLocale: 'en_US',
    messages: messages
})

const loadedLanguages = []

export default i18n

function setI18nLanguage(lang) {
    i18n.locale = lang
}

export function loadLanguageAsync(lang) {
    return new Promise((resolve) => {
        // If the same language
        if (i18n.locale === lang) {
            resolve(setI18nLanguage(lang))
        }

        // If the language was already loaded
        if (loadedLanguages.includes(lang)) {
            resolve(setI18nLanguage(lang))
        }

        // If the language hasn't been loaded yet
        import(`./i18n/${lang}/messages.json`).then((messages) => {
            import(`./i18n/${lang}/helper-messages.json`).then((m) => {
                // eslint-disable-next-line
                // @ts-ignore
                i18n.global.setLocaleMessage(lang, messages.default)
                i18n.global.mergeLocaleMessage(lang, m.default)
                loadedLanguages.push(lang)
                resolve(setI18nLanguage(lang))
            })
        })
    })
}
