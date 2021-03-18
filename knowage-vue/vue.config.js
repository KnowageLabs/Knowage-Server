module.exports = {
	publicPath: '/knowage/',
	devServer: {
		proxy: {
			'^/knowage/restful-services/[0-9].0': {
				target: process.env.VUE_APP_HOST_URL,
				changeOrigin: true
			},
			'^/knowage-api/api/': {
				target: process.env.VUE_APP_HOST_URL,
				changeOrigin: true
			},
			'^/knowage/webSocket': {
				target: process.env.VUE_APP_HOST_URL,
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
	},
	css: {
		loaderOptions: {
			scss: {
				additionalData: `
				@import "@/assets/scss/_variables.scss";
				@import "@/assets/scss/_common.scss";
				@import "@/assets/scss/_material.scss";`
			}
		}
	}
}
