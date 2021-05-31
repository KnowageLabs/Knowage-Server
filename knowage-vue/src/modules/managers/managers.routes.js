import galleryManagementRoutes from '@/modules/managers/galleryManagement/GalleryManagement.routes.js'
import configurationManagementRoutes from '@/modules/managers/configurationManagement/ConfigurationManagement.routes.js'


const baseRoutes = [];

const routes = baseRoutes.concat(galleryManagementRoutes).concat(configurationManagementRoutes)

export default routes;