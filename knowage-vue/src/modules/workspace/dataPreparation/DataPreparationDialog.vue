<template>
    <Dialog
        class="kn-dialog--toolbar--primary dataPreparationDialog"
        v-bind:visible="transformation"
        footer="footer"
        :header="(localCopy && localCopy.type ? $t(localCopy.type) + ' - ' : '') + $t('managers.workspaceManagement.dataPreparation.parametersConfiguration')"
        :closable="false"
        modal
        :breakpoints="{ '960px': '75vw', '640px': '100vw' }"
    >
        <Message severity="info" :closable="false">{{ $t(localCopy.description) }}</Message>
        <VeeForm class="p-d-flex elementClass">
            <span v-for="(fieldArray, fieldIndex) in localCopy.config.parameters" v-bind:key="fieldIndex" class="p-d-flex ">
                <span v-for="(field, index) in fieldArray" v-bind:key="index">
                    <span v-if="field.type == 'string'" class="p-float-label kn-flex">
                        <KnValidatedField v-model="field['input_fieldIndex_' + fieldIndex + '_index_' + index]" :name="field.name + '_' + fieldIndex" :label="$t(field.name)" :rules="field.validationRules" as="InputText" cssClass="kn-material-input" />
                    </span>
                    <Calendar v-if="field.type === 'calendar'" class="kn-flex" v-model="field['calendar_fieldIndex_' + fieldIndex + '_index_' + index]" />
                    <span v-if="field.type === 'boolean'" class="kn-flex">
                        <InputSwitch v-model="field[inputSwitch + '_fieldIndex_' + fieldIndex + '_index_' + index]" />
                        <label :for="field.value">{{ field.name }}</label>
                    </span>
                    <span v-if="field.type === 'dropdown'" class="kn-flex">
                        <Dropdown
                            v-if="field.name === 'columns'"
                            v-model="field['selectedCondition_fieldIndex_' + fieldIndex + '_index_' + index]"
                            :options="columns"
                            optionLabel="header"
                            optionValue="header"
                            :placeholder="$t('managers.workspaceManagement.dataPreparation.transformations.column')"
                            class="p-m-2"
                        />

                        <Dropdown v-if="field.availableValues" v-model="field['selectedCondition_fieldIndex_' + fieldIndex + '_index_' + index]" :options="field.availableValues" optionValue="code" :placeholder="$t(field.placeholder)" optionLabel="label" class="p-m-2" />
                    </span>

                    <MultiSelect
                        v-if="field.type == 'multiSelect'"
                        class="kn-flex p-m-2"
                        v-model="field['selectedItems_fieldIndex_' + fieldIndex + '_index_' + index]"
                        :options="columns"
                        optionLabel="header"
                        display="chip"
                        :placeholder="$t('managers.workspaceManagement.dataPreparation.transformations.columns')"
                        optionDisabled="disabled"
                        @change="handleMultiSelectChange($event)"
                        :allow-empty="false"
                        :disabled="col"
                    />

                    <Textarea v-if="field.type == 'textarea'" v-model="field['textarea_fieldIndex_' + fieldIndex + '_index_' + index]" rows="5" cols="30" />
                </span>
                <span class="p-d-flex p-jc-center p-ai-center" v-if="localCopy.type === 'advancedFilter'">
                    <Button icon="pi pi-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewRow()"/>
                    <Button icon="pi pi-trash" :class="'p-button-text p-button-rounded p-button-plain ' + (localCopy.config.parameters.length > 1 ? '' : 'kn-hide')" @click="deleteRow(fieldIndex)"
                /></span>
            </span>
        </VeeForm>

        <template #footer>
            <Button class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="resetAndClose" />

            <Button class="kn-button kn-button--primary" v-t="'common.apply'" @click="handleTransformation" />
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent, PropType } from 'vue'
    import { createValidations } from '@/helpers/commons/validationHelper'
    import useValidate from '@vuelidate/core'

    import Calendar from 'primevue/calendar'
    import Dialog from 'primevue/dialog'
    import Dropdown from 'primevue/dropdown'
    import InputSwitch from 'primevue/inputswitch'
    import Message from 'primevue/message'
    import MultiSelect from 'primevue/multiselect'
    import Textarea from 'primevue/textarea'
    import { ITransformation, IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'
    import DataPreparationValidationDescriptor from './DataPreparationValidationDescriptor.json'
    import KnValidatedField from '@/components/UI/KnValidatedField.vue'

    export default defineComponent({
        name: 'data-preparation-detail-dialog',
        props: {
            transformation: {} as PropType<ITransformation>,
            columns: { type: Array as PropType<Array<IDataPreparationColumn>> },
            col: String
        },
        components: { Calendar, Dialog, Dropdown, InputSwitch, Message, MultiSelect, KnValidatedField, Textarea },
        data() {
            return { localCopy: {} as ITransformation | undefined, v$: useValidate() as any, validationDescriptor: DataPreparationValidationDescriptor }
        },
        validations() {
            return {
                vTransformation: createValidations('localCopy', this.validationDescriptor.validations.configuration)
            }
        },
        emits: ['update:transformation', 'update:col', 'send-transformation'],

        beforeUpdate() {
            this.localCopy = JSON.parse(JSON.stringify(this.transformation))
        },
        updated() {
            this.refreshTransfrormation()
        },

        methods: {
            addNewRow(): void {
                this.localCopy?.config.parameters.push(this.localCopy?.config.parameters[0])
            },
            deleteRow(index): void {
                if (this.localCopy) {
                    if (this.localCopy.config.parameters?.length > 1) this.localCopy?.config.parameters.splice(index, 1)
                }
            },
            handleMultiSelectChange(e: Event): void {
                if (e) {
                    this.refreshTransfrormation()
                }
            },
            handleTransformation(): void {
                let convertedTransformation = this.convertTransformation()
                this.$emit('send-transformation', convertedTransformation)
            },
            resetAndClose(): void {
                this.closeDialog()
            },
            closeDialog(): void {
                this.$emit('update:col', false)
                this.$emit('update:transformation', false)
            },
            refreshTransfrormation(): void {
                if (this.localCopy) {
                    this.localCopy.config.parameters?.forEach((element) => {
                        element.forEach((item) => {
                            item.availableValues?.forEach((element) => {
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
            },
            convertTransformation() {
                let t = this.localCopy
                let toReturn = { parameters: [] as Array<any>, type: t?.type }

                t?.config.parameters?.forEach((element, fieldIndex) => {
                    let obj = { columns: [] as Array<any> }
                    element.forEach((item, index) => {
                        let postfix = '_fieldIndex_' + fieldIndex + '_index_' + index
                        switch (item.type) {
                            case 'multiSelect': {
                                this.handleItem(item, obj, 'selectedItems' + postfix)
                                break
                            }
                            case 'calendar': {
                                this.handleItem(item, obj, 'calendar' + postfix)
                                break
                            }
                            case 'string': {
                                this.handleItem(item, obj, 'input' + postfix)
                                break
                            }
                            case 'dropdown': {
                                this.handleItem(item, obj, 'selectedCondition' + postfix)
                                break
                            }
                            case 'textarea': {
                                this.handleItem(item, obj, 'textarea' + postfix)
                                break
                            }
                        }
                    })
                    toReturn.parameters.push(obj)
                })

                return toReturn
            },
            handleItem(item, obj, elId): void {
                const keys = Object.keys(item)
                keys.forEach((key) => {
                    if (key.includes(elId)) {
                        if (elId.includes('selectedItems')) {
                            item[key].forEach((e) => obj.columns.push(e.header))
                        } else if (elId.includes('selectedCondition') || elId.includes('textarea')) {
                            obj.operator = item[key]
                        } else if (elId.includes('input')) {
                            obj[item.name] = item[key]
                        }
                    }
                })
            }
        }
    })
</script>

<style lang="scss" scoped>
    .dataPreparationDialog {
        min-width: 600px !important;
        width: 50vw;
        max-width: 1200px !important;
        &:deep(.p-dialog-content) {
            height: 300px !important;

            width: 50vw;
        }

        .elementClass {
            flex-direction: column;
        }
    }
</style>
