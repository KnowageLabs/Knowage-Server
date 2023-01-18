import { IWidget, IWidgetInteractions } from "../../Dashboard"
import { IImageWidgetImageSettings, IImageWidgetSettings } from "../../interfaces/DashboardImageWidget"
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
        image: getFormattedImageSettings(widget),
        style: getFormattedStyle(widget),
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IImageWidgetSettings
    return formattedSettings
}

const getFormattedImageSettings = (widget: any) => {
    // TODO - move to default
    const formattedImageSettings = {
        id: -1,
        style: imageWidgetDefaultValues.getdefaultImageStyleSettings()
    } as IImageWidgetImageSettings

    if (widget.style) {
        formattedImageSettings.style = {
            height: widget.style.heightPerc ? +widget.style.heightPerc : 50,
            width: widget.style.widthPerc ? +widget.style.widthPerc : 50,
            "background-position-x": widget.style.hAlign ?? 'center',
            "background-position-y": widget.style.vAlign ?? 'center',


        }
    }
    return formattedImageSettings
}