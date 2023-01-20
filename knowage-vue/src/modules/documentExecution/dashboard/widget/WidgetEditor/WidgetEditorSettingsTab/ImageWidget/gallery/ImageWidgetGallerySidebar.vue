<template>
    <div v-if="image" id="image-widget-gallery-sidebar">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start> </template>
            <template #end>
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip="$t('common.close')" @click="$emit('close')" />
            </template>
        </Toolbar>
        <div class="p-m-4">
            <div class="p-m-4">
                <h3 class="p-m-0">{{ $t('common.name') }}</h3>
                <p class="p-m-0">{{ image.name }}</p>
            </div>

            <div class="p-m-4">
                <h3 class="p-m-0">{{ $t('common.url') }}</h3>
                <p class="p-m-0">{{ image.urlPreview }}</p>
            </div>

            <div class="p-m-4">
                <h3 class="p-m-0">{{ $t('common.size') }}</h3>
                <p class="p-m-0">{{ image.size + 'B' }}</p>
            </div>

            <div class="p-m-4">
                <h3 class="p-m-0">{{ $t('managers.resourceManagement.column.lastModified') }}</h3>
                <p class="p-m-0">{{ image.lastmod }}</p>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IImage } from '@/modules/documentExecution/dashboard/interfaces/DashboardImageWidget'

export default defineComponent({
    name: 'image-widget-gallery-sidebar',
    props: { selectedImage: { type: Object as PropType<IImage | null>, required: true } },
    emits: ['close'],
    data() {
        return {
            image: null as IImage | null
        }
    },
    watch: {
        selectedImage() {
            this.loadImage()
        }
    },
    created() {
        this.loadImage()
    },
    methods: {
        loadImage() {
            this.image = this.selectedImage
        }
    }
})
</script>

<style lang="scss" scoped>
#image-widget-gallery-sidebar {
    z-index: 150;
    background-color: white;
    height: 100%;
}
</style>
