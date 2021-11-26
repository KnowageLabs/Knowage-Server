<template>
    <div v-for="(field, index) in localTransformation.parameters" v-bind:key="index" class="p-field p-ml-2 kn-flex">
        <span v-if="field.type == 'string' && (!field.relatedWith || (field.relatedWith && isFieldVisible(field)))" class="p-float-label">
            <InputText :id="name" type="text" v-model="localTransformation[field.name]" :class="['kn-material-input', field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] ? 'p-invalid' : '']" />
            <label :for="'input' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>
        <span v-if="field.type === 'calendar'" class="p-float-label">
            <Calendar v-model="localTransformation[field.name]" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }" />
            <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>

        <span v-if="field.type === 'boolean'" class="p-float-label">
            <InputSwitch v-model="localTransformation[field.name]" />
            <label :for="'inputSwitch' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>
        <span v-if="field.type === 'dropdown'" class="p-float-label">
            <Dropdown
                v-model="localTransformation[field.name]"
                :options="field.availableOptions ? field.availableOptions : columns"
                :showClear="!field.validationRules || (field.validationRules && !field.validationRules.includes('required'))"
                :optionLabel="field.optionLabel ? field.optionLabel : 'label'"
                :optionValue="field.optionValue ? field.optionValue : 'code'"
                class="kn-material-input"
                :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }"
            />
            <label :for="'selectedCondition' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>

        <span v-if="field.type == 'multiSelect'" class="p-float-label">
            <MultiSelect
                v-model="localTransformation[field.name]"
                :options="columns"
                optionLabel="header"
                display="chip"
                optionDisabled="disabled"
                @change="handleMultiSelectChange($event)"
                :allow-empty="false"
                :disabled="col"
                class="kn-material-input"
                :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }"
            /><label :for="'selectedItems' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.columns') }}</label></span
        >

        <span v-if="field.type == 'textarea'" class="p-float-label">
            <Textarea v-model="localTransformation[field.name]" rows="5" cols="30" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }" :autoResize="false" />
            <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            <KnTextarea
                v-model="localTransformation[field.name]"
                rows="5"
                cols="30"
                :name="field.type + '_index_' + index"
                :label="$t('managers.workspaceManagement.dataPreparation.transformations.' + field.name)"
                :autoResize="false"
                :required="field.validationRules && field.validationRules.includes('required')"
            />
        </span>
    </div>
    <span class="p-d-flex p-jc-center p-ai-center" v-if="localTransformation.name === 'filter'">
        <Button icon="pi pi-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewRow()"/>
        <Button icon="pi pi-trash" :class="'p-button-text p-button-rounded p-button-plain ' + (localTransformation.parameters.length > 1 ? '' : 'kn-hide')" @click="deleteRow(fieldIndex)"
    /></span>
</template>

<script lang="ts">
    import { defineComponent, PropType } from 'vue'
    import DataPreparationCustomDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCustom/DataPreparationCustomDescriptor.json'
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
            return { descriptor: DataPreparationCustomDescriptor as any, parameters: [] as any, localTransformation: {} as ITransformation }
        },
        mounted() {
            this.localTransformation = this.transformation ? { ...this.transformation } : ({} as ITransformation)

            let name = this.transformation && this.transformation.name ? this.transformation.name : ''
            if (name && this.transformation?.type === 'custom') this.localTransformation.parameters = this.descriptor[name].parameters
        },
        methods: {
            addNewRow() {
                this.localTransformation?.parameters.forEach((x) => this.localTransformation?.parameters.push(x))
            },
            deleteRow(index) {
                if (this.localTransformation) {
                    if (this.localTransformation.parameters?.length > 1) this.localTransformation?.parameters.splice(index, 1)
                }
            },
            handleMultiSelectChange(e: Event): void {
                if (e) {
                    this.refreshTransfrormation()
                }
            },
            isFieldVisible(field): boolean {
                let visible = true
                if (field.relatedWith && field.relatedTo && this.localTransformation) {
                    for (let i in this.localTransformation.parameters) {
                        let obj = this.localTransformation.parameters[i]
                        if (obj.name === field.relatedTo) {
                            let keyValue
                            let keys = Object.keys(obj)
                            for (let i in keys) {
                                let key = keys[i]
                                if (key.includes('selectedCondition')) {
                                    keyValue = key
                                    break
                                }
                            }
                            if (keyValue) {
                                visible = visible && obj[keyValue] === field.relatedWith
                            } else {
                                visible = false
                                break
                            }
                        }
                    }
                }
                return visible
            },
            refreshTransfrormation(): void {
                if (this.localTransformation) {
                    let pars = this.localTransformation.type === 'custom' ? this.descriptor[this.localTransformation.name].parameters : []
                    pars.forEach((item) => {
                        item.availableOptions?.forEach((element) => {
                            element.label = this.$t(element.label)
                        })
                        if (item.type === 'multiSelect' && item.name === 'columns') {
                            if (this.col) {
                                let selectedItem: Array<IDataPreparationColumn> | undefined = this.columns?.filter((x) => x.header == this.col)
                                if (selectedItem && selectedItem.length > 0) {
                                    selectedItem[0].disabled = true

                                    item['selectedItems_index_0'] = selectedItem
                                }
                            } else {
                                this.columns?.forEach((e) => (e.disabled = false))
                            }
                        }
                    })
                }
            }
        },

        watch: {
            localTransformation: {
                handler(oldValue, newValue) {
                    if (oldValue !== newValue) {
                        this.$emit('update:transformation', newValue)
                    }
                },
                deep: true
            }
        }
    })
</script>

<style lang="scss" scoped></style>
