import moment from 'moment'
import store from '@/app.store.js'

export function formatDate(dateString?: string, locale?: string, format?: string){
    return moment(dateString || new Date()).locale(store.state.locale || 'en_GB').format(format || 'L')
}
  