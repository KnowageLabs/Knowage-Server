import moment from 'moment'
import store from '@/App.store.js'

let fallbackLocale = 'en_US'

function getLocale(js?: boolean): string {
    let locale = ''
    if (localStorage.getItem('locale')) locale = localStorage.getItem('locale') || ''
    else locale = store.locale ? store.local : fallbackLocale
    return js ? locale.replace('_', '-') : locale
}

export function formatDate(dateString?: string, format?: string, incomingFormat?: string) {
    return moment(dateString || new Date(), incomingFormat)
        .locale(getLocale())
        .format(format || 'L')
}

export function formatDateWithLocale(dateString?: string, format?: any) {
    let dateToFormat = new Date()
    if (dateString) dateToFormat = new Date(dateString)
    return Intl.DateTimeFormat(getLocale(true), format).format(dateToFormat)
}

export function formatNumberWithLocale(number: number, precision?: number, format?: any) {
    return Intl.NumberFormat(getLocale(true), { ...format, minimumFractionDigits: precision || 2, maximumFractionDigits: precision || 2 }).format(number)
}
