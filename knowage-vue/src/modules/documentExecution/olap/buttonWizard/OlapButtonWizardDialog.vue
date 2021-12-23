<template>
    <Dialog id="olap-button-wizard-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapButtonWizardDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('documentExecution.olap.buttonsWizard.title') }}
                </template>
            </Toolbar>
        </template>

        <DataTable :value="wizardButtons" class="p-datatable-sm kn-table p-m-4" :scrollable="true" scrollHeight="100%">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <Column :header="$t('common.name')">
                <template #body="slotProps">
                    {{ $t(olapButtonWizardDialogDescriptor.buttonLabels[slotProps.data.name]) }}
                    <!-- olapButtonWizardDialogDescriptor -->
                </template>
            </Column>
            <Column :header="$t('common.visible')">
                <template #header>
                    <Checkbox class="p-mr-2" v-model="allVisibleSelected" :binary="true" @change="setAllChecked('visible')" />
                </template>
                <template #body="slotProps">
                    <Checkbox v-model="slotProps.data.visible" :binary="true" @change="setChecked(slotProps.data, 'visible')" />
                </template>
            </Column>
            <Column :header="$t('common.clicked')">
                <template #header>
                    <Checkbox class="p-mr-2" v-model="allClickedSelected" :binary="true" @change="setAllChecked('clicked')" />
                </template>
                <template #body="slotProps">
                    <Checkbox v-model="slotProps.data.clicked" :binary="true" :disabled="!slotProps.data.clickable" @change="setChecked(slotProps.data, 'clicked')" />
                </template>
            </Column>
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iButton } from '../Olap'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import olapButtonWizardDialogDescriptor from './OlapButtonWizardDialogDescriptor.json'

export default defineComponent({
    name: 'olap-button-wizard-dialog',
    components: { Checkbox, Column, DataTable, Dialog },
    props: { visible: { type: Boolean }, sbiExecutionId: { type: String }, propButtons: { type: Array as PropType<iButton[]> }, propOlapDesigner: { type: Object } },
    data() {
        return {
            olapButtonWizardDialogDescriptor,
            buttons: [] as iButton[],
            wizardButtons: [] as any[],
            selected: {} as any,
            allVisibleSelected: false,
            allClickedSelected: false,
            olapDesigner: null as any
        }
    },
    watch: {
        propButtons() {
            this.loadButtons()
        },
        propOlapDesigner() {
            this.loadDesigner()
        },
        visible(value: boolean) {
            if (value) {
                this.loadButtons()
                this.loadDesigner()
            }
        }
    },
    created() {
        this.loadButtons()
        this.loadDesigner()
    },
    methods: {
        loadButtons() {
            this.buttons = this.propButtons as iButton[]
            console.log('BUTTONS LOADED IN DIALOG: ', this.buttons)
        },
        loadDesigner() {
            this.olapDesigner = this.propOlapDesigner as any
            //  console.log('TEMPLATE LOADED IN BUTTONS: ', this.template)
            if (this.olapDesigner) {
                this.loadWizardButtons()
            }
        },
        loadWizardButtons() {
            this.wizardButtons = this.buttons.filter((el: iButton) => el.category !== 'OLAP_DESIGNER')

            // TODO HARDCODED UNTIL NEW WAR
            // if (this.olapDocument?.engine === 'knowageolapengine') {
            this.wizardButtons = this.wizardButtons.filter((el: iButton) => el.category !== 'WHAT_IF')
            // }

            const toolbarButtonKeys = Object.keys(this.olapDesigner.template.wrappedObject.olap.TOOLBAR)

            // console.log('TOOLBAR BUTTON KEYS: ', toolbarButtonKeys)

            this.wizardButtons.forEach((tempButton: iButton) => {
                const index = toolbarButtonKeys.indexOf(tempButton.name)
                if (index >= 0) {
                    // console.log('TEMP BUTTON: ', tempButton)
                    // console.log('TTOOL BUTTON: ', this.template.olap.TOOLBAR[toolbarButtonKeys[index]])
                    tempButton.visible = this.olapDesigner.template.wrappedObject.olap.TOOLBAR[toolbarButtonKeys[index]].visible
                    tempButton.clicked = this.olapDesigner.template.wrappedObject.olap.TOOLBAR[toolbarButtonKeys[index]].clicked

                    // console.log(' >>> >>> TEMP BUTTON VISIBLE', tempButton.clicked)
                }
                // console.log('INDEX: ', index)
            })

            this.checkIfAllSelected()
            console.log('TEMP BUTTONS: ', this.wizardButtons)
        },
        checkIfColumnSelected(property: string) {
            let allChecked = true
            for (let i = 0; i < this.wizardButtons.length; i++) {
                if ((property === 'visible' || (property === 'clicked' && this.wizardButtons[i].clickable)) && !this.wizardButtons[i][property]) {
                    allChecked = false
                    break
                }
            }
            return allChecked
        },
        checkIfAllSelected() {
            this.allVisibleSelected = this.checkIfColumnSelected('visible')
            this.allClickedSelected = this.checkIfColumnSelected('clicked')
        },
        setAllChecked(type: string) {
            // console.log('SET ALL CHECKED: ', type)
            // console.log('SET ALL CLICKED: ', this.allClickedSelected)
            // console.log('SET ALL VISILBE: ', this.allVisibleSelected)

            this.wizardButtons.forEach((button: iButton) => {
                if (type === 'clicked' && button.clickable) {
                    button.clicked = this.allClickedSelected
                    console.log('ENTERED 1', button)
                } else if (type === 'visible') {
                    console.log('ENTERED 2', button)
                    button.visible = this.allVisibleSelected
                }
                this.changeCheckedValue(button, type)
            })

            this.checkIfAllSelected()
            console.log('>>> TEEEEEST: ', this.wizardButtons)
        },
        setChecked(button: any, type: string) {
            this.changeCheckedValue(button, type)
            this.checkIfAllSelected()
        },
        changeCheckedValue(button: any, type: string) {
            // console.log('SET CHECKED: ', button, type)
            if (type === 'clicked' && button.clicked) {
                button.visible = true
            } else if (type === 'visible' && !button.visible) {
                button.clicked = false
            }
        },
        closeDialog() {
            this.$emit('close')
            this.wizardButtons = []
        },
        save() {
            console.log('>>> Save wizzard ', this.wizardButtons)
            const tempOlapDesignerObject = this.olapDesigner.template.wrappedObject.olap
            console.log('>>> Save template olap ', tempOlapDesignerObject)

            const toolbarButtonKeys = Object.keys(this.olapDesigner.template.wrappedObject.olap.TOOLBAR)

            // console.log('TOOLBAR BUTTON KEYS: ', toolbarButtonKeys)

            this.wizardButtons.forEach((tempButton: iButton) => {
                const index = toolbarButtonKeys.indexOf(tempButton.name)
                if (index >= 0) {
                    this.olapDesigner.template.wrappedObject.olap.TOOLBAR[toolbarButtonKeys[index]].visible = tempButton.visible
                    this.olapDesigner.template.wrappedObject.olap.TOOLBAR[toolbarButtonKeys[index]].clicked = tempButton.clicked
                }
            })
            this.$emit('close')

            console.log('>>> Save template olap AFTER ', tempOlapDesignerObject)
        }
    }
})
</script>

<style lang="scss">
#olap-button-wizard-dialog .p-dialog-header,
#olap-button-wizard-dialog .p-dialog-content {
    padding: 0;
}
#olap-button-wizard-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
