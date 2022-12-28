import { IDashboardDatasetDriver } from "@/modules/documentExecution/dashboard/Dashboard"
import moment from "moment"
import i18n from '@/App.i18n'
import { luxonFormatDate } from "@/helpers/commons/localeHelper"

const { t } = i18n.global

export const getFormattedDrivers = (drivers: IDashboardDatasetDriver[]) => {
    const formattedDrivers = {} as any
    drivers.forEach((driver: IDashboardDatasetDriver) => {
        if (driver.typeCode === 'MAN_IN' && (driver.type === 'NUM' || driver.type === 'STRING')) {
            driver.type === 'NUM' ? getFormattedManualNumberDriver(driver, formattedDrivers) : getFormattedManualStringDriver(driver, formattedDrivers)
        } else if (driver.type === 'DATE') {
            getFormattedDateDriver(driver, formattedDrivers)
        } else {
            getFormattedOtherDrivers(driver, formattedDrivers)
        }
    })
    return formattedDrivers
}

const getFormattedManualStringDriver = (driver: any, formattedDrivers: any) => {
    formattedDrivers[driver.urlName] = driver.parameterValue[0] ? driver.parameterValue[0].value : ''
    formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0] ? driver.parameterValue[0].description : ''
}

const getFormattedManualNumberDriver = (driver: any, formattedDrivers: any) => {
    formattedDrivers[driver.urlName] = driver.parameterValue[0] && driver.parameterValue[0].value ? +driver.parameterValue[0].value : ''
    formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0] ? driver.parameterValue[0].description : ''
}
const getFormattedDateDriver = (driver: any, formattedDrivers: any) => {
    const formattedDate = moment(driver.parameterValue[0].value).format('MMM DD, YYYY')
    const formattedDate2 = luxonFormatDate(driver.parameterValue[0].value, 'MMM DD, YYYY')
    formattedDrivers[driver.urlName] = formattedDate
    formattedDrivers[driver.urlName + '_field_visible_description'] = formattedDate
}


const getFormattedOtherDrivers = (driver: any, formattedDrivers: any) => {
    if (driver.multivalue) {
        const driverValues = [] as string[]
        const driverDescriptions = [] as string[]
        driver.parameterValue.forEach((parameterValue: { value: string; description: string }) => {
            driverValues.push(parameterValue.value)
            driverDescriptions.push(parameterValue.description)
        })
        formattedDrivers[driver.urlName] = driverValues
        formattedDrivers[driver.urlName + '_field_visible_description'] = driverDescriptions.join(';')
    } else {
        formattedDrivers[driver.urlName] = driver.parameterValue[0].value
        formattedDrivers[driver.urlName + '_field_visible_description'] = driver.parameterValue[0].description
    }
}

export const getUserRole = (user: any) => {
    if (user.sessionRole && user.sessionRole !== t('role.defaultRolePlaceholder')) {
        return user.sessionRole
    } else if (user.defaultRole) {
        return user.defaultRole
    } else if (user.roles.length > 0) {
        return user.roles[0]
    } else {
        return ''
    }
}
