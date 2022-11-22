import { AxiosResponse } from 'axios'
import i18n from '@/App.i18n'
import store from '../../App.store'
import axios from 'axios'

export async function getCorrectRolesForExecution(typeCode, id, label) {
    let params = 'typeCode=' + typeCode + '&' + (id ? `id=${id}` : `label=${label}`)

    let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/documentexecution/correctRolesForExecution?` + params

    return new Promise((resolve, reject) => {
        axios.get(url).then((response: AxiosResponse<any>) => {
            let rolesForExecution = response.data
            if (rolesForExecution.length == 0) {
                store.commit('setError', {
                    title: i18n.global.t('common.error.generic'),
                    msg: i18n.global.t('documentExecution.main.userRoleError')
                })
                reject()
            } else {
                resolve(rolesForExecution)
            }
        })
    })
}
