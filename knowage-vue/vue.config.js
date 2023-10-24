process.env.VUE_APP_VERSION = process.env.npm_package_version

module.exports = {
    publicPath: process.env.VUE_APP_PUBLIC_PATH,
    outputDir: './target/knowage-vue',
    devServer: {
        https: process.env.VUE_APP_HOST_HTTPS === 'true',
        proxy: {
            '^/knowagedossierengine/api': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowageqbeengine/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagemeta/restful-services/1.0/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/restful-services/[0-9].0': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/restful-services/': {
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
            },
            '^/knowage/servlet': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagemeta': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagecockpitengine/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagewhatifengine/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage-data-preparation/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true,
                ws: true
            },
            '^/knowagekpiengine/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            }
        }
    },
    configureWebpack: {
        devtool: 'source-map',
        devServer: {
            headers: { 'Access-Control-Allow-Origin': '*' }
        }
    },
    chainWebpack: (config) => {
        config.plugin('html').tap((args) => {
            args[0].title = 'Knowage'
            return args
        })
    },
    css: {
        loaderOptions: {
            scss: {
                additionalData: '@import "@/assets/scss/main.scss";'
            }
        }
    },
    pwa: {
        manifestOptions: {
            id: '/knowage-vue/',
            scope: '/knowage-vue/',
            name: 'Knowage',
            short_name: 'Knowage',
            display: 'standalone',
            orientation: 'landscape',
            background_color: '#3b678c',
            theme_color: '#3b678c',
            description: 'The business intelligence open-source solution'
        },
        themeColor: '#3b678c',
        msTileColor: '#3b678c',
        appleMobileWebAppCapable: 'yes',
        appleMobileWebAppStatusBarStyle: 'black',
        workboxOptions: {
            exclude: [/^.*knowage-vue\/.*$/],
            runtimeCaching: [
                {
                    urlPattern: /^.+\.(ttf|woff2)/i,
                    handler: 'CacheFirst',
                    options: {
                        cacheName: 'fonts',
                        expiration: {
                            maxEntries: 10,
                            maxAgeSeconds: 60 * 60 * 24 * 365 // <== 365 days
                        },
                        cacheableResponse: {
                            statuses: [0, 200]
                        }
                    }
                },
                {
                    urlPattern: /^.+\.css/i,
                    handler: 'CacheFirst',
                    options: {
                        cacheName: 'styles',
                        expiration: {
                            maxAgeSeconds: 60 * 60 * 24 * 365 // <== 365 days
                        },
                        cacheableResponse: {
                            statuses: [0, 200]
                        }
                    }
                },
                {
                    urlPattern: /^.+\.js/i,
                    handler: 'CacheFirst',
                    options: {
                        cacheName: 'scripts',
                        expiration: {
                            maxAgeSeconds: 60 * 60 * 24 * 10 // <== 365 days
                        },
                        cacheableResponse: {
                            statuses: [0, 200]
                        }
                    }
                },
                {
                    urlPattern: /^.+\.(svg|png|jpg)/i,
                    handler: 'CacheFirst',
                    options: {
                        cacheName: 'images',
                        expiration: {
                            maxAgeSeconds: 60 * 60 * 24 * 10 // <== 365 days
                        },
                        cacheableResponse: {
                            statuses: [0, 200]
                        }
                    }
                }
            ]
        },
        manifestCrossorigin: 'use-credentials'
    }
}
