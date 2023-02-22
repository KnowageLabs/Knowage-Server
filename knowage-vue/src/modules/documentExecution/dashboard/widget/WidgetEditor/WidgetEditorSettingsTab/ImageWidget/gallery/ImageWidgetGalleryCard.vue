<template>
    <div>
        <div class="card-container">
            <Toolbar class="kn-toolbar  p-p-0 p-m-0 p-col-12" :class="[isSelected ? 'kn-toolbar--primary' : 'kn-toolbar--secondary']">
                <template #start> {{ imageProp.name }} </template>
                <template #end>
                    <Button v-tooltip.top="$t('common.view')" icon="pi pi-eye" class="p-button-link" :class="[isSelected ? 'active-image-toolbar-buttons' : '']" @click="openImageSidebar" />
                    <Button v-tooltip.top="$t('common.delete')" icon="pi pi-trash" class="p-button-link" :class="[isSelected ? 'active-image-toolbar-buttons' : '']" @click="deleteImageConfirm" />
                </template>
            </Toolbar>
            <div class="p-d-flex p-flex-column p-jc-center p-ai-center kn-flex image-container" @click="$emit('imageSelected', imageProp)">
                <img class="card-image" :src="getImageUrl(imageProp)" :alt="imageProp.name" />
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IImage } from '@/modules/documentExecution/dashboard/interfaces/DashboardImageWidget'

export default defineComponent({
    name: 'image-widget-gallery-card',
    components: {},
    props: { imageProp: { type: Object as PropType<IImage>, required: true }, isSelected: { type: Boolean, required: true } },
    emits: ['delete', 'openSidebar', 'imageSelected'],
    data() {
        return {}
    },
    methods: {
        getImageUrl(image: IImage) {
            return import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/images/getImage?IMAGES_ID=${image.imgId}`
        },
        openImageSidebar() {
            this.$emit('openSidebar', this.imageProp)
        },
        deleteImageConfirm() {
            this.$confirm.require({
                header: this.$t('common.toast.deleteConfirmTitle'),
                message: this.$t('common.toast.deleteMessage'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('delete', this.imageProp)
            })
        }
    }
})
</script>
<style lang="scss" scoped>
.card-container {
    display: flex;
    flex-direction: column;
    position: relative;
    border-radius: 0;
    overflow: hidden;
    height: 200px;
    padding: 0;
    border: 1px solid var(--kn-color-borders);
    &:hover {
        background-color: var(--kn-color-secondary);
        transition: 0.3s;
    }
}

.card-image-container {
    background-color: rgb(236, 239, 241);
}

.card-image {
    max-width: 100%;
    height: auto;
}

.image-container {
    overflow: hidden;
}
.active-image-toolbar-buttons {
    color: white;
}
</style>
