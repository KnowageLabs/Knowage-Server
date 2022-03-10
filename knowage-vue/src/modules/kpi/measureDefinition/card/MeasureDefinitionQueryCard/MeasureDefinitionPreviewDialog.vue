<template>
    <Dialog :style="previewDialogDescriptor.dialog.style" :contentStyle="previewDialogDescriptor.dialog.contentStyle" :visible="true" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :header="$t('kpi.measureDefinition.preview')" :closable="false">
        <div class="p-d-flex">
            <DataTable :value="rows" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px">
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <Column class="kn-truncated" v-for="col of columns" :field="col.name" :header="col.label" :key="col.field" :sortable="true"> </Column>
            </DataTable>

            <div v-if="rule.placeholders && rule.placeholders.length > 0" class="p-col-3">
                <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
                    <template #start>
                        {{ $t('kpi.measureDefinition.filters') }}
                    </template>
                    <template #end>
                        <Button class="kn-button p-button-text p-button-rounded" @click="loadPreview">{{ $t('common.run') }}</Button>
                    </template>
                </Toolbar>
                <div>
                    <div v-for="placeholder in rule.placeholders" :key="placeholder.id">
                        <div class="p-field p-m-2">
                            <span class="p-float-label">
                                <InputText class="kn-material-input" type="text" v-model.trim="placeholder.value" />
                                <label class="kn-material-input-label"> {{ placeholder.name }} </label>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeTemplate"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { iRule } from '../../MeasureDefinition'
    import Column from 'primevue/column'
    import DataTable from 'primevue/datatable'
    import Dialog from 'primevue/dialog'
    import previewDialogDescriptor from './MeasureDefinitionPreviewDialogDescriptor.json'

    export default defineComponent({
        name: 'measure-definition-preview-dialog',
        components: { Column, DataTable, Dialog },
        props: {
            currentRule: {
                type: Object,
                required: true
            },
            placeholders: {
                type: Array,
                required: true
            },
            columns: { type: Array },
            propRows: { type: Array }
        },
        emits: ['close', 'loadPreview'],
        watch: {
            propRows() {
                this.loadRows()
            },
            currentRule() {
                this.loadRule()
            }
        },
        data() {
            return {
                previewDialogDescriptor,
                rule: {} as iRule,
                rows: [] as any[]
            }
        },
        async created() {
            this.loadRule()
            this.loadRows()
        },
        methods: {
            loadRule() {
                this.rule = this.currentRule as iRule
            },
            loadPreview() {
                this.$emit('loadPreview')
            },
            loadRows() {
                this.rows = this.propRows as any[]
            },
            closeTemplate() {
                this.$emit('close')
            }
        }
    })
</script>
