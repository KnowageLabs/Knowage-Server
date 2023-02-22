<template>
    <div class="simple">
        <span v-for="(field, index) in localTransformation.parameters" :key="index" class="p-field p-ml-2 kn-flex data-prep-simple-transformation">
            <span v-if="field.type == 'string'" class="p-float-label">
                <InputText :id="field.id" v-model="field.value" type="text" :disabled="readOnly" :class="['kn-material-input', field.validationRules && field.validationRules.includes('required') && !field.value ? 'p-invalid' : '']" />
                <label :for="'input_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>
            <span v-if="field.type === 'date'" class="p-float-label">
                <Calendar :id="field.id" v-model="field.value" :disabled="readOnly" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }" />
                <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>

            <span v-if="field.type === 'boolean'" class="p-float-label">
                <InputSwitch :id="field.id" v-model="field.value" :disabled="readOnly" />
                <label :for="'inputSwitch_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>
            <span v-if="field.type === 'dropdown'" class="p-float-label">
                <Dropdown
                    :id="field.id"
                    v-model="field.value"
                    :options="field.availableOptions ? translate(field) : columns"
                    :show-clear="!field.validationRules || (field.validationRules && !field.validationRules.includes('required'))"
                    :option-label="field.optionLabel ? field.optionLabel : 'label'"
                    :option-value="field.optionValue ? field.optionValue : 'code'"
                    :disabled="(col && field.name === 'columns') || readOnly"
                    class="kn-material-input"
                    :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }"
                />
                <label :for="'selectedCondition_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>

            <span v-if="field.type == 'multiSelect'" class="p-float-label">
                <MultiSelect
                    :id="field.id"
                    v-model="field.value"
                    :options="columns"
                    :option-label="field.optionLabel ? field.optionLabel : 'label'"
                    display="chip"
                    option-disabled="disabled"
                    :allow-empty="false"
                    :disabled="col || readOnly"
                    class="kn-material-input"
                    :filter="true"
                    :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }"
                    @change="handleMultiSelectChange($event)"
                /><label :for="'selectedItems_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.columns') }}</label></span
            >

            <span v-if="field.type == 'textarea'" class="p-float-label">
                <Textarea :id="field.id" v-model="field.value" :disabled="readOnly" rows="5" cols="30" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }" :auto-resize="false" />
                <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import DataPreparationSimpleDescriptor from '@/modules/workspace/dataPreparation/DataPreparationSimple/DataPreparationSimpleDescriptor.json'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'
import Textarea from 'primevue/textarea'

import { ITransformation, ITransformationParameter } from '@/modules/workspace/dataPreparation/DataPreparation'
import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'data-preparation-simple',
    components: { Calendar, Dropdown, InputSwitch, MultiSelect, Textarea },

    props: { readOnly: Boolean, col: String, columns: { type: Array as PropType<Array<IDataPreparationColumn>> }, transformation: {} as PropType<ITransformation<ITransformationParameter>> },
    emits: ['update:transformation'],
    data() {
        return { descriptor: DataPreparationSimpleDescriptor as any, localTransformation: {} as ITransformation<ITransformationParameter>, v$: useValidate() as any, dirty: false, currentId: 0 }
    },
    validations() {
        if (this.transformation?.type === 'simple') {
            this.setupLocal()

            const validations = this.descriptor[this.localTransformation.name].validations ? this.descriptor[this.localTransformation.name].validations : []
            return { localTransformation: createValidations('localTransformation', validations) }
        }
    },

    watch: {
        localTransformation: {
            handler(newValue) {
                this.localTransformation = newValue
                this.$emit('update:transformation', newValue)
            },
            deep: true
        }
    },

    mounted() {
        this.setupLocal()
    },
    methods: {
        handleMultiSelectChange(e: Event): void {
            if (e) {
                this.refreshTransfrormation()
            }
        },

        refreshTransfrormation(): void {
            if (this.localTransformation) {
                const pars = this.localTransformation.type === 'simple' ? this.descriptor[this.localTransformation.name].parameters : []
                pars.forEach((item) => {
                    if (item.name == 'columns' && (item.type === 'multiSelect' || item.type === 'dropdown')) {
                        const localTransformationItemArray = this.localTransformation.parameters.filter((x) => x.name == item.name)
                        if (localTransformationItemArray?.length > 0) {
                            const localTransformationItem = localTransformationItemArray[0]

                            if (this.col) {
                                const selectedItem: Array<IDataPreparationColumn> | undefined = this.columns?.filter((x) => x.header == this.col)
                                if (selectedItem && selectedItem.length > 0) {
                                    selectedItem[0].disabled = true
                                    localTransformationItem.value = item.type === 'multiSelect' ? selectedItem : selectedItem[0][item.optionValue]
                                }
                            } else {
                                this.columns?.forEach((e) => (e.disabled = false))
                            }
                        }
                    }
                })
            }
        },
        setupLocal() {
            if (Object.keys(this.localTransformation).length == 0) {
                this.localTransformation = this.transformation ? { ...this.transformation } : ({} as ITransformation<ITransformationParameter>)

                const name = this.transformation && this.transformation.name ? this.transformation.name : ''
                if (name && this.transformation?.type === 'simple') {
                    const pars = JSON.parse(JSON.stringify(this.descriptor[name].parameters))
                    if (this.readOnly) {
                        for (let i = 0; i < pars.length; i++) {
                            const parName = pars[i]['name']
                            for (let j = 0; j < this.localTransformation.parameters.length; j++) {
                                if (this.localTransformation.parameters[j]['name'] == parName) pars[i]['value'] = this.localTransformation.parameters[j]['value']
                            }
                        }
                    }
                    this.localTransformation.parameters = pars
                }

                this.refreshTransfrormation()
            }
        },
        translate(item): [] {
            const items = item.availableOptions
            items.forEach((element) => {
                element.label = this.$t(element.label)
            })

            return items
        }
    }
})
</script>

<style lang="scss" scoped>
.data-prep-simple-transformation {
    &.p-field {
        .p-multiselect,
        .p-inputtext,
        .p-dropdown {
            width: 100%;
        }
    }
}

.simple {
    min-width: 600px;
    width: 60%;
    max-width: 1200px;
    min-height: 150px;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    &:deep(.p-dialog-content) {
        @extend .simple;
    }
    .elementClass {
        flex-direction: column;
    }
}
</style>
