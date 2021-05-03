<template>
    <Dialog class="kn-dialog--toolbar--primary importExportDialog" v-bind:visible="visibility" footer="footer" :header="$t('common.import')" :closable="false" modal>
        <FileUpload name="demo[]" :customUpload="true" @uploader="onUpload" auto="true" :maxFileSize="1000000">
            <template #empty>
                <p>Drag and drop files to here to upload.</p>
            </template>
        </FileUpload>
        <template #footer>
            <Button class="p-button-text kn-button" v-t="'common.close'" @click="closeDialog" />
            <Button class="kn-button kn-button--primary" v-t="'common.import'" @click="saveImport" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import FileUpload from 'primevue/fileupload'

export default defineComponent({
    name: 'import-dialog',
    components: { Dialog, FileUpload },
    props: {
        visibility: Boolean
    },
    data() {
        return {}
    },
    created() {},
    emits: ['update:visibility'],
    methods: {
        closeDialog(): void {
            this.$emit('update:visibility', false)
        },
        onUpload(upload) {
            console.log(upload.files[0])
        },
        saveImport(): void {
            console.log('ok')
        }
    }
})
</script>
<style lang="scss">
.importExportDialog {
    min-width: 600px;
    width: 60%;
    max-width: 1200px;
    .p-fileupload-buttonbar {
        border: none;
        .p-button:not(.p-fileupload-choose) {
            display: none;
        }
    }
}
</style>
