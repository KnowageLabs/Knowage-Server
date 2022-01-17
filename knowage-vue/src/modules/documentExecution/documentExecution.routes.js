import documentExecutionMain from '@/modules/documentExecution/main/DocumentExecutionMain.routes'
import registryRoutes from '@/modules/documentExecution/registry/Registry.routes.js'

const baseRoutes = []

const routes = baseRoutes.concat(documentExecutionMain).concat(registryRoutes)

export default routes
