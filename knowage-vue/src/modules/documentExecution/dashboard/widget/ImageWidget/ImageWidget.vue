<template>
    <div id="container" class="kn-cursor-pointer" :style="imageUrl" @click="executeInteractions"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { executeImageWidgetCrossNavigation } from '../interactionsHelpers/InteractionHelper'

export default defineComponent({
    name: 'image-widget',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, dashboardId: { type: String, required: true }, editorMode: { type: Boolean } },
    emits: ['close'],
    data() {
        return {
            height: '',
            width: '',
            backgroundPositionX: 'center',
            backgroundPositionY: 'center'
        }
    },
    computed: {
        backgroundSize() {
            return this.width && this.height ? this.width + ' ' + this.height : 'contain'
        },
        imageUrl() {
            return {
                'background-size': this.backgroundSize,
                'background-position': this.backgroundPositionX + ' ' + this.backgroundPositionY,
                'background-image': `url(/knowage/restful-services/1.0/images/getImage?IMAGES_ID=${this.widgetModel.settings.configuration.image.id})`
            }
        }
    },
    created() {
        this.setEventListeners()
        this.onRefreshImageWidget()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('refreshImageWidget', this.onRefreshImageWidget)
        },
        removeEventListeners() {
            emitter.off('refreshImageWidget', this.onRefreshImageWidget)
        },
        onRefreshImageWidget(widgetId: any | null = null) {
            if (widgetId && widgetId !== this.widgetModel.id) return
            this.height = this.widgetModel.settings.configuration.image.style.height
            this.width = this.widgetModel.settings.configuration.image.style.width
            this.backgroundPositionX = this.widgetModel.settings.configuration.image.style['background-position-x']
            this.backgroundPositionY = this.widgetModel.settings.configuration.image.style['background-position-y']
        },
        executeInteractions() {
            const crossNavigation = this.widgetModel.settings.interactions.crossNavigation
            if (!crossNavigation.enabled) return
            executeImageWidgetCrossNavigation(crossNavigation, this.dashboardId)
        }
    }
})
</script>

<style lang="scss" scoped>
#container {
    outline: none;
    height: 100%;
    width: 100%;
    background-repeat: no-repeat;
    position: relative;
}
</style>
