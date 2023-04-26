<template>
    <div v-if="having">
        <div class="p-grid p-m-2">
            <div class="p-col-3">
                <label class="kn-material-input-label"> {{ $t('common.field') }} </label>
                <InputText v-model="having.leftOperandDescription" class="kn-material-input" :disabled="true" />
            </div>

            <div class="p-col-2">
                <label v-tooltip.top="$t('qbe.filters.conditionTooltip')" class="kn-material-input-label"> {{ $t('qbe.filters.condition') }} </label>
                <Dropdown v-model="having.operator" class="kn-material-input" :options="QBEHavingDialogDescriptor.operatorValues" option-value="value">
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
                <Dropdown v-model="having.rightType" class="kn-material-input" :options="targetValues" option-value="value" option-label="label" @change="onHavingTypeChange" />
            </div>

            <div class="p-col-3">
                <label class="kn-material-input-label"> {{ $t('qbe.filters.target') }} </label>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <InputText v-if="having.rightType === ''" v-model="having.rightOperandDescription" class="kn-material-input" @input="onManualValueChange" />
                    <Dropdown v-else-if="having.rightType === 'anotherEntity'" v-model="having.rightOperandDescription" class="kn-material-input kn-flex" :options="entities" option-label="field" option-value="id" @change="onEntityTypeChanged" />
                </div>
            </div>

            <div class="p-col-2">
                <label class="kn-material-input-label"> {{ $t('qbe.filters.target') }} </label>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <Dropdown v-model="having.booleanConnector" class="kn-material-input kn-flex" :options="QBEHavingDialogDescriptor.booleanConnectors" />
                    <i class="fa fa-eraser kn-cursor-pointer p-ml-2" @click="$emit('removeHaving', having)"></i>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iFilter } from '../../QBE'
import Dropdown from 'primevue/dropdown'
import QBEHavingDialogDescriptor from './QBEHavingDialogDescriptor.json'

export default defineComponent({
    name: 'qbe-filter-card',
    components: { Dropdown },
    props: { propHaving: { type: Object as PropType<iFilter> }, entities: { type: Array } },
    emits: ['removeHaving'],
    data() {
        return {
            QBEHavingDialogDescriptor,
            having: null as iFilter | null,
            targetValues: [
                {
                    label: this.$t('qbe.filters.targets.manual'),
                    value: ''
                },
                {
                    label: this.$t('qbe.filters.targets.entity'),
                    value: 'anotherEntity'
                }
            ]
        }
    },
    watch: {
        propHaving() {
            this.loadHaving()
        }
    },
    created() {
        this.loadHaving()
    },
    methods: {
        loadHaving() {
            this.having = this.propHaving as iFilter
        },
        onHavingTypeChange() {
            if (this.having) {
                this.having.rightOperandDescription = ''
                this.having.rightOperandLongDescription = ''
                this.having.rightOperandAggregator = ''
                this.having.rightOperandType = this.having.rightType === 'anotherEntity' ? 'Field Content' : 'Static Content'
            }
        },
        onManualValueChange() {
            if (this.having) {
                this.having.rightOperandValue = [this.having.rightOperandDescription]
            }
        },
        onEntityTypeChanged() {
            if (this.having) {
                if (this.having.rightOperandType === 'Field Content' && this.entities) {
                    let rightOperand = null as any
                    const index = this.entities.findIndex((entity: any) => this.having?.rightOperandDescription === entity.id)
                    if (index !== -1) rightOperand = this.entities[index]
                    this.having.rightOperandAggregator = rightOperand.funct
                }

                this.having.rightOperandValue = [this.having.rightOperandDescription]
                this.having.rightOperandLongDescription = this.having.rightOperandDescription
            }
        }
    }
})
</script>
