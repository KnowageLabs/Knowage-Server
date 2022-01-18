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
                <Dropdown class="kn-material-input" v-model="filter.operator" :options="QBEFilterDialogDescriptor.operatorValues" optionValue="value">
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
                <label class="kn-material-input-label"> {{ $t('qbe.filters.target') }} </label>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <InputText v-if="filter.rightType === 'manual'" class="kn-material-input" v-model="filter.rightOperandDescription" @input="onManualValueChange" />
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
import Dropdown from 'primevue/dropdown'
import QBEFilterDialogDescriptor from './QBEFilterDialogDescriptor.json'
import QbeFilterValuesTable from './QbeFilterValuesTable.vue'

export default defineComponent({
    name: 'qbe-filter-card',
    components: { CascadeSelect, Chip, Dropdown, QbeFilterValuesTable },
    props: { propFilter: { type: Object as PropType<iFilter> }, id: { type: String }, propEntities: { type: Array } },
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
            this.setFilterRightOperandType()
        },
        loadEntities() {
            this.entities = this.propEntities ? [...this.propEntities] : []
            console.log(' >>> LOADED ENTITIES: ', this.entities)
        },
        async setFilterRightOperandType() {
            switch (this.filter?.rightType) {
                case 'manual':
                    this.filter.rightOperandType = 'Static Content'
                    break
                case 'valueOfField':
                    this.filter.rightOperandType = 'Static Content'
                    this.selectedValues = this.filter.rightOperandValue.filter((el: any) => el !== '')
                    await this.loadFilterValues()
                    break
                case 'anotherEntity':
                    this.filter.rightOperandType = 'Field Content'
                    break
            }
        },
        onManualValueChange() {
            if (this.filter) {
                this.filter.rightOperandValue = [this.filter.rightOperandDescription]
            }
        },
        async onFilterTypeChange() {
            if (this.filter) {
                this.filter.rightOperandDescription = ''
                this.filter.rightOperandValue = ['']
                this.selectedValues = []
                this.filterValuesData = null
                this.setFilterRightOperandType()

                if (this.filter.rightType === 'valueOfField') {
                    await this.loadFilterValues()
                }
            }
        },
        async loadFilterValues() {
            this.loading = true
            await this.$http.get(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_VALUES_FOR_QBE_FILTER_LOOKUP_ACTION&ENTITY_ID=${this.filter?.leftOperandValue}&SBI_EXECUTION_ID=${this.id}`).then((response: AxiosResponse<any>) => (this.filterValuesData = response.data))
            this.loading = false
            console.log('LOADED FILTER VALUES DATA: ', this.filterValuesData)
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
