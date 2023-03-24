import { IVegaChartsSignal, IVegaChartsData, IVegaChartsScale, IVegaChartsMark, IVegaChartsNoDataConfiguration, IVegaChartsTextConfiguration } from './../../../../../interfaces/vega/VegaChartsWidget.d';
import descriptor from './VegaDefaultValuesDescriptor.json'
import deepcopy from "deepcopy"

export const getDefaultVegaTextConfiguration = () => {
    return deepcopy(descriptor.defaultVegaTextConfiguration) as IVegaChartsTextConfiguration
}

export const getDefaultVegaNoDataConfiguration = () => {
    return deepcopy(descriptor.defaultVegaNoDataConfiguration) as IVegaChartsNoDataConfiguration
}

export const getDefaultVegaSchema = () => {
    return deepcopy(descriptor.defaultVegaSchema) as string
}

export const getDefaultVegaDescription = () => {
    return deepcopy(descriptor.defaultVegaDescription) as string
}

export const getDefaultVegaPadding = () => {
    return deepcopy(descriptor.defaultVegaPadding) as number
}

export const getDefaultVegaAutosize = () => {
    return deepcopy(descriptor.defaultVegaAutosize) as { type: string, contains: string }
}

export const getDefaultVegaSignals = () => {
    return deepcopy(descriptor.defaultVegaSignals) as IVegaChartsSignal[]
}

export const getDefaultVegaData = () => {
    return deepcopy(descriptor.defaultVegaData) as IVegaChartsData[]
}

export const getDefaultVegaScales = () => {
    return deepcopy(descriptor.defaultVegaScales) as IVegaChartsScale[]
}

export const getDefaultVegaMarks = () => {
    return deepcopy(descriptor.defaultVegaMarks) as IVegaChartsMark[]
}