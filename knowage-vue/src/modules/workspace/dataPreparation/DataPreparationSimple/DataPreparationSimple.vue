<template>
    <span v-for="(fieldArray, fieldIndex) in localTransformation.parameters" v-bind:key="fieldIndex" class="p-d-flex">
        <div v-for="(field, index) in fieldArray" v-bind:key="index" :class="[field.type === 'textarea' ? 'p-col-6' : 'p-col-4', 'p-field p-ml-2 kn-flex']">
            <span v-if="field.type == 'string' && (!field.relatedWith || (field.relatedWith && isFieldVisible(field)))" class="p-float-label">
                <InputText
                    :id="name"
                    type="text"
                    v-model="field['input_fieldIndex_' + fieldIndex + '_index_' + index]"
                    :class="['kn-material-input', field.validationRules && field.validationRules.includes('required') && !field['input_fieldIndex_' + fieldIndex + '_index_' + index] ? 'p-invalid' : '']"
                />
                <label :for="'input_fieldIndex_' + fieldIndex + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>
            <span v-if="field.type === 'calendar'" class="p-float-label">
                <Calendar v-model="field[field.type + '_fieldIndex_' + fieldIndex + '_index_' + index]" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field[field.type + '_fieldIndex_' + fieldIndex + '_index_' + index] }" />
                <label :for="field.type + '_fieldIndex_' + fieldIndex + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>

            <span v-if="field.type === 'boolean'" class="p-float-label">
                <InputSwitch v-model="field['inputSwitch_fieldIndex_' + fieldIndex + '_index_' + index]" />
                <label :for="'inputSwitch_fieldIndex_' + fieldIndex + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            </span>
            <span v-if="field.type === 'dropdown'">
                <span v-if="field.name === 'columns'" class="p-float-label">
                    <Dropdown
                        v-model="field['selectedCondition_fieldIndex_' + fieldIndex + '_index_' + index]"
                        :options="columns"
                        optionLabel="header"
                        optionValue="header"
                        class="kn-material-input"
                        :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field['selectedCondition_fieldIndex_' + fieldIndex + '_index_' + index] }"
                    />
                    <label :for="'selectedCondition_fieldIndex_' + fieldIndex + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.column') }}</label> </span
                ><span v-if="field.availableOptions" class="p-float-label">
                    <Dropdown
                        v-model="field['selectedCondition_fieldIndex_' + fieldIndex + '_index_' + index]"
                        :options="field.availableOptions"
                        optionValue="code"
                        optionLabel="label"
                        class="kn-material-input"
                        :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field['selectedCondition_fieldIndex_' + fieldIndex + '_index_' + index] }"
                    />
                    <label :for="'selectedCondition_fieldIndex_' + fieldIndex + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
                </span>
            </span>

            <span v-if="field.type == 'multiSelect'" class="p-float-label">
                <MultiSelect
                    v-model="field['selectedItems_fieldIndex_' + fieldIndex + '_index_' + index]"
                    :options="columns"
                    optionLabel="header"
                    display="chip"
                    optionDisabled="disabled"
                    @change="handleMultiSelectChange($event)"
                    :allow-empty="false"
                    :disabled="col"
                    class="kn-material-input"
                    :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field['selectedItems_fieldIndex_' + fieldIndex + '_index_' + index] }"
                /><label :for="'selectedItems_fieldIndex_' + fieldIndex + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.columns') }}</label></span
            >

            <span v-if="field.type == 'textarea'" class="p-float-label">
                <Textarea
                    v-model="field[field.type + '_fieldIndex_' + fieldIndex + '_index_' + index]"
                    rows="5"
                    cols="30"
                    class="kn-material-input"
                    :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !field[field.type + '_fieldIndex_' + fieldIndex + '_index_' + index] }"
                    :autoResize="false"
                />
                <label :for="field.type + '_fieldIndex_' + fieldIndex + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
                <KnTextarea
                    v-model="field[field.type + '_fieldIndex_' + fieldIndex + '_index_' + index]"
                    rows="5"
                    cols="30"
                    :name="field.type + '_fieldIndex_' + fieldIndex + '_index_' + index"
                    :label="$t('managers.workspaceManagement.dataPreparation.transformations.' + field.name)"
                    :autoResize="false"
                    :required="field.validationRules && field.validationRules.includes('required')"
                />
            </span>
        </div>
    </span>
</template>

<script lang="ts">
    import { defineComponent, PropType } from 'vue'
    import DataPreparationSimpleDescriptor from '@/modules/workspace/dataPreparation/DataPreparationSimple/DataPreparationSimpleDescriptor.json'
    import Calendar from 'primevue/calendar'
    import Dropdown from 'primevue/dropdown'
    import InputSwitch from 'primevue/inputswitch'
    import MultiSelect from 'primevue/multiselect'
    import KnTextarea from '@/components/UI/KnTextarea.vue'
    import Textarea from 'primevue/textarea'

    import { ITransformation } from '@/modules/workspace/dataPreparation/DataPreparation'
    import { IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'

    export default defineComponent({
        name: 'data-preparation-simple',

        props: { col: String, columns: { type: Array as PropType<Array<IDataPreparationColumn>> }, transformation: {} as PropType<ITransformation> },
        components: { Calendar, Dropdown, InputSwitch, MultiSelect, Textarea, KnTextarea },
        emits: ['update:transformation'],
        data() {
            return { descriptor: DataPreparationSimpleDescriptor as any, parameters: [] as any, localTransformation: {} as ITransformation }
        },
        mounted() {
            this.localTransformation = this.transformation ? { ...this.transformation } : ({} as ITransformation)

            let name = this.transformation && this.transformation.name ? this.transformation.name : ''
            if (name && this.transformation?.type === 'simple') this.localTransformation.parameters = this.descriptor[name].parameters
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
                    pars.forEach((element) => {
                        element.forEach((item) => {
                            item.availableOptions?.forEach((element) => {
                                element.label = this.$t(element.label)
                            })
                            if (item.type === 'multiSelect' && item.name === 'columns') {
                                if (this.col) {
                                    let selectedItem: Array<IDataPreparationColumn> | undefined = this.columns?.filter((x) => x.header == this.col)
                                    if (selectedItem && selectedItem.length > 0) {
                                        selectedItem[0].disabled = true

                                        item['selectedItems_fieldIndex_0_index_0'] = selectedItem
                                    }
                                } else {
                                    this.columns?.forEach((e) => (e.disabled = false))
                                }
                            }
                        })
                    })
                }
            }
        },
        updated() {
            this.$emit('update:transformation', this.localTransformation)
        },
        watch: {
            localTransformation(oldValue, newValue) {
                if (oldValue !== newValue) {
                    this.$emit('update:transformation', newValue)
                }
            }
        }
    })
</script>

<style lang="scss" scoped></style>
