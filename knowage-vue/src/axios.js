import axios from 'axios'
//import store from './App.store.js'
//import router from './app.routes.js'
	//axios.defaults.baseURL = process.env.VUE_APP_HOST_URL
  axios.interceptors.request.use(
    config => {
			config.headers.common["Accept"] = "application/json; charset=utf-8"
			config.headers.common["Content-Type"] = "application/json; charset=utf-8"
      return config;
    },
    error => {
        Promise.reject(error)
    });

  axios.interceptors.response.use(
    res => {
      return res
    },
    function (error) {
			console.log(error.response)
      if(error.status === 401){
				console.log(status)
				//if(router.history.current.name !== 'login') router.push('/login')
      }
			if(error.status === 500){
				console.log(500)
			}
      return Promise.reject(error);
    }
  )

export default axios
