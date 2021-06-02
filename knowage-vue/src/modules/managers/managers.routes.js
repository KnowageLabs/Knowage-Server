import galleryManagementRoutes from '@/modules/managers/galleryManagement/GalleryManagement.routes.js'
import newsManagementRoutes from '@/modules/managers/newsManagement/NewsManagement.routes.js'

const baseRoutes = []

const routes = baseRoutes.concat(galleryManagementRoutes).concat(newsManagementRoutes)

export default routes
