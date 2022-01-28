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
        <DataPreparationFilter v-if="localCopy.type === 'filter'" :transformation="localCopy" @update:transformation="updateLocalCopy" :columns="columns" :col="col" />
        <DataPreparationSplitColumn v-if="localCopy.type === 'split'" :transformation="localCopy" @update:transformation="updateLocalCopy" :columns="columns" :col="col" />

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
import { ITransformation, IDataPreparationColumn, ITransformationParameter } from '@/modules/workspace/dataPreparation/DataPreparation'
import DataPreparationValidationDescriptor from './DataPreparationValidationDescriptor.json'
import DataPreparationSimple from './DataPreparationSimple/DataPreparationSimple.vue'
import DataPreparationSimpleDescriptor from '@/modules/workspace/dataPreparation/DataPreparationSimple/DataPreparationSimpleDescriptor.json'
import DataPreparationCustom from './DataPreparationCustom/DataPreparationCustom.vue'
import DataPreparationCustomDescriptor from '@/modules/workspace/dataPreparation/DataPreparationCustom/DataPreparationCustomDescriptor.json'
import DataPreparationFilter from './DataPreparationCustom/DataPreparationFilterTransformation.vue'
import DataPreparationSplitColumn from './DataPreparationCustom/DataPreparationSplitColumnTransformation.vue'

export default defineComponent({
    name: 'data-preparation-detail-dialog',
    props: {
        transformation: {} as PropType<ITransformation<ITransformationParameter>>,
        columns: { type: Array as PropType<Array<IDataPreparationColumn>> },
        col: String
    },
    components: { DataPreparationSimple, Dialog, Message, DataPreparationCustom, DataPreparationFilter, DataPreparationSplitColumn },
    data() {
        return { localCopy: {} as ITransformation<ITransformationParameter> | undefined, v$: useValidate() as any, validationDescriptor: DataPreparationValidationDescriptor, simpleDescriptor: DataPreparationSimpleDescriptor, customDescriptor: DataPreparationCustomDescriptor }
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

    methods: {
        addNewRow(): void {
            this.localCopy?.parameters.push(this.localCopy?.parameters[0])
        },
        convertTransformation() {
            let t = this.localCopy
            let transformation = { parameters: [] as Array<any>, type: t?.name }

            if (t?.name === 'filter') return this.convertFilterTransformation(t, transformation)
            if (t?.name === 'splitColumn') return this.convertSplitTransformation(t, transformation)

            let par = { columns: [] as Array<any> }
            t?.parameters?.forEach((p) => {
                Object.keys(p).forEach((key) => {
                    if (p.value && !this.isToBeSkipped(key)) {
                        if (key == 'name' && p[key] == 'columns') par.columns = this.getColumns(p)
                        else par[p[key]] = p.value
                    }
                })
            })
            transformation.parameters.push(par)

            return transformation
        },

        convertSplitTransformation(t, transformation) {
            let p = t?.parameters
            this.convertCustomTransformation(p, transformation)
            return transformation
        },

        convertCustomTransformation(p, transformation) {
            let par = { columns: [] as Array<any> }
            Object.keys(p).forEach((key) => {
                if (key === 'column') par.columns.push(p[key].header)
                else par[key] = p[key]
            })
            transformation.parameters.push(par)
        },

        convertFilterTransformation(t, transformation) {
            t?.parameters?.forEach((p) => {
                this.convertCustomTransformation(p, transformation)
            })
            return transformation
        },

        isToBeSkipped(key: string): Boolean {
            return key === 'value' || key === 'type' || key.includes('option') || key.includes('available') || key.includes('depends') || key.includes('validation') || key.includes('placeholder')
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

        getColumns(parameter): Array<any> {
            let toReturn = [] as Array<any>
            if (Array.isArray(parameter.value)) {
                parameter.value.forEach((v) => {
                    toReturn.push(v.header)
                })
            } else {
                toReturn.push(parameter.value)
            }
            return toReturn
        },

        handleTransformation(): void {
            let convertedTransformation = this.convertTransformation()
            this.$emit('send-transformation', convertedTransformation)
            this.closeDialog()
        },

        resetAndClose(): void {
            this.closeDialog()
        },

        updateLocalCopy(t): void {
            if (this.localCopy?.name === 'filter' || this.localCopy?.name === 'splitColumn') this.localCopy.parameters = t
            else this.localCopy = t
        }
    },
    watch: {
        transformation: {
            handler(newValue, oldValue) {
                if (oldValue !== newValue) {
                    this.localCopy = JSON.parse(JSON.stringify(newValue))
                }
            },
            deep: true
        }
    }
})
</script>

<style lang="scss">
.dataPreparationDialog {
    .p-dialog-content {
        min-width: 600px;
        max-width: 1200px;
        min-height: 150px;
    }
    .elementClass {
        flex-direction: column;
    }
}
</style>
