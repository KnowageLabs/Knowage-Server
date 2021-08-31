<template>
    <Dialog :header="$t('managers.glossaryDefinition.word')" :breakpoints="glossaryDefinitionDialogDescriptor.dialog.breakpoints" :style="glossaryDefinitionDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #footer>
            <Button :label="$t('common.cancel')" @click="closeDialog" class="kn-button kn-button--secondary" />
            <Button :label="$t('common.save')" @click="saveWord" class="kn-button kn-button--primary" />
        </template>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import glossaryDefinitionDialogDescriptor from './GlossaryDefinitionDialogDescriptor.json'
export default defineComponent({
    name: 'edit-word',
    components: {
        Dialog
    },
    props: {
        visible: {
            type: Boolean,
            required: true
        }
    },
    emits: ['close', 'add'],
    data() {
        return {
            glossaryDefinitionDialogDescriptor
        }
    },
    methods: {
        saveWord() {
            // this.$emit('add', this.selectedKpi)
            // this.selectedKpi = []
        },
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>
