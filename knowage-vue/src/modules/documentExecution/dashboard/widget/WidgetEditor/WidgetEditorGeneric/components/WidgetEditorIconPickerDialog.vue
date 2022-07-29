<template>
    <Dialog id="widget-editor-icon-picker-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.iconPickerDialogStyle" :visible="true" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('managers.menuManagement.chooseIcon') }}
                </template>
            </Toolbar>
        </template>

        <div class="widget-editor-icon-picker-content">
            <label class="kn-material-input-label p-my-3"> {{ $t('dashboard.widgetEditor.fontawesome') }}</label>
            <div class="widget-editor-icon-picker-icons-container">
                <div v-for="(icon, index) in icons" :key="index" :class="{ 'widget-editor-selected-icon-container': selectedIcon?.value === icon.value }" class="widget-editor-icon-container kn-cursor-pointer" @click.stop="setSelectedIcon(icon)">
                    <i :class="'fas fa-' + icon.name"></i>
                </div>
            </div>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IIcon, IWidget } from '../../../../Dashboard'
import Dialog from 'primevue/dialog'
import descriptor from './WidgetEditorStyleTooblarDescriptor.json'
import iconsList from '../../../../../../managers/menuManagement/IconPicker/icons'
import { getModelProperty } from '../WidgetEditorGenericHelper'

export default defineComponent({
    name: 'widget-editor-icon-picker-dialog',
    components: { Dialog },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, settings: { type: Object, required: true }, itemIndex: { type: Number } },
    emits: ['close', 'save'],
    data() {
        return {
            descriptor,
            icons: [] as IIcon[],
            selectedIcon: null as IIcon | null
        }
    },
    async created() {
        this.loadIcons()
        this.getSelectedIcon()
    },
    methods: {
        loadIcons() {
            this.icons = iconsList
        },
        getSelectedIcon() {
            if (!this.settings.initialValue) return

            let tempValue = null
            const tempFunction = getModelProperty(this.widgetModel, this.settings.initialValue, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') tempValue = tempFunction(this.widgetModel, this.settings.initialValue, this.itemIndex)

            if (tempValue) this.setSelectedIconFromInitialValue(tempValue)
        },
        setSelectedIconFromInitialValue(value: string) {
            const index = this.icons.findIndex((icon: IIcon) => icon.value === value)
            if (index !== -1) this.selectedIcon = { ...this.icons[index] }
        },
        setSelectedIcon(icon: IIcon) {
            this.selectedIcon = icon
        },
        closeDialog() {
            this.$emit('close')
            this.selectedIcon = null
        },
        save() {
            this.$emit('save', { ...this.selectedIcon })
            this.selectedIcon = null
        }
    }
})
</script>

<style lang="scss">
#widget-editor-icon-picker-dialog .p-dialog-header {
    padding: 0;
}
#widget-editor-icon-picker-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
    padding: 0.5rem;
}

.widget-editor-icon-picker-content {
    margin: 1rem;
}

.widget-editor-icon-picker-icons-container {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
    align-items: center;
}

.widget-editor-icon-container {
    border: 1px solid #c2c2c2;
    padding: 0.5rem;
    border-radius: 5px;
    margin: 0.5rem;
    height: 30px;
    width: 30px;
}

.widget-editor-selected-icon-container {
    background-color: #c2c2c2;
}
</style>
