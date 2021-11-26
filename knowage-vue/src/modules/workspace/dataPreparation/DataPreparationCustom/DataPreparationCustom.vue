<template>
    <div v-for="(field, index) in localTransformation.parameters" v-bind:key="index" class="p-field p-ml-2 kn-flex">
        <span v-if="field.type == 'string' && (!field.relatedWith || (field.relatedWith && isFieldVisible(field)))" class="p-float-label">
            <InputText :id="field.id" type="text" v-model="field.value" :class="['kn-material-input', field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] ? 'p-invalid' : '']" />
            <label :for="'input' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>
        <span v-if="field.type === 'calendar'" class="p-float-label">
            <Calendar :id="field.id" v-model="field.value" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }" />
            <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>

        <span v-if="field.type === 'boolean'" class="p-float-label">
            <InputSwitch :id="field.id" v-model="field.value" />
            <label :for="'inputSwitch' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>
        <span v-if="field.type === 'dropdown'" class="p-float-label">
            <Dropdown
                :id="field.id"
                v-model="field.value"
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
                :id="field.id"
                v-model="field.value"
                :options="columns"
                optionLabel="header"
                display="chip"
                optionDisabled="disabled"
                @change="handleMultiSelectChange($event)"
                :allow-empty="false"
                :disabled="col"
                class="kn-material-input"
                :filter="true"
                :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }"
            /><label :for="'selectedItems' + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.columns') }}</label></span
        >

        <span v-if="field.type == 'textarea'" class="p-float-label">
            <Textarea :id="field.id" v-model="field.value" rows="5" cols="30" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }" :autoResize="false" />
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
            return { descriptor: DataPreparationCustomDescriptor as any, localTransformation: {} as ITransformation, currentId: 0 }
        },
        mounted() {
            this.localTransformation = this.transformation ? { ...this.transformation } : ({} as ITransformation)

            this.descriptor = { ...DataPreparationCustomDescriptor } as any

            let name = this.transformation && this.transformation.name ? this.transformation.name : ''
            if (name && this.transformation?.type === 'custom') {
                let pars = this.descriptor[name].parameters

                this.localTransformation.parameters = JSON.parse(JSON.stringify(pars))
            }
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
