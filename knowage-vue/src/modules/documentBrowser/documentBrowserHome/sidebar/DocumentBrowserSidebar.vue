<template>
    <div id="document-browser-sidebar">
        <Toolbar id="document-detail-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                <div id="document-icons-container" class="p-d-flex p-flex-row p-jc-around">
                    <i class="fa fa-play-circle document-pointer p-mx-4" v-tooltip.top="$t('documentBrowser.executeDocument')" @click="executeDocument" v-if="user?.functionalities.includes('DocumentUserManagement')" />
                    <template v-if="canEditDocument">
                        <i class="pi pi-pencil document-pointer p-mx-4" v-tooltip.top="$t('documentBrowser.editDocument')" @click="$emit('showDocumentDetails', document)" />
                        <i class="far fa-copy document-pointer p-mx-4" v-tooltip.top="$t('documentBrowser.cloneDocument')" @click="cloneDocumentConfirm" />
                        <i class="far fa-trash-alt document-pointer p-mx-4" v-tooltip.top="$t('documentBrowser.deleteDocument')" @click="deleteDocumentConfirm" v-if="user?.functionalities.includes('DocumentDeleteManagement')" />
                    </template>
                    <i v-if="user?.functionalities.includes('DocumentMoveUpState') && document.stateCode === 'TEST'" class="fa fa-arrow-up document-pointer p-mx-4" v-tooltip.left="$t('documentBrowser.moveUpDocumentState')" @click="changeStateDocumentConfirm('UP')" />
                    <i
                        v-if="user?.functionalities.includes('DocumentMoveDownState') && (document.stateCode === 'TEST' || document.stateCode === 'REL')"
                        class="fa fa-arrow-down document-pointer p-mx-4"
                        v-tooltip.left="$t('documentBrowser.moveDownDocumentState')"
                        @click="changeStateDocumentConfirm('DOWN')"
                    />
                </div>
            </template>
        </Toolbar>
        <div class="p-m-4">
            <div v-if="selectedDocument?.previewFile" class="p-text-center">
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
    props: { selectedDocument: { type: Object } },
    emits: ['documentCloneClick', 'documentDeleteClick', 'itemSelected', 'documentChangeStateClicked', 'showDocumentDetails'],
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
            return import.meta.env.VITE_HOST_URL + `/knowage/servlet/AdapterHTTP?ACTION_NAME=MANAGE_PREVIEW_FILE_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE&operation=DOWNLOAD&fileName=${this.selectedDocument?.previewFile}`
        },
        canEditDocument(): boolean {
            if (!this.user) return false
            switch (this.document.stateCode) {
                case 'TEST':
                    return this.user.functionalities.includes('DocumentTestManagement')
                case 'DEV':
                    return this.user.functionalities.includes('DocumentDevManagement')
                case 'REL':
                    return this.user.functionalities.includes('DocumentAdminManagement')
                case 'SUSPENDED':
                case 'SUSP':
                    return this.user.functionalities.includes('DocumentAdminManagement')
                default:
                    return false
            }
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
