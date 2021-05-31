import galleryManagementRoutes from '@/modules/managers/galleryManagement/GalleryManagement.routes.js'
import rolesManagementRoutes from '@/modules/managers/rolesManagement/RolesManagement.routes.js'

const baseRoutes = []

const routes = baseRoutes.concat(galleryManagementRoutes).concat(rolesManagementRoutes)

export default routes
