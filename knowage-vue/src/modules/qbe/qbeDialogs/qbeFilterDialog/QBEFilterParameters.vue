<template>
    <DataTable class="p-datatable-sm kn-table p-m-4" :value="parameters" editMode="cell" responsiveLayout="stack" breakpoint="960px" @cell-edit-complete="onCellEditComplete">
        <template #empty>
            <div>
                {{ $t('common.info.noDataFound') }}
            </div>
        </template>
        <Column :field="'name'" :header="$t('common.name')" :sortable="true"> </Column>
        <Column :field="'value'" :header="$t('common.value')" :sortable="true">
            <template #editor="slotProps">
                <div class="p-d-flex p-flex-row p-ai-center">
                    <InputText class="kn-material-input p-inputtext-sm" v-model="slotProps.data[slotProps.column.props.field]"></InputText>
                    <i class="pi pi-pencil p-ml-2" />
                </div>
            </template>
            <template #body="slotProps">
                <div class="p-d-flex p-flex-row p-ai-center">
                    <span>{{ slotProps.data[slotProps.column.props.field] }}</span>
                </div>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'

export default defineComponent({
    name: 'qbe-filter-parameters',
    components: { Column, DataTable },
    props: { visible: { type: Boolean }, propParameters: { type: Array } },
    emits: ['save', 'close', 'parametersUpdated'],
    data() {
        return {
            parameters: [] as any[]
        }
    },
    watch: {
        propParameters() {
            this.loadParameters()
        }
    },
    created() {
        this.loadParameters()
    },
    methods: {
        loadParameters() {
            this.parameters = []
            this.propParameters?.forEach((parameter: any) => {
                parameter.value = parameter.defaultValue
                this.parameters.push(parameter)
            })
        },
        onCellEditComplete(event: any) {
            this.parameters[event.index] = event.newData
            this.$emit('parametersUpdated', this.parameters)
        }
    }
})
</script>

<style lang="scss">
#qbe-filter-parameters-dialog.p-dialog-header,
#qbe-filter-parameters-dialog.p-dialog-content {
    padding: 0;
}
#qbe-filter-parameters-dialog.p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
