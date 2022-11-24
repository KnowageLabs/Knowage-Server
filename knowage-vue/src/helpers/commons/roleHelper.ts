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
                let msg = ''
                switch (typeCode) {
                    case 'DOCUMENT':
                        msg = i18n.global.t('documentExecution.main.userRoleError')
                        break
                    case 'DATAMART':
                        msg = i18n.global.t('workspace.myModels.userRoleError')
                        break
                    case 'DATASET':
                        msg = i18n.global.t('workspace.myData.userRoleError')
                        break
                }
                store.commit('setError', {
                    title: i18n.global.t('common.error.generic'),
                    msg: msg
                })
                reject()
            } else {
                resolve(rolesForExecution)
            }
        })
    })
}
