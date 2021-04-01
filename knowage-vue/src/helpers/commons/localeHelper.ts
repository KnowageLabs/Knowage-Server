import moment from 'moment'
import store from '@/App.store.js'
import i18n from '@/App.i18n'

let fallbackLocale = 'en_US'

export function formatDate(dateString?: string, format?: string) {
	let locale = ''
	if (localStorage.getItem('locale')) locale = localStorage.getItem('locale') || ''

	if (locale == '') locale = store.locale ? store.local : fallbackLocale

	return moment(dateString || new Date())
		.locale(locale)
		.format(format || 'L')
}
