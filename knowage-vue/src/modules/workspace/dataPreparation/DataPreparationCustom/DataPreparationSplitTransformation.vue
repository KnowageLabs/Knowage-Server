<template>
    <div class="data-prep-custom-transformation">
        <div class="p-d-flex">
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <Dropdown v-model="localTransformation.column" :options="getStringColumns(columns)" class="kn-material-input" optionLabel="fieldAlias" :filter="true" :disabled="col || readOnly" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.column') }}</label>
            </span>
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <Dropdown v-model="localTransformation.condition" :disabled="readOnly" :options="availableConditions" :optionLabel="translatedLabel" optionValue="code" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.conditions') }}</label>
            </span>
            <span v-if="localTransformation.condition === 'numberOfChars'" class="p-float-label p-field p-ml-2 kn-flex">
                <InputText type="number" v-model="localTransformation.numOfChars" :disabled="readOnly" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.numOfChars') }}</label>
            </span>
            <span v-if="localTransformation.condition === 'separator'" class="p-float-label p-field p-ml-2 kn-flex">
                <InputText type="text" v-model="localTransformation.separator" :disabled="readOnly" class="kn-material-input" />
                <label class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.separator') }}</label>
            </span>
        </div>
        <Fieldset :legend="$t('managers.workspaceManagement.dataPreparation.transformations.split.outputColumn') + ' 1'">
            <div class="p-d-flex">
                <span class="p-float-label p-field p-ml-2 kn-flex">
                    <InputText type="text" v-model="localTransformation.outputColumn1" :disabled="readOnly" class="kn-material-input" />
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
                    <InputText type="text" v-model="localTransformation.outputColumn2" :disabled="readOnly" class="kn-material-input" />
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

    props: { columns: { type: Array as PropType<Array<IDataPreparationColumn>> }, col: String, readOnly: Boolean, transformation: {} as any },

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
        if (this.readOnly && this.transformation && this.transformation.parameters) {
            for (let i = 0; i < this.transformation.parameters.length; i++) {
                if (this.transformation.parameters[i]['name'] == 'condition') this.localTransformation.condition = this.transformation.parameters[i]['value']
                else if (this.transformation.parameters[i]['name'] == 'numOfChars') this.localTransformation.numOfChars = this.transformation.parameters[i]['value']
                else if (this.transformation.parameters[i]['name'] == 'separator') this.localTransformation.separator = this.transformation.parameters[i]['value']
                else if (this.transformation.parameters[i]['name'] == 'outputColumn1') this.localTransformation.outputColumn1 = this.transformation.parameters[i]['value']
                else if (this.transformation.parameters[i]['name'] == 'separator') this.localTransformation.separator = this.transformation.parameters[i]['value']
                else if (this.transformation.parameters[i]['name'] == 'outputColumn1') this.localTransformation.outputColumn1 = this.transformation.parameters[i]['value']
                else if (this.transformation.parameters[i]['name'] == 'outputColumn2') this.localTransformation.outputColumn2 = this.transformation.parameters[i]['value']
                else if (this.transformation.parameters[i]['name'] == 'outputType1') this.localTransformation.outputType1 = this.transformation.parameters[i]['value']
                else if (this.transformation.parameters[i]['name'] == 'outputType2') this.localTransformation.outputType2 = this.transformation.parameters[i]['value']
                else if (this.transformation.parameters[i]['name'] == 'columns') this.localTransformation.column = this.transformation.parameters[i]['value'][0]
            }
        }
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
