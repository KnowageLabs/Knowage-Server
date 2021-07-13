<template>
    <Card :style="tabViewDescriptor.card.style">
        <template #content>
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
                            v-model.trim="v$.threshold.description.$model"
                            :class="{
                                'p-invalid': v$.threshold.description.$invalid && v$.threshold.description.$dirty
                            }"
                            @blur="v$.threshold.description.$touch()"
                            @input="onThresholdFieldChange('description', $event.target.value)"
                            data-test="description-input"
                            maxLength="500"
                        />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }}</label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.threshold.description" :additionalTranslateParams="{ fieldName: $t('common.description') }" />
                </div>
            </form>
            {{ threshold }}
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <DataTable :value="threshold.thresholdValues" :loading="loading" editMode="cell" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" data-test="messages-table">
                <Column field="label" header="Label">
                    <template #editor="slotProps">
                        <InputText v-model="slotProps.data[slotProps.column.props.field]" />
                    </template>
                </Column>
                <Column field="minValue" header="Min">
                    <template #editor="slotProps">
                        <InputNumber v-model="slotProps.data[slotProps.column.props.field]" />
                    </template>
                </Column>
                <Column field="includeMin" header="Include Min">
                    <template #body="slotProps">
                        <Checkbox v-model="slotProps.data[slotProps.column.props.field]" :binary="true" />
                    </template>
                </Column>
                <Column field="maxValue" header="Max">
                    <template #editor="slotProps">
                        <InputNumber v-model="slotProps.data[slotProps.column.props.field]" />
                    </template>
                </Column>
                <Column field="includeMax" header="Include Max">
                    <template #body="slotProps">
                        <Checkbox v-model="slotProps.data[slotProps.column.props.field]" :binary="true" />
                    </template>
                </Column>
                <Column field="severityId" header="Severity">
                    <Dropdown v-model="slotProps.data[slotProps.column.props.field]" :options="statuses" optionLabel="label" optionValue="value" placeholder="Select a Status">
                        <template #option="slotProps">
                            <span :class="'product-badge status-' + slotProps.option.value.toLowerCase()">{{ slotProps.option.label }}</span>
                        </template>
                    </Dropdown>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import Card from 'primevue/card'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import tabViewDescriptor from '../KpiDefinitionDetailDescriptor.json'
import tresholdTabDescriptor from './KpiDefinitionThresholdTabDescriptor.json'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Checkbox from 'primevue/checkbox'
import InputNumber from 'primevue/inputnumber'

export default defineComponent({
    name: 'treshold-tab',
    components: {
        Card,
        KnValidationMessages,
        Column,
        DataTable,
        Checkbox,
        InputNumber
    },
    props: {
        selectedKpi: Object
    },
    emits: ['thresholdFieldChanged', 'activeVersionChanged'],
    data() {
        return {
            v$: useValidate() as any,
            tabViewDescriptor,
            tresholdTabDescriptor,
            kpi: {} as any,
            threshold: {} as any,
            thresholdValues: [] as any,
            loading: false,
            touched: false,
            columns: tresholdTabDescriptor.datatableColumns
        }
    },

    validations() {
        return {
            threshold: createValidations('threshold', tabViewDescriptor.validations.kpi)
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
