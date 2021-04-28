import { createI18n } from 'vue-i18n'
import store from './App.store'

const messages = {
	bg_BG: require('@/i18n/bg_BG.json'),
	de_DE: require('@/i18n/de_DE.json'),
	en_GB: require('@/i18n/en_GB.json'),
	en_US: require('@/i18n/en_US.json'),
	es_ES: require('@/i18n/es_ES.json'),
	fr_FR: require('@/i18n/fr_FR.json'),
	it_IT: require('@/i18n/it_IT.json'),
	ja_JP: require('@/i18n/ja_JP.json'),
	ko_KR: require('@/i18n/ko_KR.json'),
	pt_BR: require('@/i18n/pt_BR.json'),
	ru_RU: require('@/i18n/ru_RU.json'),
	tr_TR: require('@/i18n/tr_TR.json'),
	'zh_CN_#hans': require('@/i18n/zh_Hans_CN.json')
	
}

let currentLocale = localStorage.getItem('locale') ? localStorage.getItem('locale') : store.local

const i18n = createI18n({
	locale: currentLocale,
	fallbackLocale: 'en_US',
	messages: messages
})

export default i18n
