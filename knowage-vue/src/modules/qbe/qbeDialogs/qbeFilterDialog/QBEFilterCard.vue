<template>
    <div v-if="filter">
        <div class="p-grid p-m-2">
            <div class="p-col-4 p-d-flex p-flex-row p-ai-center">
                <div class="kn-flex">
                    <label class="kn-material-input-label"> {{ $t('common.field') }} </label>
                    <InputText class="kn-material-input" v-model="filter.leftOperandDescription" :disabled="true" />
                </div>
            </div>

            <div class="p-col-2 p-d-flex p-flex-row p-ai-center">
                <div class="kn-flex">
                    <label class="kn-material-input-label" v-tooltip.top="$t('qbe.filters.conditionTooltip')"> {{ $t('qbe.filters.condition') }} </label>
                    <Dropdown class="kn-material-input" v-model="filter.operator" :options="filterOperatorOptions" optionValue="value" @change="onFilterOperatorChange">
                        <template #value="slotProps">
                            <div v-if="slotProps.value">
                                <span class="qbe-filter-option-value">{{ getFilterOperatorLabel(slotProps.value) }}</span>
                            </div>
                        </template>
                        <template #option="slotProps">
                            <div>
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </div>
                        </template>
                    </Dropdown>
                </div>

                <div v-show="filter.operator === 'SPATIAL_NN'" class=" p-ml-2">
                    <label class="kn-material-input-label"> {{ $t('common.parameter') }} </label>
                    <InputText class="kn-material-input" v-model="filter.operatorParameter" />
                </div>
            </div>

            <div class="p-col-2 p-d-flex p-flex-row p-ai-center">
                <div class="kn-flex">
                    <label class="kn-material-input-label"> {{ $t('qbe.filters.targetType') }} </label>
                    <Dropdown class="kn-material-input" v-model="filter.rightType" :options="targetValues" optionValue="value" optionLabel="label" @change="onFilterTypeChange" />
                </div>
            </div>

            <div class="p-col-4 p-d-flex p-flex-row p-ai-center">
                <div class="kn-flex">
                    <label class="kn-material-input-label" v-show="!(filter.rightType === 'manual' && ['BETWEEN', 'NOT BETWEEN', 'IN', 'NOT IN'].includes(filter.operator))"> {{ $t('qbe.filters.target') }} </label>
                    <div class="p-d-flex p-flex-row p-ai-center">
                        <div v-if="filter.rightType === 'manual' && ['BETWEEN', 'NOT BETWEEN'].includes(filter.operator) && field.dataType !== 'java.sql.Timestamp' && field.dataType !== 'java.sql.Date'" class="p-d-flex p-flex-row p-ai-center p-mt-3">
                            <div class="p-float-label">
                                <InputText class="kn-material-input" v-model="firstOperand" @input="onManualBetweenChange" />
                                <label class="kn-material-input-label"> {{ $t('qbe.filters.lowLimit') }} </label>
                            </div>
                            <span class="p-mx-2">{{ $t('qbe.filters.and') }}</span>
                            <div class="p-float-label">
                                <InputText class="kn-material-input" v-model="secondOperand" @input="onManualBetweenChange" />
                                <label class="kn-material-input-label"> {{ $t('qbe.filters.highLimit') }} </label>
                            </div>
                        </div>
                        <div v-else-if="filter.rightType === 'manual' && ['IN', 'NOT IN'].includes(filter.operator) && field.dataType !== 'java.sql.Timestamp' && field.dataType !== 'java.sql.Date'" class="kn-width-full">
                            <label class="kn-material-input-label"> {{ $t('qbe.filters.enterValue') }} </label>
                            <Chips class="kn-material-input" v-model="multiManualValues" @add="onManualMultivalueChanged" @remove="onManualMultivalueChanged" :addOnBlur="true" />
                        </div>

                        <InputText v-else-if="filter.rightType === 'manual' && field.dataType !== 'java.sql.Timestamp' && field.dataType !== 'java.sql.Date'" class="kn-material-input" v-model="filter.rightOperandDescription" @input="onManualValueChange" />

                        <div v-else-if="filter.rightType === 'manual' && (field.dataType === 'java.sql.Timestamp' || field.dataType === 'java.sql.Date')">
                            <div class="kn-flex p-d-flex p-flex-row p-m-1">
                                <Calendar class="kn-flex p-mr-2" v-model="targetDate" @input="onManualTimestampChange" @dateSelect="onManualTimestampChange"></Calendar>
                                <Calendar v-if="field.dataType === 'java.sql.Timestamp'" class="qbe-filter-time-input" v-model="targetDate" :manualInput="true" :timeOnly="true" hourFormat="24" @input="onManualTimestampChange" @dateSelect="onManualTimestampChange" />
                            </div>

                            <div v-if="['BETWEEN', 'NOT BETWEEN'].includes(filter.operator)" class="kn-flex p-d-flex p-flex-row p-m-1">
                                <Calendar class="kn-flex p-mr-2" v-model="targetEndDate" @input="onManualTimestampChange" @dateSelect="onManualTimestampEndDateChange"></Calendar>
                                <Calendar v-if="field.dataType === 'java.sql.Timestamp'" class="qbe-filter-time-input" v-model="targetEndDate" :manualInput="true" :timeOnly="true" hourFormat="24" @input="onManualTimestampEndDateChange" @dateSelect="onManualTimestampChange" />
                            </div>
                        </div>

                        <div class="qbe-filter-chip-container p-d-flex p-flex-row p-ai-center p-flex-wrap kn-flex" v-else-if="filter.rightType === 'valueOfField'">
                            <Chip v-for="(selectedValue, index) in selectedValues" :key="index" class="p-mr-1">{{ selectedValue }}</Chip>
                        </div>

                        <CascadeSelect
                            v-if="filter.rightType === 'anotherEntity'"
                            class="kn-flex"
                            v-model="filter.rightOperandDescription"
                            :options="entities"
                            optionLabel="attributes.longDescription"
                            optionValue="attributes.longDescription"
                            optionGroupLabel="text"
                            :optionGroupChildren="['children']"
                            @change="onEntityTypeChanged"
                        ></CascadeSelect>

                        <Dropdown class="kn-material-input kn-flex" v-if="filter.rightType === 'subquery'" v-model="filter.rightOperandDescription" :options="subqueries" optionValue="name" optionLabel="name" @change="onSubqeryTargetChange" />

                        <Dropdown class="kn-material-input kn-flex" v-if="filter.rightType === 'parameter'" v-model="filter.rightOperandDescription" :options="parameters" optionValue="name" optionLabel="name" @change="onParameterTargetChange" />

                        <i v-if="filter.rightType === 'valueOfField'" class="fa fa-check kn-cursor-pointer p-ml-2" @click="loadFilterValues"></i>
                        <i class="fa fa-eraser kn-cursor-pointer p-ml-2" @click="$emit('removeFilter', filter)"></i>
                    </div>
                </div>
            </div>
        </div>
        <QBEFilterValuesTable v-show="filter.rightType === 'valueOfField'" class="p-m-2" :filterValuesData="filterValuesData" :loadedSelectedValues="selectedValues" :loading="loading" :filterOperator="filter.operator" @selected="setSelectedValues"></QBEFilterValuesTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iFilter } from '../../QBE'
