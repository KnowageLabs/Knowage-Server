import axios from 'axios'

  axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*'
  axios.defaults.headers.common['Content-Type'] ='application/json;charset=utf-8'
  axios.defaults.headers.common['Crossorigin'] ='true'
  axios.interceptors.response.use(
    res => {
      return res
    },
    err => {
      console.log('axios',err)
      throw err;
    }
  )

export default axios
