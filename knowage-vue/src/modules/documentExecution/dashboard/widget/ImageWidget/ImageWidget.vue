<template>
    <div id="container" :style="imageUrl"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'

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
        imageUrl() {
            return {
                'background-size': this.width + ' ' + this.height,
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
