import galleryManagementRoutes from '@/modules/managers/galleryManagement/GalleryManagement.routes.js'
import configurationManagementRoutes from '@/modules/managers/configurationManagement/ConfigurationManagement.routes.js'
import domainsManagementRoutes from '@/modules/managers/domainsManagement/DomainsManagement.routes.js'
import metadataManagementRoutes from '@/modules/managers/metadataManagement/MetadataManagement.routes.js'
import usersManagementRoutes from '@/modules/managers/usersManagement/UsersManagement.routes.js'
import profileAttributesRoutes from '@/modules/managers/profileAttributesManagement/ProfileAttributesManagement.routes.js'
import rolesManagementRoutes from '@/modules/managers/rolesManagement/RolesManagement.routes.js'
import cacheManagementRoutes from '@/modules/managers/cacheManagement/CacheManagement.routes.js'
import newsManagementRoutes from '@/modules/managers/newsManagement/NewsManagement.routes.js'
import tenantManagementRoutes from '@/modules/managers/tenantManagement/TenantManagement.routes.js'
import templatePruningRoutes from '@/modules/managers/templatePruning/TemplatePruning.routes.js'
import internationalizationManagementRoutes from '@/modules/managers/internationalizationManagement/InternationalizationManagement.routes.js'
import mondrianSchemasManagementRoutes from '@/modules/managers/mondrianSchemasManagement/MondrianSchemasManagement.routes.js'
import dataSourceRoutes from '@/modules/managers/dataSourceManagement/DataSourceManagement.routes.js'
import functionalitiesManagementRoutes from '@/modules/managers/functionalitiesManagement/FunctionalitiesManagement.routes.js'


const baseRoutes = []

const routes = baseRoutes
    .concat(galleryManagementRoutes)
    .concat(configurationManagementRoutes)
    .concat(domainsManagementRoutes)
    .concat(metadataManagementRoutes)
    .concat(usersManagementRoutes)
    .concat(profileAttributesRoutes)
    .concat(rolesManagementRoutes)
    .concat(cacheManagementRoutes)
    .concat(newsManagementRoutes)
    .concat(tenantManagementRoutes)
    .concat(templatePruningRoutes)
    .concat(internationalizationManagementRoutes)
    .concat(mondrianSchemasManagementRoutes)
    .concat(dataSourceRoutes)
    .concat(functionalitiesManagementRoutes)

export default routes
