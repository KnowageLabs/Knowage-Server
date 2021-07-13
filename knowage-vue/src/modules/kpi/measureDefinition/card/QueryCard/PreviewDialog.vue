<template>
    <Dialog :style="previewDialogDescriptor.dialog.style" :visible="true" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :header="$t('kpi.measureDefinition.preview')" :closable="false">
        <div class="p-d-flex">
            <DataTable :value="rows" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px">
                <Column class="kn-truncated" v-for="col of columns" :field="col.name" :header="col.label" :key="col.field" :sortable="true"> </Column>
            </DataTable>
            <div v-if="this.rule.placeholders.length > 0" class="p-col-3">
                <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
                    <template #left>
                        {{ $t('kpi.measureDefinition.filters') }}
                    </template>
                    <template #right>
                        <Button class="kn-button p-button-text p-button-rounded" @click="loadPreview(true)">{{ $t('common.run') }}</Button>
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
import axios from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import previewDialogDescriptor from './PreviewDialogDescriptor.json'

export default defineComponent({
    name: 'preview-dialog',
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
        await this.loadPreview(false)
    },
    methods: {
        loadRule() {
            console.log('RULE: ', this.rule)
            this.rule = this.currentRule as iRule
        },
        async loadPreview(hasPlaceholders: Boolean) {
            console.log('RULE: ', this.rule)
            const tempDatasource = this.rule.dataSource
            delete this.rule.dataSource
            this.loadPlaceholder()
            if (this.rule.placeholders.length === 0 || hasPlaceholders) {
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
            console.log('PLACEHOLDER ', placeholder)
            if (placeholder != null) {
                for (let i = 0; i < placeholder.length; i++) {
                    const placeholderName = placeholder[i].substring(1, placeholder[i].length)
                    let tempPlaceholder = this.rule.placeholders.find((tempPlaceholder) => {
                        console.log(tempPlaceholder.name + ' === ' + placeholderName)
                        return tempPlaceholder.name === placeholderName
                    })
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
