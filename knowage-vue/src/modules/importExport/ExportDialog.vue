<template>
    <Dialog class="kn-dialog--toolbar--primary exportDialog" v-bind:visible="visibility" :closable="false" modal>
        <template #header>
            <h3>{{ $t('common.export') }}</h3>
        </template>
        <div class="exportDialogContent">
            <div class="p-field">
                <InputText class="kn-material-input fileNameInputText" type="text" v-model="fileName" maxlength="50" :placeholder="$t('importExport.filenamePlaceholder')" />
            </div>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="closeDialog" />
            <Button class="kn-button kn-button--primary" :label="$t('common.export')" autofocus :disabled="fileName.length == 0" @click="emitExport" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    name: 'export-dialog',
    components: { Dialog },
    props: {
        visibility: Boolean
    },
    data() {
        return {}
    },
    created() {},
    emits: ['update:visibility', 'export'],
    methods: {
        closeDialog(): void {
            this.$emit('update:visibility', false)
        },
        emitExport(): void {
            this.$emit('export')
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
