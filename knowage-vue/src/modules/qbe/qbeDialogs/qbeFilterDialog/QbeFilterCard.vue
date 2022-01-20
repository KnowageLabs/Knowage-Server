<template>
    {{ filter }}
    <div v-if="filter">
        <div class="p-grid p-m-2">
            <div class="p-col-4">
                <label class="kn-material-input-label"> {{ $t('common.field') }} </label>
                <InputText class="kn-material-input" v-model="filter.leftOperandDescription" :disabled="true" />
            </div>

            <div class="p-col-2">
                <label class="kn-material-input-label" v-tooltip.top="$t('qbe.filters.conditionTooltip')"> {{ $t('qbe.filters.condition') }} </label>
                <Dropdown class="kn-material-input" v-model="filter.operator" :options="QBEFilterDialogDescriptor.operatorValues" optionValue="value" @click="onFilterOperatorChange">
                    <template #value="slotProps">
                        <div v-if="slotProps.value">
                            <span class="qbe-filter-option-value">{{ slotProps.value.toLowerCase() }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>

            <div class="p-col-2">
                <label class="kn-material-input-label"> {{ $t('qbe.filters.targetType') }} </label>
                <Dropdown class="kn-material-input" v-model="filter.rightType" :options="targetValues" optionValue="value" optionLabel="label" @change="onFilterTypeChange" />
            </div>

            <div class="p-col-4">
                <label class="kn-material-input-label" v-show="!(filter.rightType === 'manual' && ['BETWEEN', 'NOT BETWEEN', 'IN', 'NOT IN'].includes(filter.operator))"> {{ $t('qbe.filters.target') }} </label>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <div v-if="filter.rightType === 'manual' && ['BETWEEN', 'NOT BETWEEN'].includes(filter.operator)" class="p-d-flex p-flex-row p-ai-center p-mt-3">
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
                    <div v-else-if="filter.rightType === 'manual' && ['IN', 'NOT IN'].includes(filter.operator)" class="kn-width-full">
                        <label class="kn-material-input-label"> {{ $t('qbe.filters.enterValue') }} </label>
                        <Chips v-model="multiManualValues" @add="onManualMultivalueChanged" @remove="onManualMultivalueChanged" />
                    </div>
                    <InputText v-else-if="filter.rightType === 'manual'" class="kn-material-input" v-model="filter.rightOperandDescription" @input="onManualValueChange" />

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

                    <i v-if="filter.rightType === 'valueOfField'" class="fa fa-check kn-cursor-pointer p-ml-2" @click="loadFilterValues"></i>
                    <i class="fa fa-eraser kn-cursor-pointer p-ml-2" @click="$emit('removeFilter', filter)"></i>
                </div>
            </div>
        </div>
        <QbeFilterValuesTable v-show="filter.rightType === 'valueOfField'" class="p-m-2" :filterValuesData="filterValuesData" :loadedSelectedValues="selectedValues" :loading="loading" @selected="setSelectedValues"></QbeFilterValuesTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iFilter } from '../../QBE'
import CascadeSelect from 'primevue/cascadeselect'
import Chip from 'primevue/chip'
import Chips from 'primevue/chips'
import Dropdown from 'primevue/dropdown'
import QBEFilterDialogDescriptor from './QBEFilterDialogDescriptor.json'
import QbeFilterValuesTable from './QbeFilterValuesTable.vue'

export default defineComponent({
    name: 'qbe-filter-card',
    components: { CascadeSelect, Chip, Chips, Dropdown, QbeFilterValuesTable },
    props: { propFilter: { type: Object as PropType<iFilter> }, id: { type: String }, propEntities: { type: Array }, subqueries: { type: Array } },
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
            loading: false
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        },
        propEntities() {
            this.loadEntities()
        }
    },
    created() {
        this.loadFilter()
        this.loadEntities()
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
        },
        loadEntities() {
            this.entities = this.propEntities ? [...this.propEntities] : []
            console.log(' >>> LOADED ENTITIES: ', this.entities)
        },
        async formatFilter() {
            switch (this.filter?.rightType) {
                case 'manual':
                    this.filter.rightOperandType = 'Static Content'

                    if (['BETWEEN', 'NOT BETWEEN'].includes(this.filter.operator)) {
                        this.firstOperand = this.filter.rightOperandValue[0]
                        this.secondOperand = this.filter.rightOperandValue[1]
                    } else if (['IN', 'NOT IN'].includes(this.filter.operator)) {
                        this.multiManualValues = [...this.filter.rightOperandValue]
                    }

                    break
                case 'valueOfField':
                    this.filter.rightOperandType = 'Static Content'
                    this.selectedValues = this.filter.rightOperandValue.filter((el: any) => el !== '')
                    await this.loadFilterValues()
                    break
                case 'anotherEntity':
                    this.filter.rightOperandType = 'Field Content'
                    break
                case 'subquery':
                    this.filter.rightOperandType = 'Subquery'
                    break
            }
        },
        onFilterOperatorChange() {
            if (this.filter && this.filter.rightType === 'manual') {
                switch (this.filter.operator) {
                    case 'BETWEEN':
                    case 'NOT BETWEEN':
                        this.filter.rightOperandDescription = ''
                        this.multiManualValues = []
                        this.resetFilterRightOperandValues()
                        break
                    case 'IN':
                    case 'NOT IN':
                        this.filter.rightOperandDescription = ''
                        this.firstOperand = ''
                        this.secondOperand = ''
                        this.resetFilterRightOperandValues()
                        break
                    default:
                        this.multiManualValues = []
                        this.firstOperand = ''
                        this.secondOperand = ''
                }
            }
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
            await this.$http.get(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_VALUES_FOR_QBE_FILTER_LOOKUP_ACTION&ENTITY_ID=${this.filter?.leftOperandValue}&SBI_EXECUTION_ID=${this.id}`).then((response: AxiosResponse<any>) => (this.filterValuesData = response.data))
            this.loading = false
            // console.log('LOADED FILTER VALUES DATA: ', this.filterValuesData)
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
                console.log('FILTER CHANGED: ', this.filter)
                const selectedField = this.findSelectedField(this.filter.rightOperandDescription) as any

                console.log('SELECETED FIELD: ', selectedField)

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
            console.log(' >>> FILTER SUB ID: ', this.filter?.rightOperandDescription)
            if (!this.filter || !this.subqueries) return

            const index = this.subqueries.findIndex((subquery: any) => subquery.name === this.filter?.rightOperandDescription)
            console.log('INDEX: ', index)
            if (index !== -1) {
                const subquery = this.subqueries[index] as any
                this.filter.rightOperandValue = [subquery.id]
                this.filter.rightOperandLongDescription = 'Subquery ' + subquery.name
            }
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
</style>
