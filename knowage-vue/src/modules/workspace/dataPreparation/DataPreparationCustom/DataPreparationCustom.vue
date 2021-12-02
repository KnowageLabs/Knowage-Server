<template>
    <div v-for="(field, index) in localTransformation.parameters" v-bind:key="index" class="p-field data-prep-custom-transformation">
        <span v-if="field.type == 'string' && (!field.dependsFromField || (field.dependsFromField && isFieldVisible(field)))" class="p-float-label">
            <InputText :id="field.id" type="text" v-model="field.value" :class="['kn-material-input', field.validationRules && field.validationRules.includes('required') && !field.value ? 'p-invalid' : '']" />
            <label :for="'input' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>
        <span v-if="field.type === 'date' && (!field.dependsFromField || (field.dependsFromField && isFieldVisible(field)))" class="p-float-label">
            <Calendar :id="field.id" v-model="field.value" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }" />
            <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>

        <span v-if="field.type === 'boolean' && (!field.dependsFromField || (field.dependsFromField && isFieldVisible(field)))" class="p-float-label">
            <InputSwitch :id="field.id" v-model="field.value" />
            <label :for="'inputSwitch' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>
        <span v-if="field.type === 'dropdown' && (!field.dependsFromField || (field.dependsFromField && isFieldVisible(field)))" class="p-float-label">
            <Dropdown
                :id="field.id"
                v-model="field.value"
                :options="getAvailableOptions(field, columns)"
                :showClear="!field.validationRules || (field.validationRules && !field.validationRules.includes('required'))"
                :optionLabel="field.optionLabel ? field.optionLabel : 'label'"
                :optionValue="field.optionValue ? field.optionValue : 'code'"
                class="kn-material-input"
                :disabled="col && field.name === 'columns'"
                :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }"
                @change="handleSelectChange($event)"
            />
            <label :for="'selectedCondition' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>

        <span v-if="field.type == 'multiSelect' && (!field.dependsFromField || (field.dependsFromField && isFieldVisible(field)))" class="p-float-label">
            <MultiSelect
                :id="field.id"
                v-model="field.value"
                :options="columns"
                optionLabel="header"
                display="chip"
                optionDisabled="disabled"
                @change="handleSelectChange($event)"
                :allow-empty="false"
                :disabled="col"
                class="kn-material-input"
                :filter="true"
                :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }"
            /><label :for="'selectedItems' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.columns') }}</label></span
        >

        <span v-if="field.type == 'textarea'" class="p-float-label">
            <Textarea :id="field.id" v-model="field.value" rows="5" cols="30" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field.value }" :autoResize="false" />
            <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            <KnTextarea
                :id="field.id"
                v-model="field.value"
                rows="5"
                cols="30"
                :name="field.type + '_index_' + index"
                :label="$t('managers.workspaceManagement.dataPreparation.transformations.' + field.name)"
                :autoResize="false"
                :required="field.validationRules && field.validationRules.includes('required')"
            />
        </span>
        <span>
            <Button
                icon="pi pi-trash"
                :class="'p-button-text p-button-rounded p-button-plain ' + (localTransformation.parameters.length > 1 ? '' : 'kn-hide')"
                @click="deleteRow(index)"
                v-if="localTransformation.name === 'filter' && index % descriptor[localTransformation.name].parameters.length == descriptor[localTransformation.name].parameters.length - 1"
        /></span>
    </div>
    <span class="p-d-flex p-jc-center p-ai-center" v-if="localTransformation.name === 'filter'">
        <Button icon="pi pi-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewRow()" />
    </span>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import DataPreparationCustomDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCustom/DataPreparationCustomDescriptor.json'
import DataPreparationDescriptor from '@/modules/workspace/dataPreparation/DataPreparationDescriptor.json'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'
import KnTextarea from '@/components/UI/KnTextarea.vue'
import Textarea from 'primevue/textarea'

import { ITransformation } from '@/modules/workspace/dataPreparation/DataPreparation'
import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'

