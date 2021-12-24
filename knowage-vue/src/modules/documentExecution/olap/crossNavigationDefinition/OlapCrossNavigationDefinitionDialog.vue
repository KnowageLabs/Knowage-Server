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

        {{ selectedParameter }}

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
    props: { visible: { type: Boolean }, sbiExecutionId: { type: String }, selectedCell: { type: Object }, propOlapDesigner: { type: Object } },
    emits: ['selectFromTable', 'close', 'save'],
    data() {
        return {
            olapCrossNavigationDefinitionDialogDescriptor,
            parameters: [] as iOlapCrossNavigationParameter[],
            selectedParameter: null as iOlapCrossNavigationParameter | null,
            cell: null as any,
            olapDesigner: null as any,
            step: 0,
            addNewParameterVisible: false
        }
    },
    watch: {
        propOlapDesigner() {
            this.loadDesigner()
        },
        selectedCell() {
            this.loadCell()
        },
        visible(value: boolean) {
            if (value) {
                this.loadDesigner()
            }
        }
    },
    created() {
        this.loadDesigner()
        this.loadCell()
    },
    methods: {
        loadDesigner() {
            this.olapDesigner = this.propOlapDesigner as any
            if (this.olapDesigner) {
                this.loadCrossNavigationParameters()
            }
        },
        loadCrossNavigationParameters() {
            const fromCellParameters = this.olapDesigner?.template?.wrappedObject?.olap?.CROSS_NAVIGATION?.PARAMETERS.PARAMETER
            const fromMemberParameters = [] as iOlapCrossNavigationParameter[]
            this.olapDesigner.template.wrappedObject.olap.MDXQUERY.clickable?.forEach((el: any) => fromMemberParameters.push({ ...el, name: el.clickParameter.name, type: 'From Member' }))

            this.parameters = []
            this.parameters = this.parameters.concat(fromCellParameters)
            this.parameters = this.parameters.concat(fromMemberParameters)
        },
        addNewParameter() {
            this.selectedParameter = {} as iOlapCrossNavigationParameter
            this.addNewParameterVisible = true
        },
        onParameterSelect(parameter: iOlapCrossNavigationParameter) {
            this.selectedParameter = parameter
            this.step = 1
        },
        loadCell() {
            this.cell = this.selectedCell?.cell as any
        },
        deleteParameter(parameter: iOlapCrossNavigationParameter) {
            parameter.type === 'From Cell' ? this.removeCellParameter(parameter) : this.removeMemberParameter(parameter)
            this.loadCrossNavigationParameters()
        },
        removeCellParameter(parameter) {
            const olapDesignerParameters = this.olapDesigner.template.wrappedObject.olap.CROSS_NAVIGATION.PARAMETERS.PARAMETER
            const index = olapDesignerParameters.findIndex((el: any) => el.name === parameter?.name)
            if (index !== -1) olapDesignerParameters.splice(index, 1)
        },
        removeMemberParameter(parameter) {
            const clickable = this.olapDesigner.template.wrappedObject.olap.MDXQUERY.clickable
            const index = clickable.findIndex((el: any) => el.clickParameter.name === parameter.name)
            if (index !== -1) clickable.splice(index, 1)
        },
        closeDialog() {
            this.$emit('close')
            this.step = 0
            this.selectedParameter = {} as iOlapCrossNavigationParameter
            this.cell = null
        },
        save() {
            if (this.selectedParameter) {
                this.selectedParameter.type === 'From Cell' ? this.addFromCellParameter() : this.addFromMemberParameter()
            }

            this.closeDialog()
        },
        addFromCellParameter() {
            const value = this.selectedParameter?.value

            const dimension = value?.substring(value.indexOf('dimension'), value.indexOf('hierarchy'))
            const hierarchy = value?.substring(value.indexOf('hierarchy'), value.indexOf('level'))
            const level = value?.substring(value.indexOf('level') + 1)

            const tempParameter = {
                dimension: dimension?.substring(dimension.indexOf('=') + 1).trim(),
                hierarchy: hierarchy?.substring(hierarchy.indexOf('=') + 1).trim(),
                level: level?.substring(level.indexOf('=') + 1).trim(),
                name: this.selectedParameter?.name,
                type: this.selectedParameter?.type
            }

            const olapDesignerParameters = this.olapDesigner.template.wrappedObject.olap.CROSS_NAVIGATION.PARAMETERS.PARAMETER

            const index = olapDesignerParameters.findIndex((el: any) => el.name === this.selectedParameter?.name)
            index !== -1 ? (olapDesignerParameters[index] = tempParameter) : olapDesignerParameters.push(tempParameter)
        },
        addFromMemberParameter() {
            if (!this.selectedParameter) {
                return
            }

            const clickable = this.olapDesigner.template.wrappedObject.olap.MDXQUERY.clickable
            const tempParameter = { clickParameter: { name: this.selectedParameter.name, value: '{0}' }, name: this.selectedParameter.name, type: this.selectedParameter.type, uniqueName: this.selectedParameter.value }

            const index = clickable.findIndex((el: any) => el.clickParameter.name === this.selectedParameter?.name)
            index !== -1 ? (clickable[index] = tempParameter) : clickable.push(tempParameter)
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
