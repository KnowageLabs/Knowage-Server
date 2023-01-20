<template>
    <div v-if="widgetModel" class="dashboard-card-shadow kn-height-full p-ml-1 p-d-flex p-flex-row">
        <div class="p-grid p-m-2 kn-flex kn-overflow dashboard-scrollbar">
            <Message v-if="images.length == 0" class="kn-flex p-m-2" severity="info" :closable="false">
                {{ $t('common.info.noDataFound') }}
            </Message>
            <div v-else id="image-widget-gallery-content" class="p-grid p-m-2 kn-flex kn-overflow dashboard-scrollbar">
                <div v-if="selectedImage" id="image-widget-gallery-backdrop" class="kn-flex" @click="selectedImage = null"></div>
                <div class="p-col-12 p-d-flex p-jc-center">
                    <Button icon="fas fa-upload fa-1x" class="p-button-text p-button-plain p-ml-2" @click="setImageUploadType" />
                    <KnInputFile :changeFunction="setImageForUpload" accept=".png, .jpg, .jpeg" :triggerInput="triggerImageUpload" />
                </div>
                <ImageWidgetGalleryCard v-for="(image, index) of images" :key="index" class="kn-cursor-pointer" :imageProp="image" @click="setSelectedImage(image)" @delete="onImageDelete" />

                <div v-if="selectedImage" id="image-widget-gallery-card-sidebar-container">
                    <ImageWidgetGallerySidebar :selectedImage="selectedImage" @close="selectedImage = null"></ImageWidgetGallerySidebar>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { IImage } from '@/modules/documentExecution/dashboard/interfaces/DashboardImageWidget'
import { mapActions } from 'pinia'
import { AxiosResponse } from 'axios'
import appStore from '@/App.store'
import descriptor from './ImageWidgetGalleryDescriptor.json'
import ImageWidgetGalleryCard from './ImageWidgetGalleryCard.vue'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import Message from 'primevue/message'
import ImageWidgetGallerySidebar from './ImageWidgetGallerySidebar.vue'

export default defineComponent({
    name: 'image-widget-gallery',
    components: { ImageWidgetGalleryCard, KnInputFile, Message, ImageWidgetGallerySidebar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, imagesListProp: { type: Array as PropType<IImage[]>, required: true } },
    emits: ['uploadedImage'],
    data() {
        return {
            descriptor,
            images: [] as IImage[],
            triggerImageUpload: false,
            selectedImage: null as IImage | null
        }
    },
    watch: {
        imagesListProp() {
            this.loadImages()
        }
    },
    created() {
        this.loadImages()
    },
    methods: {
        ...mapActions(appStore, ['setInfo', 'setError', 'setLoading']),
        loadImages() {
            this.images = this.imagesListProp
        },
        setImageUploadType() {
            this.triggerImageUpload = false
            setTimeout(() => (this.triggerImageUpload = true), 200)
        },
        async setImageForUpload(event: any) {
            this.setLoading(true)
            const imageToUpload = event.target.files[0]

            if (imageToUpload) {
                var formData = new FormData()
                formData.append('uploadedImage', imageToUpload)
                await this.$http
                    .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/images/addImage`, formData, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' } })
                    .then((response: AxiosResponse<any>) => {
                        if (response.data.success) {
                            this.$emit('uploadedImage')
                            this.setInfo({ title: this.$t('common.toast.success'), msg: this.$t('common.toast.uploadSuccess') })
                        } else this.onImageUploadError(response.data.msg)
                    })
                    .catch(() => this.onImageUploadError(''))
            }
            this.setLoading(false)
        },
        onImageUploadError(errorMessageKey: string) {
            let message = this.$t('documentExecution.documentDetails.info.imageUploadError')
            switch (errorMessageKey) {
                case 'sbi.cockpit.widgets.image.imageWidgetDesigner.tooBigImage':
                    message = this.$t('dashboard.widgetEditor.imageWidget.imageUploadErrors.tooBigImage')
                    break
                case 'sbi.cockpit.widgets.image.imageWidgetDesigner.tooImageForTenant':
                    message = this.$t('dashboard.widgetEditor.imageWidget.imageUploadErrors.noMoreImages')
                    break
                case 'sbi.cockpit.widgets.image.imageWidgetDesigner.tooImageForUser':
                    message = this.$t('dashboard.widgetEditor.imageWidget.imageUploadErrors.tooManyUserImages')
                    break
                case 'sbi.cockpit.widgets.image.imageWidgetDesigner.fileIsNotAnImage':
                    message = this.$t('dashboard.widgetEditor.imageWidget.imageUploadErrors.fileIsNotAnImage')
                    break
                case 'sbi.cockpit.widgets.image.imageWidgetDesigner.fileTypeIsNotAllowed':
                    message = this.$t('dashboard.widgetEditor.imageWidget.imageUploadErrors.fileTypeIsNotAllowed')
            }
            this.setError({ title: this.$t('common.toast.errorTitle'), msg: message })
        },
        async onImageDelete(image: IImage) {
            await this.deleteImage(image)
        },
        async deleteImage(image: IImage) {
            this.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/images/deleteImage?imageId=${image.imgId}`)
                .then((response: AxiosResponse<any>) => {
                    if (response.data.success) this.onSuccessfullImageDelete(image)
                    else this.setError({ title: this.$t('common.toast.deleteTitle'), msg: response.data.msg })
                })
                .catch(() => {})
            this.setLoading(false)
        },
        onSuccessfullImageDelete(image: IImage) {
            const index = this.images.findIndex((tempImage: IImage) => tempImage.imgId === image.imgId)
            if (index !== -1) {
                this.images.splice(index, 1)
                this.setInfo({ title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
            }
        },
        setSelectedImage(image: IImage) {
            this.selectedImage = image
        }
    }
})
</script>

<style lang="scss" scoped>
.gallery-card {
    height: 200px;
    width: 200px;
}
.gallery-card:hover {
    border-color: #43749e !important;
}

#image-widget-gallery-card-sidebar-container {
    position: absolute;
    max-width: 350px;
    height: 100%;
    right: 0;
    top: 0;
    z-index: 150;
}

#image-widget-gallery-backdrop {
    background-color: rgba(33, 33, 33, 1);
    opacity: 0.48;
    z-index: 50;
    position: absolute;
    width: 100%;
    min-height: 100%;
    top: 0;
    left: 0;
}

#image-widget-gallery-content {
    position: relative;
}
</style>
