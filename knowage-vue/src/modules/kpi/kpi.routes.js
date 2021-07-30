import measureDefinitionRoutes from '@/modules/kpi/measureDefinition/MeasureDefinition.routes.js'
import targetDefinitionRoutes from '@/modules/kpi/targetDefinition/TargetDefinition.routes.js'

const baseRoutes = []

const routes = baseRoutes
  .concat(measureDefinitionRoutes)
  .concat(targetDefinitionRoutes)

export default routes
