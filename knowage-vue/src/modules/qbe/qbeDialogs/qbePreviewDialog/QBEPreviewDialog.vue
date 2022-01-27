<template>
    <Dialog id="qbe-preview-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEPreviewDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('common.preview') }}
                </template>
            </Toolbar>
        </template>

        <div>
            <h3>radi</h3>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import QBEPreviewDialogDescriptor from './QBEPreviewDialogDescriptor.json'

export default defineComponent({
    name: 'qbe-preview-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, id: { type: String } },
    emits: ['close'],
    data() {
        return {
            QBEPreviewDialogDescriptor,
            loading: false
        }
    },
    watch: {},
    async created() {},
    methods: {
        loadData() {},
        closeDialog() {
            this.$emit('close')
        },
        save() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#qbe-preview-dialog .p-dialog-header,
#qbe-preview-dialog .p-dialog-content {
    padding: 0;
}
#qbe-preview-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
