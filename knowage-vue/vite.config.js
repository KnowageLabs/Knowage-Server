import { defineConfig, loadEnv } from 'vite'
const { resolve } = require('path')
import vue from '@vitejs/plugin-vue'
import builtins from 'rollup-plugin-node-builtins'
import forwardToTrailingSlashPlugin from './forward-to-trailing-slash-plugin.js'
import { VitePWA } from 'vite-plugin-pwa'

const path = require('path')

const builtinsPlugin = { ...builtins({ crypto: true }), name: 'rollup-plugin-node-builtins' }

const build = {
    rollupOptions: {
        input: {
            'knowage-vue': new URL('./index.html', import.meta.url).href
        }
    }
}

export default defineConfig((command, mode) => {
    const env = loadEnv(mode, process.cwd())
    return {
        plugins: [
            vue(),
            builtinsPlugin,
            forwardToTrailingSlashPlugin(Object.keys(build.rollupOptions.input)),
            VitePWA({
                registerType: 'autoUpdate',
                devOptions: {
                    enabled: true
                },
                useCredentials: true,
                manifest: {
                    name: 'Knowage',
                    short_name: 'Knowage',
                    start_url: '.',
                    display: 'standalone',
                    scope: '/knowage-vue/',
                    orientation: 'landscape',
                    background_color: '#3b678c',
                    theme_color: '#3b678c',
                    description: 'The business intelligence open-source solution',
                    icons: [
                        {
                            src: 'icons/48.png',
                            sizes: '48x48',
                            type: 'image/png'
                        },
                        {
                            src: 'icons/72.png',
                            sizes: '72x72',
                            type: 'image/png'
                        },
                        {
                            src: 'icons/96.png',
                            sizes: '96x96',
                            type: 'image/png'
                        },
                        {
                            src: 'icons/144.png',
                            sizes: '144x144',
                            type: 'image/png'
                        },
                        {
                            src: 'icons/168.png',
                            sizes: '168x168',
                            type: 'image/png'
                        },
                        {
                            src: 'icons/192.png',
                            sizes: '192x192',
                            type: 'image/png'
                        }
                    ]
                }
            })
        ],
        define: {
            _KNOWAGE_VERSION: JSON.stringify(process.env.npm_package_version)
        },
        resolve: {
            extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
            alias: {
                '@': path.resolve(__dirname, './src')
            }
        },
        css: {
            preprocessorOptions: {
                scss: {
                    additionalData: '@import "@/assets/scss/main.scss";'
                }
            }
        },
        base: env.VITE_PUBLIC_PATH,
        build: {
            outDir: './src/main/webapp',
            sourcemap: true,
            rollupOptions: {
                output: {
                    chunkFileNames: 'assets/js/[name]-[hash].js',
                    entryFileNames: 'assets/js/[name]-[hash].js'
                }
            }
        },
        server: {
            port: 3000,
            host: 'localhost',
            https: env.VITE_HOST_HTTPS === 'true',
            proxy: {
                '^/knowagedossierengine/api': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowageqbeengine/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowagemeta/restful-services/1.0/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage/restful-services/[0-9].0': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage/restful-services/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage-api/api/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage/webSocket': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true,
                    ws: true
                },
                '^/knowage/servlet': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowagemeta': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowagecockpitengine/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowagewhatifengine/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage-data-preparation/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true,
                    ws: true
                },
                '^/knowagekpiengine/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                }
            }
        },
        preview: {
            port: 3000
        }
    }
})