export default defineComponent({
    name: 'data-preparation-custom',

    props: { col: String, columns: { type: Array as PropType<Array<IDataPreparationColumn>> }, transformation: {} as PropType<ITransformation> },
    components: { Calendar, Dropdown, InputSwitch, MultiSelect, Textarea, KnTextarea },
    emits: ['update:transformation'],
    data() {
        return { descriptor: DataPreparationCustomDescriptor as any, dataPreparationDescriptor: DataPreparationDescriptor as any, localTransformation: {} as ITransformation, currentId: 0 }
    },
    mounted() {
        this.setupLocal()
    },
    methods: {
        addNewRow() {
            this.descriptor[this.localTransformation.name].parameters.forEach((x) => {
                let tmp = JSON.parse(JSON.stringify(x))

                this.localTransformation?.parameters.push(tmp)
            })
        },
        deleteRow(index) {
            if (this.localTransformation) {
                let parLength = this.descriptor[this.localTransformation.name].parameters.length
                if (this.localTransformation.parameters.length > 1) this.localTransformation.parameters.splice(index - parLength + 1, parLength)
            }
        },

        handleRelatedFields(pars, item) {
            let relatedFieldArray = pars.filter((x) => {
                return x.dependsFromField === item.name
            })

            if (relatedFieldArray && relatedFieldArray.length > 0) {
                let relatedField = relatedFieldArray[0]
                let itemValue = this.localTransformation.parameters.filter((x) => x.name === item.name)
                if (itemValue && itemValue.length > 0) {
                    let column = this.columns?.filter((x) => x.header === itemValue[0].value)
                    if (column && column.length > 0) {
                        let type = this.dataPreparationDescriptor.typeMap[column[0].Type]
                        let currentAvailableOptions = relatedField.availableOptions.filter((x) => x.availableForTypes.split('|').includes(type))
                        this.localTransformation.parameters
                            .filter((x) => x.name === relatedField.name)[0]
                            .availableOptions.forEach((option) => {
                                option.visible = false
                                currentAvailableOptions.forEach((available) => {
                                    if (available.code === option.code) option.visible = true
                                })
                            })
                    }
                }
                this.translate(relatedField)
            }
        },
        handleSelectChange(e: Event): void {
            if (e) {
                this.localTransformation.parameters.forEach((item) => {
                    this.handleRelatedFields(this.localTransformation.parameters, item)
                })
            }
        },
        isFieldVisible(field): boolean {
            let visible = true
            if (field.dependsFromField && this.localTransformation) {
                let objArr = this.localTransformation.parameters.filter((x) => x.name === field.dependsFromField)

                if (objArr?.length > 0) {
                    if (!objArr[0].value) return false

                    if (field.dependsFromOptions) visible = visible && field.dependsFromOptions.split('|').includes(objArr[0].value)
                }
            }
            return visible
        },

        getAvailableOptions(field, columns) {
            if (!field.availableOptions) return columns
            else return field.availableOptions.filter((x) => x.visible)
        },

        refreshTransfrormation(): void {
            if (this.localTransformation) {
                let pars = this.localTransformation.type === 'custom' ? this.descriptor[this.localTransformation.name].parameters : []
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
                this.localTransformation.parameters.forEach((item) => {
                    this.handleRelatedFields(this.localTransformation.parameters, item)
                })
            }
        },
        setupLocal(): void {
            this.localTransformation = this.transformation ? { ...this.transformation } : ({} as ITransformation)

            this.descriptor = { ...DataPreparationCustomDescriptor } as any

            let name = this.transformation && this.transformation.name ? this.transformation.name : ''
            if (name && this.transformation?.type === 'custom') {
                let pars = this.descriptor[name].parameters

                this.localTransformation.parameters = JSON.parse(JSON.stringify(pars))
                this.refreshTransfrormation()
            }
        },
        translate(item): [] {
            return item.availableOptions?.forEach((element) => {
                element.label = this.$t(element.label)
            })
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
    .p-field {
        .p-multiselect,
        .p-inputtext,
        .p-dropdown {
            width: 100%;
        }
    }
}

.elementClass {
    flex-direction: column;
}
</style>
