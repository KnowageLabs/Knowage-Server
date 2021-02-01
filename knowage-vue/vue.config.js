module.exports = {
  publicPath: '/knowage/',
  devServer: {
    proxy: {
      '^/knowage/restful-services/1.0': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  configureWebpack: {
    devtool: "source-map"
  },
  chainWebpack: config => {
    config
    .plugin('html')
    .tap(args => {
      args[0].title = 'Knowage'
      return args
    })
  }
}
  