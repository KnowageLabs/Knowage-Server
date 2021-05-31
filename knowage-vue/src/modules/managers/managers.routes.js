import galleryManagementRoutes from '@/modules/managers/galleryManagement/GalleryManagement.routes.js'
import metadataManagementRoutes from '@/modules/managers/metadataManagement/MetadataManagement.routes.js'
import configurationManagementRoutes from '@/modules/managers/configurationManagement/ConfigurationManagement.routes.js'

const baseRoutes = []

const routes = baseRoutes.concat(galleryManagementRoutes).concat(metadataManagementRoutes).concat(configurationManagementRoutes)

export default routes
