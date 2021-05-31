import galleryManagementRoutes from '@/modules/managers/galleryManagement/GalleryManagement.routes.js'
import metadataManagementRoutes from '@/modules/managers/metadataManagement/MetadataManagement.routes.js'

const baseRoutes = []

const routes = baseRoutes.concat(galleryManagementRoutes).concat(metadataManagementRoutes)

export default routes
