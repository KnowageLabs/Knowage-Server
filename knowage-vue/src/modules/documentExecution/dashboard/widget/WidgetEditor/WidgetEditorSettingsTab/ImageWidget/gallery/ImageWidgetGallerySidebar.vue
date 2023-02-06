<template>
    <div v-if="image" id="image-widget-gallery-sidebar">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                <Button icon="fas fa-clone" class="p-button-text p-button-rounded p-button-plain kn-button-light kn-cursor-pointer" @click="copyToBase64" />
            </template>
            <template #end>
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain kn-cursor-pointer" v-tooltip="$t('common.close')" @click="$emit('close')" />
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

            <div :style="testUrl"></div>
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
    computed: {
        testUrl() {
            return {
                'background-image': `url('data:image/png;base64,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');`
            }
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
