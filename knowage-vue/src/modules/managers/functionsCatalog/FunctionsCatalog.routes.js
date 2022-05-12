const routes = [
    {
        path: '/functions-catalog',
        name: 'functions-catalog',
        component: () => import('@/modules/managers/functionsCatalog/FunctionsCatalog.vue')
    }
]

export default routes
