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

        <OlapCrossNavigationStepOne v-if="step === 0" :propParameters="parameters" :addNewParameterVisible="addNewParameterVisible" :propSelectedParameter="selectedParameter"></OlapCrossNavigationStepOne>
        <OlapCrossNavigationStepTwo v-else></OlapCrossNavigationStepTwo>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button v-if="step === 0" class="kn-button kn-button--primary" :disabled="!selectedParameter || !selectedParameter.type" @click="step = 1"> {{ $t('common.next') }}</Button>
            <Button v-else class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
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
    props: { sbiExecutionId: { type: String } },
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
            step: 0,
            addNewParameterVisible: false
        }
    },
    created() {},
    methods: {
        addNewParameter() {
            this.selectedParameter = {} as iOlapCrossNavigationParameter
            this.addNewParameterVisible = true
        },
        closeDialog() {
            this.$emit('close')
        },
        save() {
            console.log('SAVE CLICKED!')
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
