<template>
    <!-- <Button icon="fas fa-terminal" class="p-button-text p-button-rounded p-button-plain" @click="logModel" /> -->
    <div class="htmlMirrorContainer" style="height: 500px; width: 100%">
        <Button icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain editor-tags-menu-button" v-tooltip.left="$t('common.menu')" @click="toggle"></Button>
        <VCodeMirror ref="codeMirrorHtmlEditor" v-model:value="widgetModel.settings.editor.html" :options="scriptOptions" />
    </div>

    <TieredMenu ref="menu" :model="toolbarMenuItems" :popup="true" />
    <TagsDialog :visible="tagsDialogVisible" :mode="tagsDialogMode" widgetType="html" @close="closeTagsDialog" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import VCodeMirror from 'codemirror-editor-vue3'
import TieredMenu from 'primevue/tieredmenu'
import TagsDialog from '../../common/editor/WidgetTagsDialog.vue'

export default defineComponent({
    name: 'widget-responsive',
    components: { VCodeMirror, TieredMenu, TagsDialog },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            codeMirrorHtmlEditor: null as any,
            toolbarMenuItems: [] as any[],
            tagsDialogMode: '' as string,
            tagsDialogVisible: false,
            scriptOptions: {
                cursor: true,
                line: false,
                lineNumbers: true,
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                mode: 'html',
                tabSize: 4,
                theme: 'eclipse'
            }
        }
    },
    created() {
        this.setupCodeMirror()
    },
    methods: {
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorHtmlEditor) return
                this.codeMirrorHtmlEditor = (this.$refs.codeMirrorHtmlEditor as any).cminstance as any
                setTimeout(() => {
                    this.codeMirrorHtmlEditor.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        },
        logModel() {
            console.log(this.widgetModel)
        },
        toggle(event: Event) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.toolbarMenuItems.length = 0
            this.toolbarMenuItems.push(
                { label: this.$t('dashboard.widgetEditor.editorTags.columnsData'), command: () => this.openTagsDialog('columnsData') },
                { label: this.$t('dashboard.widgetEditor.editorTags.parameters'), command: () => this.openTagsDialog('parameters') },
                { label: this.$t('dashboard.widgetEditor.editorTags.variables'), command: () => this.openTagsDialog('variables') },
                { label: this.$t('dashboard.widgetEditor.editorTags.internationalization'), command: () => this.openTagsDialog('internationalization') },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.repeater'),
                    items: [
                        { label: this.$t('dashboard.widgetEditor.editorTags.repeater'), command: () => this.openTagsDialog('repeater') },
                        { label: this.$t('dashboard.widgetEditor.editorTags.repeatIndex'), command: () => this.openTagsDialog('repeatIndex') }
                    ]
                },
                { label: this.$t('dashboard.widgetEditor.editorTags.calculator'), command: () => this.openTagsDialog('calculator') },
                {
                    label: this.$t('dashboard.widgetEditor.interactions.title'),
                    items: [
                        { label: this.$t('dashboard.widgetEditor.editorTags.selection'), command: () => this.openTagsDialog('selection') },
                        { label: this.$t('dashboard.widgetEditor.editorTags.preview'), command: () => this.openTagsDialog('preview') },
                        { label: this.$t('dashboard.widgetEditor.editorTags.crossnav'), command: () => this.openTagsDialog('crossnav') }
                    ]
                },
                { label: this.$t('dashboard.widgetEditor.editorTags.conditional'), command: () => this.openTagsDialog('conditional') },
                { label: this.$t('dashboard.widgetEditor.editorTags.activesel'), command: () => this.openTagsDialog('activesel') }
            )
        },
        openTagsDialog(mode: string) {
            this.tagsDialogMode = mode
            this.tagsDialogVisible = true
        },
        closeTagsDialog() {
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
