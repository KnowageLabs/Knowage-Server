import moment from 'moment'
import { DateTime } from 'luxon'
import store from '@/App.store.js'

import formats from '@/helpers/commons/localeDateFormats.json'

let fallbackLocale = 'en_US'

export function getLocale(js?: boolean): string {
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

export function luxonFormatDate(dateString: any | Date, inputFormat?: string, outputFormat?: string) {
    const tempDate = inputFormat ? DateTime.fromFormat(dateString, inputFormat) : DateTime.fromJSDate(dateString).setLocale(getLocale(true))
    if (outputFormat) return tempDate.toFormat(outputFormat)
    else return tempDate.toLocaleString(DateTime.DATE_SHORT)
}

export function primeVueDate(locale: any = 'en-US'): String {
    return formats[locale].replaceAll('yy', 'y').replaceAll('M', 'm')
}
