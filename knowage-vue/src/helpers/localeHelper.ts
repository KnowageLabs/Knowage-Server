import moment from 'moment'
import store from '@/App.store.js'

export type Locale = {
  country: string
  language: string
}

export function concatLocale(obj: Locale): string {
  return obj ? obj.language + '_' + obj.country : 'undefined'
}

export function formatDate(dateString?: string, format?: string) {
  return moment(dateString || new Date())
    .locale(localStorage.getItem('locale') ? concatLocale(JSON.parse(localStorage.getItem('locale') || '')) : store.locale)
    .format(format || 'L')
}
