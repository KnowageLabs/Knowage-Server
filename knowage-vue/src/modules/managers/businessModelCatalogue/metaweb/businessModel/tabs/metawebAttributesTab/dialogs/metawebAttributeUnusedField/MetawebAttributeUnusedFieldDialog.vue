<template>
    <Dialog id="metaweb-attribute-unused-field-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="metawebAttributeUnusedFieldDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('metaweb.businessModel.addUnusedFields') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <Message v-if="fields.length === 0" class="p-m-4" severity="info" :closable="false" :style="metawebAttributeUnusedFieldDialogDescriptor.styles.message">
            {{ $t('metaweb.businessModel.noUnusedFields') }}
        </Message>

        <Listbox v-else class="metaweb-unused-fields-listbox p-m-4" :options="fields">
            <template #option="slotProps">
                <div class="p-m-2">
                    <Checkbox class="p-mr-2" v-model="selectedUnusedFields" :value="slotProps.option"></Checkbox>
                    <span>{{ slotProps.option.name }}</span>
                </div>
            </template>
        </Listbox>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import Listbox from 'primevue/listbox'
import Message from 'primevue/message'
import metawebAttributeUnusedFieldDialogDescriptor from './MetawebAttributeUnusedFieldDialogDescriptor.json'

export default defineComponent({
    name: 'metaweb-attribute-detail-dialog',
    components: { Checkbox, Dialog, Listbox, Message },
    props: { visible: { type: Boolean }, unusedFields: { type: Object } },
    emits: ['close', 'save'],
    data() {
        return {
            metawebAttributeUnusedFieldDialogDescriptor,
            fields: [] as any[],
            selectedUnusedFields: [] as any[],
            loading: false
        }
    },
    watch: {
        unusedFields() {
            this.loadFields()
        }
    },
    created() {
        this.loadFields()
    },
    methods: {
        loadFields() {
            this.fields = this.unusedFields as any[]
        },
        closeDialog() {
            this.$emit('close')
            this.selectedUnusedFields = []
        },
        save() {
            this.$emit('save', this.selectedUnusedFields)
        }
    }
})
</script>

<style lang="scss">
#metaweb-attribute-unused-field-dialog #metaweb-attribute-detail-dialog .p-dialog-header,
#metaweb-attribute-unused-field-dialog .p-dialog-content {
    padding: 0;
}

#metaweb-attribute-unused-field-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}

.metaweb-unused-fields-listbox {
    border: none;
}

.metaweb-unused-fields-listbox .p-listbox-item {
    padding: 0 !important;
}
</style>
