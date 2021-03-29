import moment from 'moment'
import store from '@/App.store.js'
import i18n from '@/App.i18n'

export function formatDate(dateString?: string, format?: string) {
	return moment(dateString || new Date())
		.locale(localStorage.getItem('locale') ? localStorage.getItem('locale') || i18n.fallbackLocale : store.locale ? store.local : i18n.fallbackLocale)
		.format(format || 'L')
}
