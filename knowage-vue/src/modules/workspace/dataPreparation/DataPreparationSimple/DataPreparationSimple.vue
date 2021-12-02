<template>
    <div class="simple">
        <span v-for="(field, index) in localTransformation.parameters" v-bind:key="index" class="p-field p-ml-2 kn-flex data-prep-simple-transformation">
            <span v-if="field.type == 'string'" class="p-float-label">
                <InputText :id="field.id" type="text" v-model="field.value" :class="['kn-material-input', field.validationRules && field.validationRules.includes('required') && !field.value ? 'p-invalid' : '']" />
                <label :for="'input_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>
            <span v-if="field.type === 'date'" class="p-float-label">
                <Calendar :id="field.id" v-model="field.value" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }" />
                <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>

            <span v-if="field.type === 'boolean'" class="p-float-label">
                <InputSwitch :id="field.id" v-model="field.value" />
                <label :for="'inputSwitch_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>
            <span v-if="field.type === 'dropdown'" class="p-float-label">
                <Dropdown
                    :id="field.id"
                    v-model="field.value"
                    :options="field.availableOptions ? translate(field) : columns"
                    :showClear="!field.validationRules || (field.validationRules && !field.validationRules.includes('required'))"
                    :optionLabel="field.optionLabel ? field.optionLabel : 'label'"
                    :optionValue="field.optionValue ? field.optionValue : 'code'"
                    :disabled="col && field.name === 'columns'"
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
                    :optionLabel="field.optionLabel ? field.optionLabel : 'label'"
                    display="chip"
                    optionDisabled="disabled"
                    @change="handleMultiSelectChange($event)"
                    :allow-empty="false"
                    :disabled="col"
                    class="kn-material-input"
                    :filter="true"
                    :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }"
                /><label :for="'selectedItems_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.columns') }}</label></span
            >

            <span v-if="field.type == 'textarea'" class="p-float-label">
                <Textarea :id="field.id" v-model="field.value" rows="5" cols="30" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }" :autoResize="false" />
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

import { ITransformation } from '@/modules/workspace/dataPreparation/DataPreparation'
import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'data-preparation-simple',

    props: { col: String, columns: { type: Array as PropType<Array<IDataPreparationColumn>> }, transformation: {} as PropType<ITransformation> },
    components: { Calendar, Dropdown, InputSwitch, MultiSelect, Textarea },
    emits: ['update:transformation'],
    data() {
        return { descriptor: DataPreparationSimpleDescriptor as any, localTransformation: {} as ITransformation, v$: useValidate() as any, dirty: false, currentId: 0 }
    },
    validations() {
        if (this.transformation?.type === 'simple') {
            this.setupLocal()

            let validations = this.descriptor[this.localTransformation.name].validations ? this.descriptor[this.localTransformation.name].validations : []
            return { localTransformation: createValidations('localTransformation', validations) }
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
                let pars = this.localTransformation.type === 'simple' ? this.descriptor[this.localTransformation.name].parameters : []
                pars.forEach((item) => {
                    if (item.name == 'columns' && (item.type === 'multiSelect' || item.type === 'dropdown')) {
                        let localTransformationItemArray = this.localTransformation.parameters.filter((x) => x.name == item.name)
                        if (localTransformationItemArray?.length > 0) {
                            let localTransformationItem = localTransformationItemArray[0]

                            if (this.col) {
                                let selectedItem: Array<IDataPreparationColumn> | undefined = this.columns?.filter((x) => x.header == this.col)
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
                this.localTransformation = this.transformation ? { ...this.transformation } : ({} as ITransformation)

                let name = this.transformation && this.transformation.name ? this.transformation.name : ''
                if (name && this.transformation?.type === 'simple') {
                    let pars = this.descriptor[name].parameters

                    this.localTransformation.parameters = JSON.parse(JSON.stringify(pars))
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
    },

    watch: {
        localTransformation: {
            handler(newValue) {
                this.localTransformation = newValue
                this.$emit('update:transformation', newValue)
            },
            deep: true
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
