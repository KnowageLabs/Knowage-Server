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
            <div class="p-m-2">
                <InputText class="p-inputtext p-component kn-material-input" v-model="searchWord" :placeholder="$t('common.search')" @input="filterIcons()" />
            </div>

            <label class="kn-material-input-label p-my-3"> {{ $t('dashboard.widgetEditor.fontawesome') }}</label>
            <div class="widget-editor-icon-picker-icons-container">
                <div v-for="(icon, index) in filteredIcons" :key="index" :class="{ 'widget-editor-selected-icon-container': selectedIcon?.className === icon.className }" class="widget-editor-icon-container kn-cursor-pointer" @click.stop="setSelectedIcon(icon)">
                    <i :class="icon.className"></i>
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
import { IIcon, IWidgetStyleToolbarModel } from '@/modules/documentExecution/Dashboard/Dashboard'
import Dialog from 'primevue/dialog'
import descriptor from './WidgetEditorStyleToolbarDescriptor.json'

export default defineComponent({
    name: 'widget-editor-icon-picker-dialog',
    components: { Dialog },
    props: { propModel: { type: Object as PropType<IWidgetStyleToolbarModel | null>, required: true } },
    emits: ['close', 'save'],
    data() {
        return {
            descriptor,
            icons: [] as IIcon[],
            filteredIcons: [] as IIcon[],
            selectedIcon: null as IIcon | null,
            searchWord: ''
        }
    },
    async created() {
        this.loadIcons()
        this.getSelectedIcon()
    },
    methods: {
        loadIcons() {
            this.icons = descriptor.iconsList
            this.filteredIcons = this.icons ? [...this.icons] : []
        },
        getSelectedIcon() {
            if (!this.propModel || !this.propModel.icon) return
            const index = this.icons.findIndex((icon: IIcon) => icon.className === this.propModel?.icon)
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
        },
        filterIcons() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredIcons = [...this.icons] as IIcon[]
                } else {
                    this.filteredIcons = this.filteredIcons.filter((icon: IIcon) => {
                        return icon.label?.toLowerCase().includes(this.searchWord.toLowerCase())
                    })
                }
            }, 250)
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
