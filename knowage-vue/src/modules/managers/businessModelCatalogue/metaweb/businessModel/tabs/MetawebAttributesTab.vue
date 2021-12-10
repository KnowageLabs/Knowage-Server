<template>
    <DataTable v-if="businessModel" class="p-datatable-sm kn-table p-m-2" :value="businessModel.columns" editMode="cell" responsiveLayout="stack" breakpoint="960px" @rowReorder="onRowReorder">
        <Column :rowReorder="true" :headerStyle="metawebAttributesTabDescriptor.reorderColumnStyle" :reorderableColumn="false" />
        <Column class="kn-truncated" v-for="(column, index) in metawebAttributesTabDescriptor.columns" :key="index" :field="column.field" :header="$t(column.header)">
            <template #editor="slotProps">
                <div class="p-d-flex p-flex-row">
                    <InputText v-if="column.field === 'name'" class="kn-material-input" v-model="slotProps.data[slotProps.column.props.field]" />
                    <Checkbox v-if="column.field === 'identifier' || column.field === 'Visibility'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
                </div>
            </template>
            <template #body="slotProps">
                <div class="p-d-flex p-flex-row">
                    <Checkbox v-if="column.field === 'identifier' || column.field === 'Visibility'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
                    <span v-else>{{ slotProps.data[slotProps.column.props.field] }}</span>
                </div>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '../../Metaweb'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import metawebAttributesTabDescriptor from './MetawebAttributesTabDescriptor.json'
import metaMock from '../../MetawebMock.json'

export default defineComponent({
    name: 'metaweb-attributes-tab',
    components: { Checkbox, Column, DataTable },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> } },
    emits: ['loading'],
    data() {
        return {
            metawebAttributesTabDescriptor,
            meta: metaMock as any,
            businessModel: null as iBusinessModel | null
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
            console.log('MOCKED META: ', metaMock)
            this.businessModel = metaMock.metaSales.businessModels[10] as iBusinessModel

            this.formatBusinessModel()

            console.log('BUSINESS MODEL LOADED: ', this.businessModel)
        },
        formatBusinessModel() {

        }
        async onRowReorder(event: any) {
            console.log('EVENT: ', event)
            this.$emit('loading', true)
            const postData = { data: { businessModelUniqueName: this.businessModel?.uniqueName, index: event.dragIndex, direction: event.dropIndex - event.dragIndex }, diff: [] }
            console.log('TODO: ', postData)
            // TODO FIX SERVICE
            // await this.$http
            //     .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/moveBusinessColumn`, postData)
            //     .then(() => {
            //         console.log('TODO REORDER')
            //     })
            //     .catch(() => {})
            this.$emit('loading', false)
        }
    }
})
</script>
