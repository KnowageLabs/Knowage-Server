<template>
    <div class="p-m-4">
        <DataTable :value="parameters" class="p-datatable-sm kn-table p-m-2" dataKey="name" responsiveLayout="stack" breakpoint="600px">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <Column class="kn-truncated" v-for="(column, index) in olapCrossNavigationDefinitionDialogDescriptor.columns" :key="index" :field="column.field" :header="$t(column.label)" :sortable="true"></Column>
            <Column :style="olapCrossNavigationDefinitionDialogDescriptor.iconColumn.style">
                <template #body="slotProps">
                    <Button icon="pi pi-trash" class="p-button-link" v-tooltip.top="$t('common.delete')" @click="deleteParameterConfirm(slotProps.data)" />
                </template>
            </Column>
        </DataTable>

        <div v-if="addNewParameterVisible">
            <label for="type" class="kn-material-input-label"> {{ $t('documentExecution.olap.crossNavigationDefinition.crossNavigationType') }} </label>
            <Dropdown id="type" class="kn-material-input" v-model="selectedParameter.type" :options="olapCrossNavigationDefinitionDialogDescriptor.typeList" optionLabel="label" optionValue="value" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iOlapCrossNavigationParameter } from '../Olap'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import olapCrossNavigationDefinitionDialogDescriptor from './OlapCrossNavigationDefinitionDialogDescriptor.json'

export default defineComponent({
    name: 'olap-cross-navigation-step-one',
    components: { Column, DataTable, Dropdown },
    props: { propParameters: { type: Array as PropType<iOlapCrossNavigationParameter[]> }, addNewParameterVisible: { type: Boolean }, propSelectedParameter: { type: Object as PropType<iOlapCrossNavigationParameter | null> } },
    data() {
        return {
            olapCrossNavigationDefinitionDialogDescriptor,
            parameters: [] as iOlapCrossNavigationParameter[],
            selectedParameter: {} as any
        }
    },
    watch: {
        propParameters() {
            this.loadParameters()
        },
        propSelectedParameter() {
            this.loadSelectedParameter()
        }
    },
    created() {
        this.loadParameters()
        this.loadSelectedParameter()
    },
    methods: {
        loadParameters() {
            this.parameters = this.propParameters as iOlapCrossNavigationParameter[]
            console.log('LOADED PARAMETERS STEP ONE: ', this.parameters)
        },
        loadSelectedParameter() {
            this.selectedParameter = this.propSelectedParameter
            console.log('LOADED SELECTED PARAMETER: ', this.selectedParameter)
        },
        deleteParameterConfirm(parameter: iOlapCrossNavigationParameter) {
            console.log('DELETE CLICKED FOR: ', parameter)
        }
    }
})
</script>
