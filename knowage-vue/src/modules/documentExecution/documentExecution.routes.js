import documentExecutionMain from '@/modules/documentExecution/main/DocumentExecutionMain.routes'
import registryRoutes from '@/modules/documentExecution/registry/Registry.routes.js'
import documentDetailRoutes from '@/modules/documentExecution/documentDetails/DocumentDetails.routes.js'

const baseRoutes = []

const routes = baseRoutes
    .concat(documentExecutionMain)
    .concat(registryRoutes)
    .concat(documentDetailRoutes)

export default routes