import Calendar from 'primevue/calendar'
import CascadeSelect from 'primevue/cascadeselect'
import Chip from 'primevue/chip'
import Chips from 'primevue/chips'
import Dropdown from 'primevue/dropdown'
import QBEFilterDialogDescriptor from './QBEFilterDialogDescriptor.json'
import QBEFilterValuesTable from './QBEFilterValuesTable.vue'
import moment from 'moment'

export default defineComponent({
    name: 'qbe-filter-card',
    components: { Calendar, CascadeSelect, Chip, Chips, Dropdown, QBEFilterValuesTable },
    props: { propFilter: { type: Object as PropType<iFilter> }, id: { type: String }, propEntities: { type: Array }, subqueries: { type: Array, required: true }, field: { type: Object, required: true }, propParameters: { type: Array } },
    emits: ['removeFilter'],
    data() {
        return {
            QBEFilterDialogDescriptor,
            filter: null as iFilter | null,
            targetValues: [
                {
                    label: this.$t('qbe.filters.targets.manual'),
                    value: 'manual'
                },
                {
                    label: this.$t('qbe.filters.targets.value'),
                    value: 'valueOfField'
                },
                {
                    label: this.$t('qbe.filters.targets.entity'),
                    value: 'anotherEntity'
                }
            ],
            selectedValues: [] as string[],
            filterValuesData: null,
            anotherEntityValue: '',
            entities: [] as any[],
            firstOperand: '',
            secondOperand: '',
            multiManualValues: [] as string[],
            targetDate: null as Date | null,
            targetEndDate: null as Date | null,
            parameters: [] as any[],
            loading: false,
            filterOperatorOptions: [] as { label: string; value: string }[]
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        },
        propEntities() {
            this.loadEntities()
        },
        propParameters() {
            this.loadParameters()
        }
    },
    created() {
        this.loadEntities()
        this.loadFilter()
        this.loadParameters()
    },
    methods: {
        loadFilter() {
            this.filter = this.propFilter as iFilter
            if (this.subqueries?.length > 0) {
                this.targetValues.push({
                    label: this.$t('qbe.filters.targets.subquery'),
                    value: 'subquery'
                })
            }

            this.formatFilter()

            this.filterOperatorOptions = this.QBEFilterDialogDescriptor.operatorValues
            const tempEntity = this.getEntity() as any
            if (tempEntity?.iconCls === 'geographic_dimension') {
                this.filterOperatorOptions = this.filterOperatorOptions.concat(this.QBEFilterDialogDescriptor.spatialOperatorValues)
            }
        },
        getEntity() {
            let entity = null
            for (let i = 0; i < this.entities.length && !entity; i++) {
                for (let j = 0; j < this.entities[i].children.length; j++) {
                    if (this.entities[i].children[j].id === this.field.id) {
                        entity = this.entities[i]
                        break
                    }
                }
            }

            return entity
        },
        loadEntities() {
            this.entities = this.propEntities ? [...this.propEntities] : []
        },
        loadParameters() {
            this.parameters = this.propParameters as any[]
            if (this.parameters.length > 0) {
                this.targetValues.push({
                    label: this.$t('common.parameter'),
                    value: 'parameter'
                })
            }
        },
        async formatFilter() {
            switch (this.filter?.rightType) {
                case 'manual':
                    this.filter.rightOperandType = 'Static Content'

                    if (['java.sql.Timestamp'].includes(this.field.dataType)) {
                        this.targetDate = this.filter.rightOperandValue[0] ? moment(this.filter.rightOperandValue[0], 'DD/MM/YYYY hh:mm').toDate() : new Date()
                        this.onManualTimestampChange()
                        if (['BETWEEN', 'NOT BETWEEN'].includes(this.filter.operator)) {
                            this.targetEndDate = this.filter.rightOperandValue[1] ? moment(this.filter.rightOperandValue[1], 'DD/MM/YYYY hh:mm').toDate() : new Date()
                        }
                    } else if (['java.sql.Date'].includes(this.field.dataType)) {
                        this.targetDate = this.filter.rightOperandValue[0] ? moment(this.filter.rightOperandValue[0], 'DD/MM/YYYY').toDate() : new Date()
                        this.onManualTimestampChange()
                        if (['BETWEEN', 'NOT BETWEEN'].includes(this.filter.operator)) {
                            this.targetEndDate = this.filter.rightOperandValue[1] ? moment(this.filter.rightOperandValue[1], 'DD/MM/YYYY').toDate() : new Date()
                        }
                    } else {
                        if (['BETWEEN', 'NOT BETWEEN'].includes(this.filter.operator)) {
                            this.firstOperand = this.filter.rightOperandValue[0]
                            this.secondOperand = this.filter.rightOperandValue[1]
                        } else if (['IN', 'NOT IN'].includes(this.filter.operator)) {
                            this.multiManualValues = [...this.filter.rightOperandValue]
                        }
                    }

                    this.filter.hasParam = false
                    this.filter.paramName = ''
                    break
                case 'valueOfField':
                    this.filter.rightOperandType = 'Static Content'
                    this.selectedValues = this.filter.rightOperandValue.filter((el: any) => el !== '')
                    await this.loadFilterValues()

                    this.filter.hasParam = false
                    this.filter.paramName = ''
                    break
                case 'anotherEntity':
                    this.filter.rightOperandType = 'Field Content'
                    this.filter.hasParam = false
                    this.filter.paramName = ''
                    break
                case 'subquery':
                    this.filter.rightOperandType = 'Subquery'
                    this.filter.hasParam = false
                    this.filter.paramName = ''
                    break
                case 'parameter':
                    this.filter.rightOperandType = 'Static Content'
                    break
            }
        },
        onFilterOperatorChange() {
            if (this.filter && this.filter.rightType === 'manual') {
                this.filter.rightOperandDescription = ''
                this.multiManualValues = []
                this.firstOperand = ''
                this.secondOperand = ''
                this.targetDate = null
                this.targetEndDate = null
            } else if (this.filter && this.filter.rightType === 'valueOfField') {
                this.selectedValues = []
            }

            if (this.filter && this.filter.operator !== 'SPATIAL_NN') {
                delete this.filter.operatorParameter
            }
            this.resetFilterRightOperandValues()
        },
        onManualValueChange() {
            if (this.filter) {
                this.filter.rightOperandValue = [this.filter.rightOperandDescription]
            }
        },
        onManualBetweenChange() {
            if (this.filter) {
                this.filter.rightOperandValue = [this.firstOperand, this.secondOperand]
                this.filter.rightOperandDescription = this.firstOperand + ' ---- ' + this.secondOperand
            }
        },
        onManualMultivalueChanged() {
            if (this.filter) {
                this.filter.rightOperandValue = [...this.multiManualValues]
                this.filter.rightOperandDescription = this.multiManualValues.join(' ---- ')
            }
        },
        async onFilterTypeChange() {
            if (this.filter) {
                this.resetFilterRightOperandValues()
                this.selectedValues = []
                this.filterValuesData = null
                this.firstOperand = ''
                this.secondOperand = ''
                this.multiManualValues = []
                this.formatFilter()

                if (this.filter.rightType === 'valueOfField') {
                    await this.loadFilterValues()
                }
            }
        },
        resetFilterRightOperandValues() {
            if (this.filter) {
                this.filter.rightOperandDescription = ''
                this.filter.rightOperandLongDescription = ''
                this.filter.rightOperandValue = ['']
                this.filter.rightOperandAlias = ''
            }
        },
        async loadFilterValues() {
            this.loading = true
            await this.$http
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_VALUES_FOR_QBE_FILTER_LOOKUP_ACTION&ENTITY_ID=${this.filter?.leftOperandValue}&SBI_EXECUTION_ID=${this.id}`, {
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8' }
                })
                .then((response: AxiosResponse<any>) => (this.filterValuesData = response.data))
            this.loading = false
        },
        setSelectedValues(selected: string[]) {
            this.selectedValues = selected
            if (this.filter) {
                this.filter.rightOperandValue = selected
                this.filter.rightOperandDescription = selected.join(' ---- ')
                this.filter.rightOperandLongDescription = selected.join(' ---- ')
            }
        },
        onEntityTypeChanged() {
            if (this.filter) {
                const selectedField = this.findSelectedField(this.filter.rightOperandDescription) as any

                this.filter.rightOperandValue = [selectedField?.id]
                this.filter.rightOperandLongDescription = this.filter.rightOperandDescription
                this.filter.rightOperandAlias = selectedField.text
            }
        },
        findSelectedField(fieldDescription: string) {
            let tempField = null

            for (let i = 0; i < this.entities.length && !tempField; i++) {
                for (let j = 0; j < this.entities[i].children.length; j++) {
                    if (this.entities[i].children[j].attributes.longDescription === fieldDescription) {
                        tempField = this.entities[i].children[j]
                        break
                    }
                }
            }

            return tempField
        },
        onSubqeryTargetChange() {
            if (!this.filter || !this.subqueries) return

            const index = this.subqueries.findIndex((subquery: any) => subquery.name === this.filter?.rightOperandDescription)
            if (index !== -1) {
                const subquery = this.subqueries[index] as any
                this.filter.rightOperandValue = [subquery.id]
                this.filter.rightOperandLongDescription = 'Subquery ' + subquery.name
            }
        },
        onManualTimestampChange() {
            const format = this.field.dataType === 'java.sql.Date' ? 'DD/MM/YYYY' : 'DD/MM/YYYY hh:mm'
            if (this.filter) {
                this.filter.rightOperandDescription = this.targetDate instanceof Date ? moment(this.targetDate).format(format) : ''
                this.filter.rightOperandValue[0] = this.targetDate instanceof Date ? moment(this.targetDate).format(format) : ''
            }
        },
        onManualTimestampEndDateChange() {
            const format = this.field.dataType === 'java.sql.Date' ? 'DD/MM/YYYY' : 'DD/MM/YYYY hh:mm'
            if (this.filter) {
                this.filter.rightOperandValue[1] = this.targetDate instanceof Date ? moment(this.targetEndDate).format(format) : ''
            }
        },
        onParameterTargetChange() {
            if (this.filter) {
                this.filter.hasParam = true
                this.filter.paramName = this.filter.rightOperandDescription
                this.filter.rightOperandValue = ['$P{' + this.filter.rightOperandDescription + '}']
                this.filter.rightOperandLongDescription = 'Static Content ' + '$P{' + this.filter.rightOperandDescription + '}'
            }
        },
        getFilterOperatorLabel(value: string) {
            for (let i = 0; i < this.filterOperatorOptions.length; i++) {
                if (this.filterOperatorOptions[i].value === value) {
                    return this.$t(this.filterOperatorOptions[i].label)
                }
            }
            return ''
        }
    }
})
</script>

<style lang="scss">
.qbe-filter-option-value {
    text-transform: capitalize;
}

.qbe-filter-chip-container {
    border-bottom: 1px solid #c2c2c2;
    min-height: 2.775rem;
}

.qbe-filter-time-input {
    flex: 0.3;
}
</style>
