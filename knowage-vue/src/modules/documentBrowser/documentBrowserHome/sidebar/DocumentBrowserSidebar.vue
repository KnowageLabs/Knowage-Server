<template>
    <Toolbar id="document-detail-toolbar" class="kn-toolbar kn-toolbar--secondary">
        <template #left>
            <div id="document-icons-container" class="p-d-flex p-flex-row p-jc-around ">
                <i class="fa fa-play-circle document-pointer p-mx-4" />
                <i class="pi pi-pencil document-pointer p-mx-4" />
                <i class="far fa-copy document-pointer p-mx-4" @click="cloneDocument" />
                <i class="far fa-trash-alt document-pointer p-mx-4" />
            </div>
        </template>
    </Toolbar>
    <div class="p-m-4">
        <div v-if="selectedDocument.previewFile" class="p-text-center">
            <img id="image-preview" :src="getImageUrl()" />
        </div>

        <div v-if="document.name" class="p-m-4">
            <h3 class="p-m-0">{{ $t('common.name') }}</h3>
            <p class="p-m-0">{{ document.name }}</p>
        </div>
        <div v-if="document.label" class="p-m-4">
            <h3 class="p-m-0">{{ $t('common.label') }}</h3>
            <p class="p-m-0">{{ document.label }}</p>
        </div>
        <div v-if="document.creationUser" class="p-m-4">
            <h3 class="p-m-0">{{ $t('common.author') }}</h3>
            <p class="p-m-0">{{ document.creationUser }}</p>
        </div>
        <div v-if="document.description" class="p-m-4">
            <h3 class="p-m-0">{{ $t('common.description') }}</h3>
            <p class="p-m-0">{{ document.description }}</p>
        </div>
        <div v-if="document.stateCodeStr" class="p-m-4">
            <h3 class="p-m-0">{{ $t('documentBrowser.state') }}</h3>
            <p class="p-m-0">{{ document.stateCodeStr }}</p>
        </div>
        <div v-if="document.typeCode" class="p-m-4">
            <h3 class="p-m-0">{{ $t('common.type') }}</h3>
            <p class="p-m-0">{{ document.typeCode }}</p>
        </div>
        <div v-if="document.creationDate" class="p-m-4">
            <h3 class="p-m-0">{{ $t('common.creationDate') }}</h3>
            <p class="p-m-0">{{ getFormatedDate(document.creationDate) }}</p>
        </div>
        <div v-if="document.visible" class="p-m-4">
            <h3 class="p-m-0">{{ $t('common.visibility') }}</h3>
            <p class="p-m-0">{{ document.visible ? $t('documentBrowser.visible') : $t('documentBrowser.notVisible') }}</p>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { formatDate } from '@/helpers/commons/localeHelper'

export default defineComponent({
    name: 'document-browser-sidebar',
    components: {},
    props: { selectedDocument: { type: Object } },
    emits: ['documentCloneClick'],
    data() {
        return {
            document: null as any
        }
    },
    watch: {
        selectedDocument() {
            this.loadDocument()
        }
    },
    created() {
        this.loadDocument()
    },
    methods: {
        loadDocument() {
            this.document = this.selectedDocument
            console.log('LOADED DOCUMENT: ', this.document)
        },
        getFormatedDate(date: any) {
            return formatDate(date, 'MMM DD, YYYY h:mm:ss A')
        },
        getImageUrl() {
            return process.env.VUE_APP_HOST_URL + `/knowage/servlet/AdapterHTTP?ACTION_NAME=MANAGE_PREVIEW_FILE_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE&operation=DOWNLOAD&fileName=${this.selectedDocument?.previewFile}`
        },
        cloneDocument() {
            this.$emit('documentCloneClick', this.document)
        }
    }
})
</script>

<style lang="scss">
#document-detail-toolbar .p-toolbar-group-left {
    width: 100%;
}

#document-icons-container {
    width: 100%;
}

.document-pointer:hover {
    cursor: pointer;
}

#image-preview {
    max-width: 100%;
    max-height: 200px;
}
</style>
