<template>
    <div v-for="(field, index) in localTransformation.parameters" v-bind:key="index" :class="[field.type === 'textarea' ? 'p-col-6' : 'p-col-4', ' p-d-flex data-prep-simple-transformation p-field p-ml-2 kn-flex p-d-flex']">
        <span v-if="field.type == 'string'" class="p-float-label">
            <InputText :id="'input_index_' + index" type="text" v-model="localTransformation[field.name]" :class="['kn-material-input', field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] ? 'p-invalid' : '']" />
            <label :for="'input_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>
        <span v-if="field.type === 'calendar'" class="p-float-label">
            <Calendar :id="field.type + '_index_' + index" v-model="localTransformation[field.name]" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }" />
            <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>

        <span v-if="field.type === 'boolean'" class="p-float-label">
            <InputSwitch :id="'inputSwitch_index_' + index" v-model="localTransformation[field.name]" v-model.trim="localTransformation[field.name].$model" />
            <label :for="'inputSwitch_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>
        <span v-if="field.type === 'dropdown'" class="p-float-label">
            <Dropdown
                :id="'selectedCondition_index_' + index"
                v-model="localTransformation[field.name]"
                :options="field.availableOptions ? field.availableOptions : columns"
                :showClear="!field.validationRules || (field.validationRules && !field.validationRules.includes('required'))"
                :optionLabel="field.optionLabel ? field.optionLabel : 'label'"
                :optionValue="field.optionValue ? field.optionValue : 'code'"
                class="kn-material-input"
                :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }"
            />
            <label :for="'selectedCondition_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
        </span>

        <span v-if="field.type == 'multiSelect'" class="p-float-label">
            <MultiSelect
                :id="'selectedItems_index_' + index"
                v-model="localTransformation[field.name]"
                :options="columns"
                :optionLabel="field.optionLabel ? field.optionLabel : 'label'"
                display="chip"
                optionDisabled="disabled"
                @change="handleMultiSelectChange($event)"
                :allow-empty="false"
                :disabled="col"
                class="kn-material-input"
                :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }"
            /><label :for="'selectedItems_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.columns') }}</label></span
        >

        <span v-if="field.type == 'textarea'" class="p-float-label">
            <Textarea :id="field.type + '_index_' + index" v-model="localTransformation[field.name]" rows="5" cols="30" class="kn-material-input" :class="{ 'p-invalid': field.validationRules && field.validationRules.includes('required') && !localTransformation[field.name] }" :autoResize="false" />
            <label :for="field.type + '_index_' + index" class="kn-material-input-label">{{ $t('managers.workspaceManagement.dataPreparation.transformations.' + field.name) }}</label>
            <KnTextarea
                :id="field.type + '_index_' + index"
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

    import { createValidations } from '@/helpers/commons/validationHelper'
    import useValidate from '@vuelidate/core'

    export default defineComponent({
        name: 'data-preparation-simple',

        props: { col: String, columns: { type: Array as PropType<Array<IDataPreparationColumn>> }, transformation: {} as PropType<ITransformation> },
        components: { Calendar, Dropdown, InputSwitch, MultiSelect, Textarea, KnTextarea },
        emits: ['update:transformation'],
        data() {
            return { descriptor: DataPreparationSimpleDescriptor as any, parameters: [] as any, localTransformation: {} as ITransformation, v$: useValidate() as any, dirty: false }
        },
        validations() {
            if (this.transformation?.type === 'simple') {
                this.setupLocal()

                let validations = this.descriptor[this.localTransformation.name].validations ? this.descriptor[this.localTransformation.name].validations : []
                return { localTransformation: createValidations('localTransformation', validations) }
            }
        },

        beforeMount() {
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
                        item.availableOptions?.forEach((element) => {
                            element.label = this.$t(element.label)
                        })
                        if (item.type === 'multiSelect' && item.name === 'columns') {
                            if (this.col) {
                                let selectedItem: Array<IDataPreparationColumn> | undefined = this.columns?.filter((x) => x.header == this.col)
                                if (selectedItem && selectedItem.length > 0) {
                                    selectedItem[0].disabled = true
                                }
                            } else {
                                this.columns?.forEach((e) => (e.disabled = false))
                            }
                        }
                    })
                }
            },
            setupLocal() {
                if (Object.keys(this.localTransformation).length == 0) {
                    this.localTransformation = this.transformation ? { ...this.transformation } : ({} as ITransformation)

                    let name = this.transformation && this.transformation.name ? this.transformation.name : ''
                    if (name && this.transformation?.type === 'simple') this.localTransformation.parameters = this.descriptor[name].parameters
                }
            }
        },

        watch: {
            transformation() {
                this.setupLocal()
            }
        }
    })
</script>

<style lang="scss">
    .data-prep-simple-transformation {
        .p-field {
            .p-multiselect,
            .p-inputtext,
            .p-dropdown {
                width: 100%;
            }
        }
    }
</style>
