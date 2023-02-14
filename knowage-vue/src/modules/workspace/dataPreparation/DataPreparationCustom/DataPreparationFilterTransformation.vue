<template>
    <div class="data-prep-custom-transformation">
        <div class="p-d-flex" v-for="(filter, index) in localTransformation" v-bind:key="index">
            <span class="p-float-label p-field kn-flex">
                <Dropdown v-model="filter.column" :options="columns" class="kn-material-input" optionLabel="fieldAlias" :filter="true" :disabled="col || readOnly" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.column') }}</label>
            </span>
            <span v-if="filter.column" class="p-float-label p-field p-mx-2">
                <Dropdown v-model="filter.condition" :disabled="readOnly" :options="getAvailableConditions(index)" optionLabel="label" optionValue="code" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.conditions') }}</label>
            </span>
            <span v-if="showStartDate(index)" class="p-float-label p-field kn-flex">
                <Calendar v-model="filter.startDate" :disabled="readOnly" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.startDate') }}</label>
            </span>
            <span v-if="showEndDate(index)" class="p-float-label p-field kn-flex">
                <Calendar v-model="filter.endDate" :disabled="readOnly" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.endDate') }}</label>
            </span>
            <span v-if="showInputText(index)" class="p-float-label p-field kn-flex">
                <InputText type="text" v-model="filter.text" :disabled="readOnly" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.text') }}</label>
            </span>
            <span v-if="showInputNumber(index)" class="p-float-label p-field kn-flex">
                <InputText type="number" v-model="filter.number" :disabled="readOnly" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.number') }}</label>
            </span>
            <span v-if="showValuesList(index)" class="kn-flex">
                <span class="p-float-label kn-material-input">
                    <Chips class="kn-width-full" :multiple="true" v-model="filter.valuesList" />
                    <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.values') }}</label>
                </span>
                <small id="chips-help">{{ $t('common.chipsHint') }}</small>
            </span>
            <span> <Button icon="pi pi-trash" :class="'p-button-text p-button-rounded p-button-plain'" @click="deleteRow(index)" v-if="!readOnly && localTransformation.length > 1"/></span>
        </div>
        <span class="p-d-flex p-jc-center p-ai-center">
            <Button v-if="!readOnly" icon="pi pi-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewRow()" />
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import DataPreparationFilterDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCustom/DataPreparationFilterDescriptor.json'
import { IFilterTransformationParameter } from '@/modules/workspace/dataPreparation/DataPreparation'
import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import Chips from 'primevue/chips'

export default defineComponent({
    name: 'data-preparation-filter-transformation',

    props: { columns: { type: Array as PropType<Array<IDataPreparationColumn>> }, col: String, readOnly: Boolean, transformation: {} as any },

    components: { Dropdown, Calendar, Chips },
    emits: ['update:transformation'],
    data() {
        return {
            availableConditions: DataPreparationFilterDescriptor.availableConditions as Array<any>,
            descriptor: DataPreparationFilterDescriptor as any,
            localTransformation: [{}] as Array<IFilterTransformationParameter>
        }
    },
    mounted() {
        this.localTransformation = [{}] as Array<IFilterTransformationParameter>
        if (this.transformation && this.transformation.parameters) {
            for (let i = 0; i < this.transformation.parameters.length; i++) {
                let name = this.transformation.parameters[i]['name']
                let value = this.transformation.parameters[i]['value']
                switch (name) {
                    case 'condition':
                        this.localTransformation[0].condition = value
                        break
                    case 'text':
                        this.localTransformation[0].text = value
                        break
                    case 'startDate':
                        this.localTransformation[0].startDate = value
                        break
                    case 'endDate':
                        this.localTransformation[0].endDate = value
                        break
                    case 'number':
                        this.localTransformation[0].number = value
                        break
                }
            }
        }
        if (this.col && this.columns) this.localTransformation[0].column = this.columns.filter((item) => item.header === this.col)[0]
    },
    methods: {
        addNewRow() {
            var newRow = {} as IFilterTransformationParameter
            if (this.col && this.columns) {
                newRow.column = this.columns.filter((item) => item.header === this.col)[0]
            }
            this.localTransformation.push(newRow)
        },
        deleteRow(index) {
            this.localTransformation.splice(index, 1)
        },
        getAvailableConditions(index) {
            let toReturn = this.availableConditions.filter((item) => {
                let availableForTypes = item.availableForTypes.split('|')
                let type = this.getColType(this.localTransformation[index].column)
                if (availableForTypes.includes(type)) {
                    return true
                } else {
                    return false
                }
            })
            return toReturn
        },
        getColType(col) {
            return this.descriptor.typesMap[col.Type]
        },
        showEndDate(index) {
            let allowedConditions = ['between', 'before']
            return this.isShowDate(index, allowedConditions)
        },
        isShowDate(index, allowedConditions) {
            if (!this.localTransformation[index].column || !this.localTransformation[index].condition) return false
            let colType = this.getColType(this.localTransformation[index].column)
            if ((colType == 'date' || colType == 'timestamp') && allowedConditions.includes(this.localTransformation[index].condition)) return true
            else return false
        },
        showStartDate(index) {
            let allowedConditions = ['between', 'after']
            return this.isShowDate(index, allowedConditions)
        },
        showInputText(index) {
            let excludedConditions = ['isNull', 'isNotNull', 'in', 'notIn']
            if (!this.localTransformation[index].column || !this.localTransformation[index].condition) return false
            let colType = this.getColType(this.localTransformation[index].column)
            if (colType == 'string' && !excludedConditions.includes(this.localTransformation[index].condition)) return true
            else return false
        },
        showInputNumber(index) {
            let excludedConditions = ['in', 'notIn']
            if (!this.localTransformation[index].column || !this.localTransformation[index].condition) return false
            let colType = this.getColType(this.localTransformation[index].column)
            if (colType == 'number' && !excludedConditions.includes(this.localTransformation[index].condition)) return true
            else return false
        },
        showValuesList(index) {
            let allowedConditions = ['in', 'notIn']
            if (!this.localTransformation[index].column || !this.localTransformation[index].condition) return false
            if (allowedConditions.includes(this.localTransformation[index].condition)) return true
            else return false
        }
    },
    watch: {
        localTransformation: {
            handler(newValue) {
                this.$emit('update:transformation', newValue)
            },
            deep: true
        }
    }
})
</script>
<style lang="scss">
.data-prep-custom-transformation {
    .p-multiselect,
    .p-inputtext,
    .p-dropdown {
        width: 100%;
    }
}
</style>
