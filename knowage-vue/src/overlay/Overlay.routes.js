import genericFunctionalityRoutes from '@/overlay/GenericFunctionality/GenericFunctionality.routes.js'
import dataEntryRoutes from '@/overlay/DataEntry/DataEntry.routes.js'

const baseRoutes = genericFunctionalityRoutes.concat(dataEntryRoutes)

export default baseRoutes
