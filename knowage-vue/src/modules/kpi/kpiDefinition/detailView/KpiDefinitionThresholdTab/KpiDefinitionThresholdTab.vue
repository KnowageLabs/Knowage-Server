<template>
    <Card v-if="!loading" :style="tabViewDescriptor.card.style">
        <template #content>
            <Message v-if="false" severity="info">
                THRESHOLD USED BY OTHER KPI WARNING: THIS THRESHOLD IS USED ELSEWHERE. ANY CHANGE WILL AFFECT OTHER KPIS. PLEASE CONSIDER CREATING A CLONE
                <Button label="CLONE" />
            </Message>
            {{ kpi.threshold.usedByKpi }}
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-md-6" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            maxLength="100"
                            v-model.trim="v$.threshold.name.$model"
                            :class="{
                                'p-invalid': v$.threshold.name.$invalid && v$.threshold.name.$dirty
                            }"
                            @blur="v$.threshold.name.$touch()"
                            @input="onThresholdFieldChange('name', $event.target.value)"
                            data-test="name-input"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.threshold.name" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
                </div>
                <div class="p-field p-col-12 p-md-6" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            class="kn-material-input"
                            type="text"
                            maxLength="500"
                            v-model.trim="v$.threshold.description.$model"
                            :class="{
                                'p-invalid': v$.threshold.description.$invalid && v$.threshold.description.$dirty
                            }"
                            @blur="v$.threshold.description.$touch()"
                            @input="onThresholdFieldChange('description', $event.target.value)"
                            data-test="description-input"
                        />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }}</label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.threshold.description" :additionalTranslateParams="{ fieldName: $t('common.description') }" />
                </div>
            </form>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <DataTable v-if="!loading" :value="kpi.threshold.thresholdValues" :loading="loading" :resizableColumns="true" editMode="cell" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" data-test="messages-table">
                <Column field="label" header="Label" :style="{ width: '10%' }">
                    <template #editor="slotProps">
                        <InputText v-model="slotProps.data['label']" />
                    </template>
                </Column>
                <Column field="label" header="Label">
                    <template #editor="slotProps">
                        <InputText v-model="slotProps.data['label']" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from '../KpiDefinitionDetailDescriptor.json'
import tresholdTabDescriptor from './KpiDefinitionThresholdTabDescriptor.json'
// import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Card from 'primevue/card'
import Message from 'primevue/message'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
// import Checkbox from 'primevue/checkbox'
// import InputNumber from 'primevue/inputnumber'
// import ColorPicker from 'primevue/colorpicker'
// import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'treshold-tab',
    components: {
        // KnValidationMessages
        Card,
        Message,
        DataTable,
        Column
        // Checkbox,
        // InputNumber,
        // ColorPicker,
        // Dropdown,
    },
    props: {
        selectedKpi: Object,
        severityOptions: Array,
        loading: Boolean
    },
    emits: ['thresholdFieldChanged', 'activeVersionChanged'],
    data() {
        return {
            v$: useValidate() as any,
            tabViewDescriptor,
            tresholdTabDescriptor,
            kpi: {} as any,
            threshold: {} as any,
            touched: false,
            columns: tresholdTabDescriptor.datatableColumns
        }
    },

    validations() {
        return {
            threshold: createValidations('threshold', tresholdTabDescriptor.validations.kpi)
        }
    },

    mounted() {
        if (this.selectedKpi) {
            this.kpi = { ...this.selectedKpi } as any
            this.threshold = this.kpi.threshold
        }
    },
    watch: {
        selectedKpi() {
            this.kpi = { ...this.selectedKpi } as any
            this.threshold = this.kpi.threshold
        }
    },
    methods: {
        onThresholdFieldChange(fieldName: string, value: any) {
            this.$emit('thresholdFieldChanged', { fieldName, value })
        }
    }
})
</script>
