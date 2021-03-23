import { concatLocale } from '@/helpers/localeHelper'
import { createI18n } from 'vue-i18n'
import store from './App.store'

const messages = {
	en_GB: require('@/i18n/en_GB.json'),
	it_IT: require('@/i18n/it_IT.json'),
	en_US: require('@/i18n/en_US.json'),
	fr_FR: require('@/i18n/fr_FR.json'),
	es_ES: require('@/i18n/es_ES.json'),
	pt_BR: require('@/i18n/pt_BR.json')
}

let currentLocale = localStorage.getItem('locale') ? concatLocale(localStorage.getItem('locale').language) : store.local

const i18n = createI18n({
	locale: currentLocale,
	fallbackLocale: 'en_GB',
	messages: messages
})

export default i18n
