import { createI18n } from 'vue-i18n'

const messages = {
    'en': require('@/i18n/en_BG.json'),
    'it': require('@/i18n/it_IT.json')
}

const i18n =  createI18n({
    locale: 'it',
    fallbackLocale: 'en',
    messages: messages
})

export default i18n