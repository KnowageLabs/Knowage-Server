<template>
    <Dialog
        class="kn-dialog--toolbar--primary dataPreparationDialog"
        v-bind:visible="transformation"
        footer="footer"
        :header="(localCopy && localCopy.type ? $t('managers.workspaceManagement.dataPreparation.transformations.' + localCopy.name + '.label') + ' - ' : '') + $t('managers.workspaceManagement.dataPreparation.parametersConfiguration')"
        :closable="false"
        modal
        :breakpoints="{ '960px': '75vw', '640px': '100vw' }"
    >
        <Message severity="info" :closable="false" v-if="localCopy && localCopy.description">{{ $t(localCopy.description) }}</Message>

        <DataPreparationSimple v-if="localCopy.type === 'simple'" :transformation="localCopy" @update:transformation="updateLocalCopy" :columns="columns" :col="col" />
        <DataPreparationCustom v-if="localCopy.type === 'custom'" :transformation="localCopy" @update:transformation="updateLocalCopy" :columns="columns" :col="col" />

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
    import Dialog from 'primevue/dialog'
    import Message from 'primevue/message'
    import { ITransformation, IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'
    import DataPreparationValidationDescriptor from './DataPreparationValidationDescriptor.json'
    import DataPreparationSimple from './DataPreparationSimple/DataPreparationSimple.vue'
    import DataPreparationSimpleDescriptor from '@/modules/workspace/dataPreparation/DataPreparationSimple/DataPreparationSimpleDescriptor.json'
    import DataPreparationCustom from './DataPreparationCustom/DataPreparationCustom.vue'
    import DataPreparationCustomDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCustom/DataPreparationCustomDescriptor.json'

    export default defineComponent({
        name: 'data-preparation-detail-dialog',
        props: {
            transformation: {} as PropType<ITransformation>,
            columns: { type: Array as PropType<Array<IDataPreparationColumn>> },
            col: String
        },
        components: { DataPreparationSimple, Dialog, Message, DataPreparationCustom },
        data() {
            return { localCopy: {} as ITransformation | undefined, v$: useValidate() as any, validationDescriptor: DataPreparationValidationDescriptor, simpleDescriptor: DataPreparationSimpleDescriptor, customDescriptor: DataPreparationCustomDescriptor }
        },
        validations() {
            return {
                vTransformation: createValidations('localCopy', this.validationDescriptor.validations.configuration)
            }
        },
        emits: ['update:transformation', 'update:col', 'send-transformation'],

        created() {
            this.simpleDescriptor = { ...DataPreparationSimpleDescriptor } as any

            this.customDescriptor = { ...DataPreparationCustomDescriptor } as any
        },
        beforeUpdate() {
            if (!this.localCopy?.parameters) {
                this.localCopy = JSON.parse(JSON.stringify(this.transformation))
            }
        },

        methods: {
            addNewRow(): void {
                this.localCopy?.parameters.push(this.localCopy?.parameters[0])
            },
            convertTransformation() {
                let t = this.localCopy
                let toReturn = { parameters: [] as Array<any>, type: t?.name }

                t?.parameters?.forEach((item, index) => {
                    let obj = { columns: [] as Array<any> }

                    let postfix = '_index_' + index
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

                    toReturn.parameters.push(obj)
                })

                return toReturn
            },

            closeDialog(): void {
                this.$emit('update:col', false)
                this.$emit('update:transformation', false)
            },

            deleteRow(index): void {
                if (this.localCopy) {
                    if (this.localCopy.parameters?.length > 1) this.localCopy?.parameters.splice(index, 1)
                }
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
            },

            handleTransformation(): void {
                let convertedTransformation = this.convertTransformation()
                this.$emit('send-transformation', convertedTransformation)
            },

            resetAndClose(): void {
                this.closeDialog()
            },

            updateLocalCopy(t: ITransformation): void {
                this.localCopy = t
            }
        }
    })
</script>

<style lang="scss" scoped>
    .dataPreparationDialog {
        min-width: 600px;
        width: 60%;
        max-width: 1200px;
        min-height: 150px;
        &:deep(.p-dialog-content) {
            @extend .dataPreparationDialog;
        }
        .elementClass {
            flex-direction: column;
        }
    }
</style>
