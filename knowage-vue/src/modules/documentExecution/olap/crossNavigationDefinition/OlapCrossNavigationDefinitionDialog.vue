<template>
    <Dialog id="olap-cross-naviagtion-definition-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapCrossNavigationDefinitionDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('documentExecution.olap.crossNavigationDefinition.title') }}
                </template>
                <template #right>
                    <Button v-if="step === 0" id="olap-add-new-cross-navigation-button" class="kn-button kn-button--primary" @click="addNewParameter"> {{ $t('common.addNew') }}</Button>
                </template>
            </Toolbar>
        </template>

        <OlapCrossNavigationStepOne v-if="step === 0" :propParameters="parameters" :addNewParameterVisible="addNewParameterVisible" :propSelectedParameter="selectedParameter" @parameterSelected="onParameterSelect" @deleteParameter="deleteParameter"></OlapCrossNavigationStepOne>
        <OlapCrossNavigationStepTwo v-else :propSelectedParameter="selectedParameter" :cell="cell" @selectFromTable="$emit('selectFromTable', selectedParameter?.type)"></OlapCrossNavigationStepTwo>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button v-if="step === 0" class="kn-button kn-button--primary" :disabled="!selectedParameter || !selectedParameter.type" @click="step = 1"> {{ $t('common.next') }}</Button>
            <Button v-else class="kn-button kn-button--primary" :disabled="!selectedParameter || !selectedParameter.name" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iOlapCrossNavigationParameter } from '../Olap'
import Dialog from 'primevue/dialog'
import olapCrossNavigationDefinitionDialogDescriptor from './OlapCrossNavigationDefinitionDialogDescriptor.json'
import OlapCrossNavigationStepOne from './OlapCrossNavigationStepOne.vue'
import OlapCrossNavigationStepTwo from './OlapCrossNavigationStepTwo.vue'

export default defineComponent({
    name: 'olap-cross-naviagtion-definition-dialog',
    components: { Dialog, OlapCrossNavigationStepOne, OlapCrossNavigationStepTwo },
    props: { sbiExecutionId: { type: String }, selectedCell: { type: Object } },
    emits: ['selectFromTable', 'close', 'save'],
    data() {
        return {
            olapCrossNavigationDefinitionDialogDescriptor,
            parameters: [
                {
                    name: 'Test',
                    dimension: 'Customers',
                    hierarchy: '[Customers.All Customers]',
                    level: '[Customers.All Customers].[Country]',
                    type: 'From Cell'
                },
                {
                    name: 'Test 2',
                    dimension: 'Customers',
                    hierarchy: '[Customers.All Customers]',
                    level: '[Customers.All Customers].[City]'
                }
            ] as iOlapCrossNavigationParameter[],
            selectedParameter: null as iOlapCrossNavigationParameter | null,
            cell: {} as any,
            step: 0,
            addNewParameterVisible: false
        }
    },
    watch: {
        selectedCell() {
            this.loadCell()
        }
    },
    created() {
        this.loadCell()
    },
    methods: {
        addNewParameter() {
            this.selectedParameter = {} as iOlapCrossNavigationParameter
            this.addNewParameterVisible = true
        },
        onParameterSelect(parameter: iOlapCrossNavigationParameter) {
            this.selectedParameter = parameter
            this.step = 1
            console.log('SELECTED PARAMETER: ', this.selectedParameter)
        },
        loadCell() {
            this.cell = this.selectedCell?.cell as any
            console.log(' >>> THIS SELECTED CELL: ', this.cell)
        },
        deleteParameter(parameter: iOlapCrossNavigationParameter) {
            console.log('PARAMETER FOR DELETE: ', parameter)
            const index = this.parameters.findIndex((el: iOlapCrossNavigationParameter) => el.name === parameter.name)
            if (index !== -1) this.parameters.splice(index, 1)
        },
        closeDialog() {
            this.$emit('close')
            this.step = 0
            this.selectedParameter = {} as iOlapCrossNavigationParameter
        },
        save() {
            console.log('SAVE CLICKED!')
            this.step = 0
        }
    }
})
</script>

<style lang="scss">
#olap-cross-naviagtion-definition-dialog .p-dialog-header,
#olap-cross-naviagtion-definition-dialog .p-dialog-content {
    padding: 0;
}

#olap-cross-naviagtion-definition-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}

#olap-add-new-cross-navigation-button {
    font-size: 0.75rem;
}
</style>
