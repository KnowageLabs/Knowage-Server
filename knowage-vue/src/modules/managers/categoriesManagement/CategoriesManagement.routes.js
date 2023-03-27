const routes = [
    {
        path: '/categories-management',
        name: 'categories-management',
        component: () => import('@/modules/managers/categoriesManagement/CategoriesManagement.vue')
    }
]

export default routes
