import dossierRoutes from '@/modules/documentExecution/dossier/Dossier.routes.js'
import registryRoutes from '@/modules/documentExecution/registry/Registry.routes.js'

const baseRoutes = []

const routes = baseRoutes.concat(dossierRoutes).concat(registryRoutes)

export default routes