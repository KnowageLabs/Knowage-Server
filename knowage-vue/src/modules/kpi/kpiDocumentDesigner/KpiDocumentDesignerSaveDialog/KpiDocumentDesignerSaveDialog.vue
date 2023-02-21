<template>
    <Dialog id="kpi-edit-save-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="KpiDocumentDesignerSaveDialogDescriptor.dialog.style" :content-style="KpiDocumentDesignerSaveDialogDescriptor.dialog.contentStyle" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('kpi.kpiDocumentDesigner.saveDialogTitle') }}
                </template>
            </Toolbar>
        </template>

        <div>
            <h2>{{ $t('kpi.kpiDocumentDesigner.saveTitle') }}</h2>
            <h4>{{ $t('kpi.kpiDocumentDesigner.saveSubheader') }}</h4>
            <div class="p-field">
                <span class="p-float-label">
                    <InputText
                        v-model="kpiName"
                        class="kn-material-input"
                        :class="{
                            'p-invalid': kpiName.length === 0
                        }"
                    />
                    <label class="kn-material-input-label"> {{ $t('kpi.kpiDocumentDesigner.kpiName') }}</label>
                </span>
            </div>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" :label="$t('common.close')" @click="close"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="saveButtonDisabled" @click="saveKpi"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import KpiDocumentDesignerSaveDialogDescriptor from './KpiDocumentDesignerSaveDialogDescriptor.json'

export default defineComponent({
    name: 'kpi-edit-save-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean } },
    emits: ['close', 'saveKpi'],
    data() {
        return {
            KpiDocumentDesignerSaveDialogDescriptor,
            kpiName: ''
        }
    },
    computed: {
        saveButtonDisabled(): boolean {
            return this.kpiName.length === 0
        }
    },
    created() {},
    methods: {
        close() {
            this.kpiName = ''
            this.$emit('close')
        },
        saveKpi() {
            this.$emit('saveKpi', this.kpiName)
        }
    }
})
</script>

<style lang="scss">
#kpi-edit-save-dialog .p-dialog-header,
#kpi-edit-save-dialog .p-dialog-content {
    padding: 0;
}
#kpi-edit-save-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
