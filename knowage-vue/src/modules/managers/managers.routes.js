import galleryManagementRoutes from '@/modules/managers/galleryManagement/GalleryManagement.routes.js'
import configurationManagementRoutes from '@/modules/managers/configurationManagement/ConfigurationManagement.routes.js'
import domainsManagementRoutes from '@/modules/managers/domainsManagement/DomainsManagement.routes.js'
import metadataManagementRoutes from '@/modules/managers/metadataManagement/MetadataManagement.routes.js'
import usersManagementRoutes from '@/modules/managers/usersManagement/UsersManagement.routes.js'
import profileAttributesRoutes from '@/modules/managers/profileAttributesManagement/ProfileAttributesManagement.routes.js'
import rolesManagementRoutes from '@/modules/managers/rolesManagement/RolesManagement.routes.js'
import cacheManagementRoutes from '@/modules/managers/cacheManagement/CacheManagement.routes.js'

const baseRoutes = []

const routes = baseRoutes.concat(galleryManagementRoutes)
  .concat(configurationManagementRoutes)
  .concat(domainsManagementRoutes)
  .concat(metadataManagementRoutes)
  .concat(usersManagementRoutes)
  .concat(profileAttributesRoutes)
  .concat(rolesManagementRoutes)
  .concat(cacheManagementRoutes)

export default routes
