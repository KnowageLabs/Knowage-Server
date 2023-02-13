import { ITableWidgetColumnStyles } from "@/modules/documentExecution/dashboard/Dashboard"
import deepcopy from "deepcopy"
import descriptor from './PivotTableDefaultValuesDescriptor.json'

export const getDefaultColumnStyles = () => {
    return deepcopy(descriptor.defaultColumnStyles) as ITableWidgetColumnStyles
}
