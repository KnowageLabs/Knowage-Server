<template>
    <div class="p-m-4">
        <div class="p-fluid p-field p-col-4">
            <span class="p-float-label">
                <InputText id="address" class="kn-material-input" v-model="model.address" />
                <label for="address" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.designerDialog.xmlaAddress') }}</label>
            </span>
        </div>
        <div class="p-m-2 p-col-6">
            <div class="p-d-flex p-flex-row">
                <label class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.designerDialog.xmlaParameters') }}</label>
                <Button id="add-parameter-button" class="kn-button kn-button--primary p-ml-auto" :label="$t('documentExecution.documentDetails.designerDialog.addParameter')" @click="addParameter"></Button>
            </div>

            <DataTable :value="xmlModel?.parameters" class="p-datatable-sm kn-table p-m-5" responsiveLayout="stack" breakpoint="960px">
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <Column class="kn-truncated p-mr-2" v-for="column in descriptor.xmlaParametersColumns" :key="column.header" :field="column.field" :header="$t(column.header)">
                    <template #body="slotProps">
                        <div class="p-fluid">
                            <InputText class="kn-material-input p-inputtext-sm p-mr-2" v-model.trim="slotProps.data[slotProps.column.props.field]" />
                            <i class="pi pi-pencil edit-icon" />
                        </div>
                    </template>
                </Column>
                <Column :style="descriptor.iconColumnStyle">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteParameter(slotProps.index)" :data-test="'delete-button-' + slotProps.index" />
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iXMLATemplate } from '../../DocumentDetails'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import descriptor from './DocumentDetailOlapDesignerDialogDescriptor.json'

export default defineComponent({
    name: 'document-detail-xmla-form',
    components: { Column, DataTable },
    props: { xmlModel: { type: Object as PropType<iXMLATemplate> } },
    data() {
        return {
            descriptor,
            model: {} as iXMLATemplate
        }
    },
    watch: {
        xmlModel() {
            this.loadModel()
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.xmlModel as iXMLATemplate
        },
        addParameter() {
            this.model.parameters.push({ name: '', value: '' })
        },
        deleteParameter(index: number) {
            this.model.parameters.splice(index, 1)
        }
    }
})
</script>
