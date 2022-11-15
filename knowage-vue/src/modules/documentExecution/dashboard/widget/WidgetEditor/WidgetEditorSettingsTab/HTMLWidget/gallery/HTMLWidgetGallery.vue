<template>
    <div class="dashboard-card-shadow kn-height-full p-ml-1 p-d-flex p-flex-column">
        <div class="p-d-flex p-flex-row p-ai-center p-flex-wrap">
            <InputText class="kn-material-input p-m-3 model-search" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchItems" />
        </div>
        <div class="p-grid p-m-2 kn-flex kn-overflow">
            <Message v-if="htmlGalleryProp.length == 0" class="kn-flex p-m-2" severity="info" :closable="false">
                {{ $t('common.info.noDataFound') }}
            </Message>
            <template v-else>
                <GalleryCard v-for="(galleryItem, index) of filteredGallery" :key="index" :widgetModel="widgetModel" :htmlGalleryItemProp="galleryItem" @click="checkForTemplateContent(galleryItem)" />
            </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IGalleryItem } from '@/modules/documentExecution/Dashboard/Dashboard'
import Message from 'primevue/message'
import GalleryCard from './HTMLWidgetGalleryCard.vue'

export default defineComponent({
    name: 'html-widget-settings-container',
    components: { Message, GalleryCard },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        htmlGalleryProp: { type: Array as PropType<IGalleryItem[]>, required: true }
    },
    data() {
        return {
            htmlEditor: '',
            cssEditor: '',
            searchWord: '',
            filteredGallery: [] as IGalleryItem[]
        }
    },
    watch: {},
    created() {
        this.loadWidgetEditors()
        this.filteredGallery = [...this.htmlGalleryProp] as IGalleryItem[]
    },
    methods: {
        loadWidgetEditors() {
            if (!this.widgetModel) return
            this.htmlEditor = this.widgetModel.settings.editor.html
            this.cssEditor = this.widgetModel.settings.editor.css
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredGallery = [...this.htmlGalleryProp] as IGalleryItem[]
                } else {
                    this.filteredGallery = this.htmlGalleryProp.filter((el: IGalleryItem) => {
                        return el.label?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || this.galleryHasTag(el)
                    })
                }
            }, 250)
        },
        galleryHasTag(galleryItem: IGalleryItem) {
            let tagFound = false
            for (let i = 0; i < galleryItem.tags.length; i++) {
                const tempTag = galleryItem.tags[i]
                if (tempTag.toLowerCase().includes(this.searchWord.toLowerCase())) {
                    tagFound = true
                    break
                }
            }
            return tagFound
        },
        checkForTemplateContent(galleryItem: IGalleryItem) {
            if (this.widgetModel.settings.editor.html > 0 || this.widgetModel.settings.editor.css > 0) {
                this.$confirm.require({
                    message: this.$t('documentExecution.dossier.deleteConfirm'),
                    header: this.$t('documentExecution.dossier.deleteTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.loadGalleryItem(galleryItem)
                })
            } else this.loadGalleryItem(galleryItem)
        },
        loadGalleryItem(galleryItem: IGalleryItem) {
            console.log(galleryItem)
        }
    }
})
</script>

<style lang="scss" scoped>
::-webkit-scrollbar {
    width: 5px;
}
::-webkit-scrollbar-track {
    background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
    background: #888;
}
::-webkit-scrollbar-thumb:hover {
    background: #555;
}
</style>
