import { ISelection } from "../../Dashboard"
import { formatDate } from "@/helpers/commons/localeHelper"
import dashboardDescriptor from '../../DashboardDescriptor.json'
import moment from "moment"

export const formatSelectionForDisplay = (selection: ISelection) => {
    if (!selection.value) return ''
    let result = ''
    for (let i = 0; i < selection.value.length; i++) {
        const tempValue = selection.value[i]
        if (moment(tempValue, dashboardDescriptor.selectionsDateFormat, true).isValid()) {
            result = formatDate(tempValue as string, '', dashboardDescriptor.selectionsDateFormat) + ' '
        } else {
            result = tempValue + ' '
        }
    }
    return result.trim()
}