import axios from 'axios'
//import router from './App.routes.js'
import authHelper from '@/helpers/authHelper'

axios.interceptors.request.use(
	(config) => {
		config.headers.common['Accept'] = 'application/json; charset=utf-8'
		config.headers.common['Content-Type'] = 'application/json; charset=utf-8'
		if (localStorage.getItem('token')) config.headers.common['Authorization'] = 'Bearer ' + localStorage.getItem('token')
		return config
	},
	(error) => {
		Promise.reject(error)
	}
)

axios.interceptors.response.use(
	(res) => {
		return res
	},
	function(error) {
		if (error.response.status) {
			if (error.response.status === 401) {
				authHelper.logout()
			}
			if (error.response.status === 500) {
				console.log(500)
			}
		}
		return Promise.reject(error)
	}
)

export default axios
