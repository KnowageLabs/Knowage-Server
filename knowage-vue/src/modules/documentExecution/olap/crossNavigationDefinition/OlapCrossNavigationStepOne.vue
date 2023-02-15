<template>
    <div class="p-m-4">
        <DataTable :value="parameters" class="p-datatable-sm kn-table p-m-2" data-key="name" responsive-layout="stack" breakpoint="600px" @rowClick="$emit('parameterSelected', $event.data)">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <Column v-for="(column, index) in olapCrossNavigationDefinitionDialogDescriptor.columns" :key="index" class="kn-truncated" :field="column.field" :header="$t(column.label)" :sortable="true"></Column>
            <Column :style="olapCrossNavigationDefinitionDialogDescriptor.iconColumn.style">
                <template #body="slotProps">
                    <Button v-tooltip.top="$t('common.delete')" icon="pi pi-trash" class="p-button-link" @click="deleteParameterConfirm(slotProps.data)" />
                </template>
            </Column>
        </DataTable>

        <div v-if="addNewParameterVisible">
            <label for="type" class="kn-material-input-label"> {{ $t('documentExecution.olap.crossNavigationDefinition.crossNavigationType') }} </label>
            <Dropdown id="type" v-model="selectedParameter.type" class="kn-material-input" :options="olapCrossNavigationDefinitionDialogDescriptor.typeList" option-label="label" option-value="value" />
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
    emits: ['parameterSelected', 'deleteParameter'],
    data() {
        return {
            olapCrossNavigationDefinitionDialogDescriptor,
            parameters: [] as iOlapCrossNavigationParameter[],
            selectedParameter: {} as iOlapCrossNavigationParameter
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
        },
        loadSelectedParameter() {
            this.selectedParameter = this.propSelectedParameter as iOlapCrossNavigationParameter
        },
        deleteParameterConfirm(parameter: iOlapCrossNavigationParameter) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => this.$emit('deleteParameter', parameter)
            })
        }
    }
})
</script>
