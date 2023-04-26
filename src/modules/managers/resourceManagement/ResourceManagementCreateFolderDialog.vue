<template>
    <Dialog class="kn-dialog--toolbar--primary createFolderDialog" :visible="visibility" footer="footer" :header="$t('managers.resourceManagement.createFolder')" :closable="false" modal>
        <Breadcrumb :home="home" :model="items"> </Breadcrumb>
        <div class="createFolderDialogContent">
            <span class="p-float-label">
                <InputText v-model="folderName" class="folderNameInputText" type="text" />
                <label class="kn-material-input-label" for="label">{{ $t('managers.resourceManagement.foldernamePlaceholder') }}</label>
            </span>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="folderName && folderName.length == 0" @click="emitCreateFolder"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Breadcrumb from 'primevue/breadcrumb'
import Dialog from 'primevue/dialog'
import resourceManagementDescriptor from './ResourceManagementDescriptor.json'

export default defineComponent({
    name: 'import-file-dialog',
    components: { Breadcrumb, Dialog },
    props: {
        visibility: Boolean,
        path: String
    },
    emits: ['update:visibility', 'createFolder'],
    data() {
        return {
            descriptor: resourceManagementDescriptor,
            home: { icon: 'pi pi-home' },
            items: [] as Array<{ label: string }>,
            folderName: '',
            loading: false
        }
    },
    watch: {
        path(oldPath, newPath) {
            if (oldPath != newPath) this.setBreadcrumbs()
        }
    },
    mounted() {
        this.setBreadcrumbs()
        this.folderName = ''
    },
    methods: {
        closeDialog(): void {
            this.folderName = ''
            this.$emit('update:visibility', false)
        },
        emitCreateFolder(): void {
            this.$emit('createFolder', this.folderName)
            this.folderName = ''
        },
        setBreadcrumbs() {
            this.folderName = ''
            this.items = []

            if (this.path) {
                const pathFolders = this.path.split('\\', -1)
                pathFolders.forEach((element) => {
                    const obj = { label: element }
                    this.items.push(obj)
                })
            }
        }
    }
})
</script>

<style lang="scss">
.createFolderDialog {
    min-width: 600px;
    width: 600px;
    max-width: 1200px;

    .p-fileupload-buttonbar {
        border: none;
        .p-button:not(.p-fileupload-choose) {
            display: none;
        }
    }
}
.p-breadcrumb {
    border: none;
    border-radius: 0;
    border-bottom: 1px solid var(--kn-list-border-color);

    cursor: default !important;

    &:deep(.p-menuitem-link) {
        cursor: default !important;
    }
}

.folderNameInputText {
    width: 100%;
}
.createFolderDialogContent {
    padding: 16px;
}
</style>
