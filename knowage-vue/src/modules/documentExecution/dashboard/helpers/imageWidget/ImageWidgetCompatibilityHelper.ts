import { IWidget, IWidgetExports, IWidgetInteractions } from "../../Dashboard"
import { IImageWidgetImageSettings, IImageWidgetSettings, IImageWidgetConfiguration } from "../../interfaces/DashboardImageWidget"
import { getFormattedInteractions } from "../common/WidgetInteractionsHelper"
import { getFormattedStyle } from "./ImageWidgetStyleHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as  imageWidgetDefaultValues from '../../widget/WidgetEditor/helpers/imageWidget/ImageWidgetDefaultValues'

export const formatImageWidget = (widget: any) => {
    console.log(">>>>>>>> LOAEDED OLD WIDGET: ", widget)

    const formattedWidget = {
        id: widget.id,
        dataset: null,
        type: widget.type,
        columns: [],
        theme: '',
        settings: {} as IImageWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget) as IImageWidgetSettings

    console.log(">>>>>>>> FORMATTED NEW WIDGET: ", formattedWidget)

    return formattedWidget
}


const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        configuration: getFormattedConfiguration(widget),
        style: getFormattedStyle(widget),
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IImageWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return { image: getFormattedImageSettings(widget), exports: { showExcelExport: widget.style?.showExcelExport ?? false } as IWidgetExports } as IImageWidgetConfiguration
}


const getFormattedImageSettings = (widget: any) => {
    const formattedImageSettings = {
        id: widget.content.imgId ?? -1,
        style: imageWidgetDefaultValues.getdefaultImageStyleSettings()
    } as IImageWidgetImageSettings

    if (widget.style) {
        formattedImageSettings.style = {
            height: widget.style.heightPerc ?? '',
            width: widget.style.widthPerc ?? '',
            "background-position-x": widget.style.hAlign ?? 'center',
            "background-position-y": widget.style.vAlign ?? 'center',


        }
    }
    return formattedImageSettings
}