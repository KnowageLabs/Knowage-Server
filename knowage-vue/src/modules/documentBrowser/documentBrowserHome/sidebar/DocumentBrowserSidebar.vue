<template>
    <div id="document-browser-sidebar">
        <Toolbar id="document-detail-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                <div id="document-icons-container" class="p-d-flex p-flex-row p-jc-around ">
                    <i class="fa fa-play-circle document-pointer p-mx-4" v-tooltip.top="$t('documentBrowser.executeDocument')" @click="executeDocument" />
                    <template v-if="isSuperAdmin">
                        <i class="pi pi-pencil document-pointer p-mx-4" v-tooltip.top="$t('documentBrowser.editDocument')" @click="editDocument" />
                        <i class="far fa-copy document-pointer p-mx-4" v-tooltip.top="$t('documentBrowser.cloneDocument')" @click="cloneDocumentConfirm" />
                        <i class="far fa-trash-alt document-pointer p-mx-4" v-tooltip.top="$t('documentBrowser.deleteDocument')" @click="deleteDocumentConfirm" />
                        <i v-if="document.stateCode === 'TEST'" class="fa fa-arrow-up document-pointer p-mx-4" v-tooltip.left="$t('documentBrowser.moveUpDocumentState')" @click="changeStateDocumentConfirm('UP')" />
                        <i v-if="document.stateCode === 'TEST' || document.stateCode === 'REL'" class="fa fa-arrow-down document-pointer p-mx-4" v-tooltip.left="$t('documentBrowser.moveDownDocumentState')" @click="changeStateDocumentConfirm('DOWN')" />
                    </template>
                </div>
            </template>
        </Toolbar>
        <div class="p-m-4">
            <div v-if="selectedDocument.previewFile" class="p-text-center">
                <img id="image-preview" :src="getImageUrl" />
            </div>

            <div v-if="document.functionalities && document.functionalities.length > 0" class="p-m-4">
                <h3 class="p-m-0">{{ $t('common.path') }}</h3>
                <p v-for="(path, index) in document.functionalities" :key="index" class="p-m-0">{{ path }}</p>
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
                <h3 class="p-m-0">{{ $t('common.state') }}</h3>
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
                <p class="p-m-0">{{ document.visible ? $t('common.visible') : $t('common.notVisible') }}</p>
            </div>
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
    emits: ['documentCloneClick', 'documentDeleteClick', 'itemSelected', 'documentChangeStateClicked'],
    data() {
        return {
            document: null as any,
            user: null as any
        }
    },
    watch: {
        selectedDocument() {
            this.loadDocument()
        }
    },
    computed: {
        isSuperAdmin(): boolean {
            return this.user?.isSuperadmin
        },
        getImageUrl(): string {
            return process.env.VUE_APP_HOST_URL + `/knowage/servlet/AdapterHTTP?ACTION_NAME=MANAGE_PREVIEW_FILE_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE&operation=DOWNLOAD&fileName=${this.selectedDocument?.previewFile}`
        }
    },
    created() {
        this.loadDocument()
        this.user = (this.$store.state as any).user
    },
    methods: {
        loadDocument() {
            this.document = this.selectedDocument
        },
        getFormatedDate(date: any) {
            return formatDate(date, 'MMM DD, YYYY h:mm:ss A')
        },
        cloneDocumentConfirm() {
            this.$confirm.require({
                header: this.$t('common.toast.cloneConfirmTitle'),
                accept: () => this.$emit('documentCloneClick', this.document)
            })
        },
        deleteDocumentConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('documentDeleteClick', this.document)
            })
        },
        changeStateDocumentConfirm(direction: string) {
            this.$confirm.require({
                message: this.$t('documentBrowser.changeStateMessage'),
                header: this.$t('documentBrowser.changeStateTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('documentChangeStateClicked', { document: this.document, direction: direction })
            })
        },
        executeDocument() {
            this.$emit('itemSelected', { item: this.document, mode: 'execute' })
        },
        editDocument() {
            this.$router.push(`/documentBrowser/editDocument/${this.document.id}`)
        }
    }
})
</script>

<style lang="scss" scoped>
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

#document-browser-sidebar {
    z-index: 150;
    background-color: white;
    height: 100%;
}
</style>
