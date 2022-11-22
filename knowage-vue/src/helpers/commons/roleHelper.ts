import { AxiosResponse } from 'axios'
import i18n from '@/App.i18n'
import mainStore from '@/App.store'
import axios from 'axios'

export async function getCorrectRolesForExecution(typeCode, id, label) {
    let params = 'typeCode=' + typeCode + '&' + (id ? `id=${id}` : `label=${label}`)

    let url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/documentexecution/correctRolesForExecution?` + params

    const store = mainStore()
    return new Promise((resolve, reject) => {
        axios.get(url).then((response: AxiosResponse<any>) => {
            let rolesForExecution = response.data
            if (rolesForExecution.length == 0) {
                store.setError({
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
