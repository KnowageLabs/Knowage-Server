import axios from 'axios'
//import router from './App.routes.js'
import store from './App.store.js'
import authHelper from '@/helpers/commons/authHelper'

axios.defaults.baseURL = import.meta.env.VITE_BASE_URL

axios.interceptors.request.use(
    (config) => {
        config.headers.common['Accept'] = 'application/json; charset=utf-8'
        config.headers.common['Content-Type'] = 'application/json; charset=utf-8'
        config.headers.common['Access-Control-Allow-Origin'] = '*'
        if (localStorage.getItem('token')) config.headers.common[import.meta.env.VITE_DEFAULT_AUTH_HEADER] = 'Bearer ' + localStorage.getItem('token')
        return config
    },
    (error) => {
        Promise.reject(error)
    }
)

axios.interceptors.response.use(
    (res) => {
        if (res.config.headers['X-Disable-Interceptor']) return res
        if (res.data && res.data.errors) {
            if (!res.config.headers['X-Disable-Errors']) store.commit('setError', { title: 'Server error', msg: res.data.errors[0].message })
            return Promise.reject(res.data.errors[0])
        }
        return res
    },
    function (error) {
        if (error.response && error.response.status) {
            if (error.response.status === 401) {
                authHelper.handleUnauthorized()
            }
            if (error.response.status === 500) {
                console.log(500)

                let obj = error.response.data
                if (error.response.data instanceof ArrayBuffer) {
                    obj = JSON.parse(new TextDecoder().decode(error.response.data))
                }
                if (obj.errors) {
                    if (obj.errors[0].code) {
                        let errArray = obj.errors

                        for (var idx in errArray) {
                            let err = errArray[idx]

                            let hints = ''
                            for (var hintIdx in err.hints) {
                                let hint = err.hints[hintIdx]

                                if (idx > 0) hints += '\n' + hint
                                else hints += hint
                            }
                            store.commit('setError', { title: err.message, msg: hints })
                        }
                    } else {
                        store.commit('setError', { title: 'Server error', msg: obj.errors[0].message })
                    }
                }
            }
        }
        return Promise.reject(error)
    }
)

export default axios
