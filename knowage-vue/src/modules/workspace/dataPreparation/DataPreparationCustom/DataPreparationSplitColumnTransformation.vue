<template>
    <div class="data-prep-custom-transformation">
        <div class="p-d-flex">
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <Dropdown v-model="localTransformation.column" :options="columns" class="kn-material-input" optionLabel="fieldAlias" :filter="true" :disabled="col" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.column') }}</label>
            </span>
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <Dropdown v-model="localTransformation.condition" :options="availableConditions" :optionLabel="translatedLabel" optionValue="code" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.conditions') }}</label>
            </span>
            <span v-if="localTransformation.condition === 'numberOfChars'" class="p-float-label p-field p-ml-2 kn-flex">
                <InputText type="number" v-model="localTransformation.numOfChars" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.numOfChars') }}</label>
            </span>
            <span v-if="localTransformation.condition === 'separator'" class="p-float-label p-field p-ml-2 kn-flex">
                <InputText type="text" v-model="localTransformation.separator" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.separator') }}</label>
            </span>
        </div>
        <div class="p-d-flex">
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <InputText type="text" v-model="localTransformation.outputColumn1" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.splitColumn.outputColumn') + ' 1' }}</label>
            </span>
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <Dropdown v-model="localTransformation.outputType1" :options="availableOutputTypes" optionLabel="label" optionValue="code" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.splitColumn.outputType') + ' 1' }}</label>
            </span>
        </div>
        <div class="p-d-flex">
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <InputText type="text" v-model="localTransformation.outputColumn2" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.splitColumn.outputColumn') + ' 2' }}</label>
            </span>
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <Dropdown v-model="localTransformation.outputType2" :options="availableOutputTypes" optionLabel="label" optionValue="code" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.splitColumn.outputType') + ' 2' }}</label>
            </span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import DataPreparationSplitColumnDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCustom/DataPreparationSplitColumnDescriptor.json'
import { ISplitTransformationParameter } from '@/modules/workspace/dataPreparation/DataPreparation'
import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'data-preparation-split-transformation',

    props: { columns: { type: Array as PropType<Array<IDataPreparationColumn>> }, col: String },

    components: { Dropdown },
    emits: ['update:transformation'],
    data() {
        return {
            availableConditions: DataPreparationSplitColumnDescriptor.condition.availableOptions as Array<any>,
            availableOutputTypes: DataPreparationSplitColumnDescriptor.availableOutputTypes as Array<any>,
            descriptor: DataPreparationSplitColumnDescriptor as any,
            localTransformation: {} as ISplitTransformationParameter
        }
    },
    mounted() {
        this.localTransformation = {} as ISplitTransformationParameter
    },
    methods: {
        translatedLabel(item) {
            return this.$t(item.label)
        }
    },
    watch: {
        localTransformation: {
            handler(newValue, oldValue) {
                if (oldValue !== newValue) {
                    this.$emit('update:transformation', newValue)
                }
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
