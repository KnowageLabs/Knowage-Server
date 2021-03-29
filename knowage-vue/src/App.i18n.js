import { createI18n } from 'vue-i18n'
import store from './App.store'

const messages = {
	de_DE: require('@/i18n/de_DE.json'),
	en_GB: require('@/i18n/en_GB.json'),
	en_US: require('@/i18n/en_US.json'),
	es_ES: require('@/i18n/es_ES.json'),
	fr_FR: require('@/i18n/fr_FR.json'),
	it_IT: require('@/i18n/it_IT.json'),
	pt_BR: require('@/i18n/pt_BR.json'),
	'zh_cn_#hans': require('@/i18n/zh_Hans_CN.json')
}

let currentLocale = localStorage.getItem('locale') ? localStorage.getItem('locale') : store.local

const i18n = createI18n({
	locale: currentLocale,
	fallbackLocale: 'en_US',
	messages: messages
})

export default i18n
