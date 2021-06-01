import galleryManagementRoutes from '@/modules/managers/galleryManagement/GalleryManagement.routes.js'
import mondrianSchemasManagementRoutes from '@/modules/managers/mondrianSchemasManagement/MondrianSchemasManagement.routes.js'

const baseRoutes = []

const routes = baseRoutes.concat(galleryManagementRoutes).concat(mondrianSchemasManagementRoutes)

export default routes
