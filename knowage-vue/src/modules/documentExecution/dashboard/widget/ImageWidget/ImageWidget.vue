<template>
    <div class="p-d-flex p-flex-column kn-flex kn-overflow-y dashboard-scrollbar">
        <img :src="imageUrl" alt="Image Widget" :style="{ height: height, width: width }" />
    </div>
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
            height: 'auto',
            width: 'auto'
        }
    },
    computed: {
        imageUrl() {
            return this.widgetModel.settings.configuration?.image?.id ? import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/images/getImage?IMAGES_ID=${this.widgetModel.settings.configuration.image.id}&preview=true` : ''
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
            console.log('------------- onRefreshImageWidget!!!!', this.widgetModel)
            if (widgetId && widgetId !== this.widgetModel.id) return
            console.log('------------- GOT HERE!!!!')
            this.height = this.widgetModel.settings.configuration.image.style.height
            this.width = this.widgetModel.settings.configuration.image.style.width
        }
    }
})
</script>
