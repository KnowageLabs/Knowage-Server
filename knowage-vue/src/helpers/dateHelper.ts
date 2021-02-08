import moment from 'moment'

export function formatDate(dateString?: string, format?: string){
    return moment(dateString || new Date()).format(format || 'L')
}
  