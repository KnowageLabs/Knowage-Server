import moment from 'moment'
import store from '@/App.store.js'

export function formatDate(dateString?: string, format?: string) {
	return moment(dateString || new Date())
		.locale(store.state.user.locale)
		.format(format || 'L')
}
