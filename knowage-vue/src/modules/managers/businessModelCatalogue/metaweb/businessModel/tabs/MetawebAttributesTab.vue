<template>
    <DataTable v-if="businessModel" class="p-datatable-sm kn-table p-m-2" :value="businessModel.columns" :loading="loading" editMode="cell" responsiveLayout="stack" breakpoint="960px" @rowReorder="onRowReorder">
        <Column :rowReorder="true" :headerStyle="metawebAttributesTabDescriptor.reorderColumnStyle" :reorderableColumn="false" />
        <Column class="kn-truncated" v-for="(column, index) in metawebAttributesTabDescriptor.columns" :key="index" :field="column.field" :header="$t(column.header)">
            <template #editor="slotProps">
                <div class="p-d-flex p-flex-row">
                    <InputText v-if="column.field === 'name' || column.field === 'description'" class="p-inputtext-sm kn-material-input" v-model="slotProps.data[slotProps.column.props.field]" @change="test" />
                    <Checkbox v-else-if="column.field === 'identifier'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
                    <Checkbox v-else-if="column.field === 'visible'" v-model="columnsVisibility[slotProps.data.uniqueName]" :binary="true" @change="onChange(slotProps.data, 'visibility')"></Checkbox>
                    <Dropdown v-else-if="column.field === 'type'" class="kn-material-input" v-model="columnsType[slotProps.data.uniqueName]" :options="metawebAttributesTabDescriptor.typeOptions" @change="onChange(slotProps.data, 'type')" />
                    <i v-if="column.field !== 'identifier' && column.field !== 'visible'" class="pi pi-pencil edit-icon p-ml-2" />
                </div>
            </template>
            <template #body="slotProps">
                <div class="p-d-flex p-flex-row">
                    <Checkbox v-if="column.field === 'identifier'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
                    <Checkbox v-else-if="column.field === 'visible'" v-model="columnsVisibility[slotProps.data.uniqueName]" :binary="true" @change="onChange(slotProps.data, 'visibility')"></Checkbox>
                    <span v-else-if="column.field === 'type'">{{ columnsType[slotProps.data.uniqueName] }}</span>
                    <span v-else>{{ slotProps.data[slotProps.column.props.field] }}</span>
                    <i v-if="column.field !== 'identifier' && column.field !== 'visible'" class="pi pi-pencil edit-icon p-ml-2" />
                </div>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iBusinessModel, iBusinessModelColumn } from '../../Metaweb'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import metawebAttributesTabDescriptor from './MetawebAttributesTabDescriptor.json'
import metaMock from '../../MetawebMock.json'

const { observe, generate } = require('fast-json-patch')

export default defineComponent({
    name: 'metaweb-attributes-tab',
    components: { Checkbox, Column, DataTable, Dropdown },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> } },
    emits: ['loading'],
    data() {
        return {
            metawebAttributesTabDescriptor,
            meta: metaMock as any,
            businessModel: null as iBusinessModel | null,
            columnsVisibility: {} as any,
            columnsType: {} as any,
            observer: null as any,
            loading: false
        }
    },
    watch: {
        selectedBusinessModel() {
            this.loadBusinessModel()
        }
    },
    created() {
        this.loadBusinessModel()
    },
    methods: {
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel

            // TODO REMOVE MOCK
            // console.log('MOCKED META: ', metaMock)
            this.businessModel = this.selectedBusinessModel as iBusinessModel

            if (this.businessModel) {
                this.observer = observe(this.businessModel)
            }

            this.formatBusinessModel()

            // console.log('BUSINESS MODEL LOADED: ', this.businessModel)
        },
        formatBusinessModel() {
            if (this.businessModel) {
                this.businessModel.columns?.forEach((column: any) => {
                    // console.log('COLUMN: ', column)
                    for (let i = 0; i < column.properties.length; i++) {
                        const tempProperty = column.properties[i]
                        // console.log('TEMP PROPERTY: ', tempProperty)
                        const key = Object.keys(tempProperty)[0]
                        if (key === 'structural.visible') {
                            // console.log('FOUND: ', tempProperty[key])
                            this.columnsVisibility[column.uniqueName] = tempProperty[key].value === 'true'
                        } else if (key === 'structural.columntype') {
                            this.columnsType[column.uniqueName] = tempProperty[key].value
                        }
                    }
                })
            }

            // console.log('COLUMNS VISIBILITY: ', this.columnsVisibility)
            // console.log('COLUMNS TYPE: ', this.columnsType)
        },
        async onRowReorder(event: any) {
            // console.log('EVENT: ', event)
            this.loading = true
            const postData = { data: { businessModelUniqueName: this.businessModel?.uniqueName, index: event.dragIndex, direction: event.dropIndex - event.dragIndex }, diff: generate(this.observer) }
            await this.$http
                .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/moveBusinessColumn`, postData)
                .then(() => {
                    this.reorderArray(event.dragIndex, event.dropIndex)
                })
                .catch(() => {})
            this.loading = false
        },
        reorderArray(from: number, to: number) {
            if (this.businessModel) {
                this.businessModel.columns.splice(to, 0, this.businessModel.columns.splice(from, 1)[0])
            }
        },
        onChange(column: iBusinessModelColumn, type: string) {
            // console.log('COLUMN CHANGED: ', column)
            // console.log('COLUMNS VISIBILITY: ', this.columnsVisibility)
            for (let i = 0; i < column.properties.length; i++) {
                const tempProperty = column.properties[i]
                const key = Object.keys(tempProperty)[0]
                if (key === 'structural.visible' && type === 'visibility') {
                    // console.log('FOUND: ', tempProperty[key])
                    tempProperty[key].value = this.columnsVisibility[column.uniqueName]
                } else if (key === 'structural.columntype' && type === 'type') {
                    tempProperty[key].value = this.columnsType[column.uniqueName]
                }
            }

            // const patch = generate(this.observer)
            // console.log('PATCH: ', patch)
        }
    }
})
</script>
