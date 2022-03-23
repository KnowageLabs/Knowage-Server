<template>
    <div class="data-prep-custom-transformation">
        <div class="p-d-flex">
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <Dropdown v-model="localTransformation.column" :options="getStringColumns(columns)" class="kn-material-input" optionLabel="fieldAlias" :filter="true" :disabled="col" />
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
        <Fieldset :legend="$t('managers.workspaceManagement.dataPreparation.transformations.split.outputColumn') + ' 1'">
            <div class="p-d-flex">
                <span class="p-float-label p-field p-ml-2 kn-flex">
                    <InputText type="text" v-model="localTransformation.outputColumn1" class="kn-material-input" />
                    <label class="kn-material-input-label">{{ $t('common.name') }}</label>
                </span>
                <!-- <span class="p-float-label p-field p-ml-2 kn-flex">
                    <Dropdown v-model="localTransformation.outputType1" :options="availableOutputTypes" optionLabel="label" optionValue="code" class="kn-material-input" />
                    <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.split.outputType') }}</label>
                </span> -->
            </div>
        </Fieldset>
        <Fieldset :legend="$t('managers.workspaceManagement.dataPreparation.transformations.split.outputColumn') + ' 2'">
            <div class="p-d-flex">
                <span class="p-float-label p-field p-ml-2 kn-flex">
                    <InputText type="text" v-model="localTransformation.outputColumn2" class="kn-material-input" />
                    <label class="kn-material-input-label">{{ $t('common.name') }}</label>
                </span>
                <!-- <span class="p-float-label p-field p-ml-2 kn-flex">
                    <Dropdown v-model="localTransformation.outputType2" :options="availableOutputTypes" optionLabel="label" optionValue="code" class="kn-material-input" />
                    <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.split.outputType') }}</label>
                </span> -->
            </div>
        </Fieldset>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import DataPreparationSplitDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCustom/DataPreparationSplitDescriptor.json'
import { ISplitTransformationParameter } from '@/modules/workspace/dataPreparation/DataPreparation'
import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'
import Dropdown from 'primevue/dropdown'
import Fieldset from 'primevue/fieldset'

export default defineComponent({
    name: 'data-preparation-split-transformation',

    props: { columns: { type: Array as PropType<Array<IDataPreparationColumn>> }, col: String },

    components: { Dropdown, Fieldset },
    emits: ['update:transformation'],
    data() {
        return {
            availableConditions: DataPreparationSplitDescriptor.condition.availableOptions as Array<any>,
            availableOutputTypes: DataPreparationSplitDescriptor.availableOutputTypes as Array<any>,
            descriptor: DataPreparationSplitDescriptor as any,
            localTransformation: {} as ISplitTransformationParameter
        }
    },
    mounted() {
        this.localTransformation = {} as ISplitTransformationParameter
    },
    methods: {
        getStringColumns(columns: Array<IDataPreparationColumn>): Array<IDataPreparationColumn> {
            return columns.filter((x) => x.Type == 'java.lang.String')
        },
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
