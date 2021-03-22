import { createI18n } from 'vue-i18n'

const messages = {
	en_GB: require('@/i18n/en_GB.json'),
	it_IT: require('@/i18n/it_IT.json'),
	en_US: require('@/i18n/en_US.json'),
	fr_FR: require('@/i18n/fr_FR.json'),
	es_ES: require('@/i18n/es_ES.json'),
	pt_BR: require('@/i18n/pt_BR.json')
}

const i18n = createI18n({
	locale: localStorage.getItem('user') ? localStorage.getItem('user').locale : null,
	fallbackLocale: 'en_GB',
	messages: messages
})

export default i18n
