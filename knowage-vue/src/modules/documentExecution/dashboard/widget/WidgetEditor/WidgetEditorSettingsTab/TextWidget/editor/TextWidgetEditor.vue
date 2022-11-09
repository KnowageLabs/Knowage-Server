<template>
    <div class="htmlMirrorContainer" style="height: 500px; width: 100%">
        <Button icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain editor-tags-menu-button" v-tooltip.left="$t('common.menu')" @click="toggle"></Button>
        <QuillEditor v-if="widgetModel?.settings?.editor" v-model:content="widgetModel.settings.editor.text" contentType="text" theme="snow"></QuillEditor>
    </div>

    <TieredMenu ref="menu" :model="toolbarMenuItems" :popup="true" />
    <TagsDialog :visible="tagsDialogVisible" :widgetModel="widgetModel" :mode="tagsDialogMode" widgetType="text" :drivers="drivers" :variables="variables" :selectedDatasets="selectedDatasets" @close="closeTagsDialog" @insert="onInsert" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVariable, IWidget, IDataset } from '@/modules/documentExecution/Dashboard/Dashboard'
import TieredMenu from 'primevue/tieredmenu'
import TagsDialog from '../../common/editor/WidgetTagsDialog.vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

export default defineComponent({
    name: 'text-widget-editor',
    components: { TieredMenu, TagsDialog, QuillEditor },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        activeIndex: { type: Number, required: true },
        drivers: { type: Array as PropType<any[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        dashboardId: { type: String, required: true }
    },
    data() {
        return {
            toolbarMenuItems: [] as any[],
            tagsDialogMode: '' as string,
            tagsDialogVisible: false,
            cursorPosition: null
        }
    },
    watch: {},
    created() {},
    methods: {
        toggle(event: Event) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.toolbarMenuItems.length = 0
            this.toolbarMenuItems.push(
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.columnsData'),
                    command: () => this.openTagsDialog('columnsData')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.parameters'),
                    command: () => this.openTagsDialog('parameters')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.variables'),
                    command: () => this.openTagsDialog('variables')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.internationalization'),
                    command: () => this.openTagsDialog('internationalization')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.repeater'),
                    items: [
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.repeater'),
                            command: () => this.openTagsDialog('repeater')
                        },
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.repeatIndex'),
                            command: () => this.openTagsDialog('repeatIndex')
                        }
                    ]
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.calculator'),
                    command: () => this.openTagsDialog('calculator')
                },
                {
                    label: this.$t('dashboard.widgetEditor.interactions.title'),
                    items: [
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.selection'),
                            command: () => this.openTagsDialog('selection')
                        },
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.preview'),
                            command: () => this.openTagsDialog('preview')
                        },
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.crossnav'),
                            command: () => this.openTagsDialog('crossnav')
                        }
                    ]
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.conditional'),
                    command: () => this.openTagsDialog('conditional')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.activesel'),
                    command: () => this.openTagsDialog('activesel')
                }
            )
        },
        openTagsDialog(mode: string) {
            this.tagsDialogMode = mode
            this.tagsDialogVisible = true
        },
        closeTagsDialog() {
            this.tagsDialogVisible = false
        },
        onInsert(value: string) {
            console.log('>>> ON INSERT: ', value)
            // this.cursorPosition = ''
            this.tagsDialogVisible = false
        }
    }
})
</script>

<style lang="scss">
.editor-tags-menu-button {
    position: absolute;
    font-size: 20px;
    top: 45px;
    right: 20px;
    z-index: 9999;
}
</style>
