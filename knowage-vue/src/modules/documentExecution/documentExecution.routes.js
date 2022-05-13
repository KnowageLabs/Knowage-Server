import documentExecutionMain from '@/modules/documentExecution/main/DocumentExecutionMain.routes'
import registryRoutes from '@/modules/documentExecution/registry/Registry.routes.js'
import documentDetailRoutes from '@/modules/documentExecution/documentDetails/DocumentDetails.routes.js'
import dashboardRoutes from '@/modules/documentExecution/dashboard/Dashboard.routes.js'

const baseRoutes = []

const routes = baseRoutes
    .concat(documentExecutionMain)
    .concat(registryRoutes)
    .concat(documentDetailRoutes)
    .concat(dashboardRoutes)

export default routes
