import moment from 'moment'
import { DateTime } from 'luxon'
import store from '@/App.store.js'

import formats from '@/helpers/commons/localeDateFormats.json'
import timezones from '@/helpers/commons/localeTimeZones.json'

let fallbackLocale = 'en_US'

export function getLocale(js?: boolean): string {
    let locale = ''
    if (localStorage.getItem('locale')) locale = localStorage.getItem('locale') || ''
    else locale = store.locale ? store.local : fallbackLocale
    return js ? locale.replace('_', '-') : locale
}

export function formatDate(dateString?: string, format?: string, incomingFormat?: string) {
    let tmp = moment(dateString || new Date(), incomingFormat).locale(getLocale())

    if (format === 'toISOString') return tmp.toISOString()
    else return tmp.format(format || 'L')
}

export function formatDateWithLocale(dateString?: string | number, format?: any, keepNull?: boolean): string {
    if (keepNull && !dateString) return ''
    let dateToFormat = new Date()
    if (dateString) {
        if (typeof dateString == 'string') {
            for (var key in timezones) dateString = dateString.replace(key, timezones[key])
        }
        dateToFormat = new Date(dateString)
    }

    return Intl.DateTimeFormat(getLocale(true), format).format(dateToFormat)
}

export function formatNumberWithLocale(number: number, precision?: number, format?: any) {
    return Intl.NumberFormat(getLocale(true), { ...format, minimumFractionDigits: precision || 2, maximumFractionDigits: precision || 2 }).format(number)
}

export function luxonFormatDate(dateString: any | Date, inputFormat?: string, outputFormat?: string) {
    const tempDate = inputFormat ? DateTime.fromFormat(dateString, inputFormat).setLocale(getLocale(true)) : DateTime.fromJSDate(dateString).setLocale(getLocale(true))
    console.log(">>>>>>> TEMP DATE: ", tempDate)
    console.log(">>>>>>> TEMP DATE BLA: ", tempDate.toFormat(outputFormat as string))
    if (outputFormat) return tempDate.toFormat(outputFormat)
    else return tempDate.toLocaleString(DateTime.DATE_SHORT)
}

export function localeDate(locale?: any): String {
    let loc = locale
    if (!loc) loc = getLocale(true)
    return formats[loc].replaceAll('m', 'M')
}

export function primeVueDate(locale?: any): String {
    let loc = locale
    if (!loc) loc = getLocale(true)
    return convertToPrimeVueFormat(formats[loc])
}

export function convertToPrimeVueFormat(format: String) {
    return format.replaceAll('yy', 'y').replaceAll('M', 'm')
}
