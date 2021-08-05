import alert from '@/modules/kpi/alert/Alert.routes.js'
import measureDefinitionRoutes from '@/modules/kpi/measureDefinition/MeasureDefinition.routes.js'
import targetDefinitionRoutes from '@/modules/kpi/targetDefinition/TargetDefinition.routes.js'
import kpiDefinitionRoutes from '@/modules/kpi/kpiDefinition/KpiDefinition.routes.js'

const baseRoutes = []

const routes = baseRoutes
    .concat(measureDefinitionRoutes)
    .concat(targetDefinitionRoutes)
    .concat(kpiDefinitionRoutes)
    .concat(alert)

export default routes
