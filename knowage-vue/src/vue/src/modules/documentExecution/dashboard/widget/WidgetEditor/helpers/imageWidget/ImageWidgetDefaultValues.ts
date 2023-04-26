import descriptor from './ImageWidgetDefaultValuesDescriptor.json'
import deepcopy from "deepcopy"

export const getdefaultImageStyleSettings = () => {
    return deepcopy(descriptor.defaultImageStyleSettings)
}
