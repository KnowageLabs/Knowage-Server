import galleryManagementRoutes from '@/modules/managers/galleryManagement/GalleryManagement.routes.js'
import configurationManagementRoutes from '@/modules/managers/configurationManagement/ConfigurationManagement.routes.js'
import domainsManagementRoutes from '@/modules/managers/domainsManagement/DomainsManagement.routes.js'
import metadataManagementRoutes from '@/modules/managers/metadataManagement/MetadataManagement.routes.js'

const baseRoutes = []

const routes = baseRoutes.concat(galleryManagementRoutes)
  .concat(configurationManagementRoutes)
  .concat(domainsManagementRoutes)
  .concat(metadataManagementRoutes)

export default routes
