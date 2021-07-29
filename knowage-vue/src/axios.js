import axios from 'axios'
//import router from './App.routes.js'
import store from './App.store.js'
import authHelper from '@/helpers/commons/authHelper'

axios.defaults.baseURL = process.env.VUE_APP_BASE_URL

axios.interceptors.request.use(
    (config) => {
        config.headers.common['Accept'] = 'application/json; charset=utf-8'
        config.headers.common['Content-Type'] = 'application/json; charset=utf-8'
        if (localStorage.getItem('token')) config.headers.common[process.env.VUE_APP_DEFAULT_AUTH_HEADER] = 'Bearer ' + localStorage.getItem('token')
        return config
    },
    (error) => {
        Promise.reject(error)
    }
)

axios.interceptors.response.use(
    (res) => {
        if (res.data && res.data.errors) return Promise.reject(res.data.errors[0])
        return res
    },
    function(error) {
        if (error.response.status) {
            if (error.response.status === 401) {
                authHelper.logout()
            } else if (error.response.status === 500) {
                console.log(500)
                store.commit('setError', { title: 'Server error', msg: error.response.data.errors[0].message })
            } else store.commit('setError', { title: 'Server error', msg: error.response.data.errors[0].message })
        }
        return Promise.reject(error)
    }
)

export default axios
