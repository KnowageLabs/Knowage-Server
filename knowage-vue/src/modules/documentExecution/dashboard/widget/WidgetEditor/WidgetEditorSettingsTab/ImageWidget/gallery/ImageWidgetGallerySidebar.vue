<template>
    <div v-if="image" id="image-widget-gallery-sidebar">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                <Button icon="fas fa-clone" class="p-button-text p-button-rounded p-button-plain kn-button-light kn-cursor-pointer" @click="copyToBase64" />
            </template>
            <template #end>
                <Button v-tooltip="$t('common.close')" icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain kn-cursor-pointer" @click="$emit('close')" />
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
        },
        async copyToBase64() {
            if (!this.image) return
            this.toDataURL(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/images/getImage?IMAGES_ID=${this.image.imgId}`, async (dataUrl: string) => await navigator.clipboard.writeText(dataUrl))
        },
        toDataURL(url: string, callback: Function) {
            const xhr = new XMLHttpRequest()
            xhr.onload = () => {
                const reader = new FileReader()
                reader.onloadend = () => callback(reader.result)
                reader.readAsDataURL(xhr.response)
            }
            xhr.open('GET', url)
            xhr.responseType = 'blob'
            xhr.send()
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
