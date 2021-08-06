import alertDefinition from '@/modules/kpi/alertDefinition/AlertDefinition.routes.js'
import targetDefinitionRoutes from '@/modules/kpi/targetDefinition/TargetDefinition.routes.js'
import kpiDefinitionRoutes from '@/modules/kpi/kpiDefinition/KpiDefinition.routes.js'

const baseRoutes = []

const routes = baseRoutes
    .concat(targetDefinitionRoutes)
    .concat(kpiDefinitionRoutes)
    .concat(alertDefinition)

export default routes
