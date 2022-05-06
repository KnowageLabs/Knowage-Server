import measureDefinitionRoutes from '@/modules/kpi/measureDefinition/MeasureDefinition.routes.js'
import targetDefinitionRoutes from '@/modules/kpi/targetDefinition/TargetDefinition.routes.js'
import kpiDefinitionRoutes from '@/modules/kpi/kpiDefinition/KpiDefinition.routes.js'
import kpiSchedulerRoutes from '@/modules/kpi/kpiScheduler/KpiScheduler.routes.js'
import alertDefinitionRoutes from '@/modules/kpi/alertDefinition/AlertDefinition.routes.js'
import kpiDocumentDesignerRoutes from '@/modules/kpi/kpiDocumentDesigner/KpiDocumentDesigner.routes.js'

const baseRoutes = []

const routes = baseRoutes
    .concat(measureDefinitionRoutes)
    .concat(targetDefinitionRoutes)
    .concat(kpiDefinitionRoutes)
    .concat(kpiSchedulerRoutes)
    .concat(alertDefinitionRoutes)
    .concat(kpiDocumentDesignerRoutes)

export default routes
