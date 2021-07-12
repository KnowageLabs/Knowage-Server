<template>
    <Dialog :style="previewDialogDescriptor.dialog.style" :visible="true" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :header="$t('kpi.measureDefinition.preview')" :closable="false">
        <DataTable :value="rows" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px">
            <Column class="kn-truncated" v-for="col of columns" :field="col.name" :header="col.label" :key="col.field" :sortable="true"> </Column>
        </DataTable>
        <div>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
                <template #left>
                    {{ $t('kpi.measureDefinition.filters') }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text p-button-rounded" @click="previewVisible = true">{{ $t('kpi.measureDefinition.filters') }}</Button>
                </template>
            </Toolbar>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeTemplate"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iRule } from '../../MeasureDefinition'
import axios from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import previewDialogDescriptor from './PreviewDialogDescriptor.json'

export default defineComponent({
    name: 'name',
    components: { Column, DataTable, Dialog },
    props: {
        currentRule: {
            type: Object,
            required: true
        },
        placeholders: {
            type: Array,
            required: true
        }
    },
    emits: ['close'],
    data() {
        return {
            previewDialogDescriptor,
            rule: {} as iRule,
            columns: [],
            rows: []
        }
    },
    async created() {
        this.loadRule()
        await this.loadPreview()
    },
    methods: {
        loadRule() {
            this.rule = this.currentRule as iRule
        },
        async loadPreview() {
            console.log('RULE: ', this.rule)
            const tempDatasource = this.rule.dataSource
            delete this.rule.dataSource
            this.loadPlaceholder()
            if (this.rule.placeholders.length === 0) {
                const postData = { rule: this.rule, maxItem: 10 }
                await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/queryPreview', postData).then((response) => {
                    this.columns = response.data.columns
                    this.rows = response.data.rows
                })
            }
            this.rule.dataSource = tempDatasource
        },
        loadPlaceholder() {
            const placeholder = this.currentRule.definition.match(/@\w*/g)
            if (placeholder != null) {
                for (let i = 0; i < placeholder.length; i++) {
                    const placeholderName = placeholder[i].substring(1, placeholder[i].length)
                    let tempPlaceholder = this.rule.placeholders.find((tempPlaceholder) => tempPlaceholder.name.toUpperCase() === placeholderName)
                    console.log('TEMP PLACEHOLDER', tempPlaceholder)
                    if (!tempPlaceholder) {
                        tempPlaceholder = this.placeholders.find((placeholder: any) => placeholder.name === tempPlaceholder?.name) as any
                        if (tempPlaceholder == undefined) {
                            const newPlaceholder = {
                                name: placeholderName,
                                value: ''
                            }
                            this.rule.placeholders.push(newPlaceholder)
                        } else {
                            this.rule.placeholders.push(tempPlaceholder)
                        }
                    }
                    console.log('RULE PLACEHOLDERS: ', this.rule.placeholders)
                    for (let index = 0; index < this.rule.placeholders.length; index++) {
                        if (placeholder.indexOf('@' + this.rule.placeholders[index].name) == -1) {
                            this.rule.placeholders.splice(index, 1)
                            index--
                        }
                    }
                }
            } else {
                this.rule.placeholders = []
            }
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
