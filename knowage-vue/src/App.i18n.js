import { createI18n } from 'vue-i18n'
import store from './App.store'

const messages = {
    en_US: require('@/i18n/messages.json'),
    bg_BG: require('@/i18n/messages-bg_BG.json'),
    de_DE: require('@/i18n/messages-de_DE.json'),
    en_GB: require('@/i18n/messages-en_GB.json'),
    es_ES: require('@/i18n/messages-es_ES.json'),
    fr_FR: require('@/i18n/messages-fr_FR.json'),
    hu_HU: require('@/i18n/messages-hu_HU.json'),
    it_IT: require('@/i18n/messages-it_IT.json'),
    ja_JP: require('@/i18n/messages-ja_JP.json'),
    ko_KR: require('@/i18n/messages-ko_KR.json'),
    pt_BR: require('@/i18n/messages-pt_BR.json'),
    ru_RU: require('@/i18n/messages-ru_RU.json'),
    tr_TR: require('@/i18n/messages-tr_TR.json'),
    'zh_CN_#Hans': require('@/i18n/messages-zh_Hans_CN.json')
}

let currentLocale = localStorage.getItem('locale') ? localStorage.getItem('locale') : store.locale

const i18n = createI18n({
    locale: currentLocale,
    fallbackLocale: 'en_US',
    messages: messages
})

export default i18n
