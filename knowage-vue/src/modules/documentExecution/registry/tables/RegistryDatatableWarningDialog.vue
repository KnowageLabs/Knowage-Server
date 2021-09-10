<template>
    <Dialog :style="registryDatatableWarningDialogDescriptor.dialog.style" :header="$t('documentExecution.registry.warning')" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <template #footer>
            <div>
                <p>
                    {{ $t('documentExecution.registry.column') }}
                    {{ columnFileds }}
                    {{ $t('documentExecution.registry.warningDependences') }}
                </p>
            </div>
            <div>
                <Checkbox v-model="stopWarnings" :binary="true"></Checkbox>
                <label> {{ $t('documentExecution.registry.warningCheckbox') }}</label>
            </div>
            <Button class="kn-button kn-button--primary" @click="$emit('close', stopWarnings)"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import registryDatatableWarningDialogDescriptor from './RegistryDatatableWarningDialogDescriptor.json'

export default defineComponent({
    name: 'registry-datatable-warning-dialog',
    components: { Checkbox, Dialog },
    emits: ['close'],
    props: {
        visible: { type: Boolean },
        columns: { type: Array, required: true }
    },
    data() {
        return {
            registryDatatableWarningDialogDescriptor,
            stopWarnings: false
        }
    },
    computed: {
        columnFileds(): string {
            let fields = ''
            for (let i = 0; i < this.columns.length; i++) {
                fields += (this.columns[i] as any).field + (this.columns.length - 1 != i) ? ', ' : ''
            }
            return fields
        }
    },
    created() {},
    methods: {}
})
</script>
