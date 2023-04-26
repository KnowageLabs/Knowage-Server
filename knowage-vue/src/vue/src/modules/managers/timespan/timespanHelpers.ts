import moment from 'moment'

export function createDateFromIntervalTime(intervalTime: string) {
    return moment(intervalTime, 'DD/MM/yyyy').toDate()
}