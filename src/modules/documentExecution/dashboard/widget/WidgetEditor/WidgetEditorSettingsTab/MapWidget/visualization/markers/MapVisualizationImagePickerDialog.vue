<template>
    <Dialog class="kn-dialog--toolbar--primary" :visible="visible" :header="$t('dashboard.widgetEditor.map.imagesGallery')" :style="descriptor.style.imagesDialog" :closable="false" modal :breakpoints="descriptor.style.imagesDialogBreakpoints">
        <ImageWidgetGallery :widget-model="null" :images-list-prop="imagesList" mode="map" @uploadedImage="loadImages(true)" @selectedImage="onSelectedImage" @imageDeleted="loadImages(true)"></ImageWidgetGallery>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="setImage">{{ $t('common.set') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IImage } from '@/modules/documentExecution/dashboard/interfaces/DashboardImageWidget'
import { mapActions } from 'pinia'
import { AxiosResponse } from 'axios'
import appStore from '@/App.store'
import Dialog from 'primevue/dialog'
import descriptor from '../MapVisualizationTypeDescriptor.json'
import ImageWidgetGallery from '../../../ImageWidget/gallery/ImageWidgetGallery.vue'

export default defineComponent({
    name: 'map-visualization-image-picker-dialog',
    components: { Dialog, ImageWidgetGallery },
    props: { visible: { required: true, type: Boolean } },
    emits: ['close', 'setImage'],
    data() {
        return {
            descriptor,
            imagesList: [] as IImage[],
            selectedImage: null as IImage | null
        }
    },
    watch: {},
    created() {
        this.loadImages(true)
    },
    methods: {
        ...mapActions(appStore, ['setLoading']),
        async loadImages(reload: boolean) {
            if (this.imagesList.length > 0 && !reload) return
            this.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/images/listImages`)
                .then((response: AxiosResponse<any>) => (this.imagesList = response.data ? response.data.data : []))
                .catch(() => {})
            this.setLoading(false)
        },
        onSelectedImage(image: IImage) {
            this.selectedImage = image
        },
        setImage() {
            this.$emit('setImage', this.selectedImage)
            this.selectedImage = null
        },
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss" scoped>
#add-range-button {
    font-size: 0.8rem;
}
</style>
