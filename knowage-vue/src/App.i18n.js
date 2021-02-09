import { createI18n } from 'vue-i18n'

const messages = {
    'en_BG': require('@/i18n/en_BG.json'),
    'it_IT': require('@/i18n/it_IT.json')
}

const i18n =  createI18n({
    locale: 'it_IT',
    fallbackLocale: 'en_BG',
    messages: messages
})

export default i18n