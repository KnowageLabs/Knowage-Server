<template>
    <div class="p-grid">
        <div class="p-col-12">{{ widgetModel?.settings.editor.text }}</div>
        <!-- <div id="editor-container" class="p-col-12"></div> -->
        <!-- <Button class="p-button-text p-button-rounded p-button-plain p-col-6" v-tooltip.left="$t('common.menu')" @click="toggle">TEEEEEEEEEEEEST</Button> -->
        <div class="p-col-12">
            <div class="htmlMirrorContainer" style="height: 600px; width: 100%">
                <Editor class="p-col-12" v-model="widgetModel.settings.editor.text" editorStyle="height: 320px">
                    <template v-slot:toolbar>
                        <span class="ql-formats">
                            <select class="ql-font">
                                <option selected value="arial">Arial</option>
                                <option value="aref-ruqua">Aref Ruqua</option>
                                <option value="mirza">Mirza</option>
                                <option value="roboto">Roboto</option>
                                <option value="inconsolata">Inconsolata</option>
                                <option value="sans-serif">Sans Serif</option>
                                <option value="serif">Serif</option>
                                <option value="monospace">Monospace</option>
                            </select>

                            <select class="ql-size">
                                <option value="small"></option>
                                <option selected></option>
                                <option value="large"></option>
                                <option value="huge"></option>
                            </select>
                        </span>

                        <span class="ql-formats">
                            <button class="ql-bold"></button>
                            <button class="ql-italic"></button>
                            <button class="ql-underline"></button>
                            <button class="ql-strike"></button>
                        </span>

                        <span class="ql-formats">
                            <button class="ql-color"></button>
                            <button class="ql-background"></button>
                        </span>

                        <span class="ql-formats">
                            <button class="ql-script" value="sub"></button>
                            <button class="ql-script" value="super"></button>
                        </span>

                        <span class="ql-formats">
                            <button class="ql-header" value="1"></button>
                            <button class="ql-header" value="2"></button>
                            <button class="ql-blockquote"></button>
                            <button class="ql-block"></button>
                        </span>

                        <span class="ql-formats">
                            <button class="ql-list" value="ordered"></button>
                            <button class="ql-list" value="bullet"></button>

                            <button class="ql-indent" value="-1"></button>
                            <button class="ql-indent" value="+1"></button>
                        </span>

                        <span class="ql-formats">
                            <button class="ql-direction"></button>
                            <button class="ql-align"></button>
                        </span>

                        <span class="ql-formats">
                            <button class="ql-clean"></button>
                        </span>

                        <span class="ql-formats">
                            <Button icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain" v-tooltip.left="$t('common.menu')" @click="toggle"></Button>
                        </span>
                    </template>
                </Editor>
            </div>
        </div>
    </div>

    <TieredMenu ref="menu" :model="toolbarMenuItems" :popup="true" />
    <TagsDialog :visible="tagsDialogVisible" :widgetModel="widgetModel" :mode="tagsDialogMode" widgetType="text" :drivers="drivers" :variables="variables" :selectedDatasets="selectedDatasets" @close="closeTagsDialog" @insert="onInsert" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVariable, IWidget, IDataset, IDashboardDriver } from '@/modules/documentExecution/Dashboard/Dashboard'
import TieredMenu from 'primevue/tieredmenu'
import TagsDialog from '../../common/editor/WidgetTagsDialog.vue'
import { Delta, Quill } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import Editor from 'primevue/editor'
import { CrossNavBlot } from './TextWidgetEditorQuillHelpers'

// const BlockEmbed = Quill.import('blots/block/embed')

// class keepHTML extends BlockEmbed {
//     static create(node) {
//         return node
//     }
//     static value(node) {
//         return node
//     }
// }

// ;(keepHTML as any).blotName = 'keepHTML'
// ;(keepHTML as any).className = 'keepHTML'
// ;(keepHTML as any).tagName = 'div'

// Quill.register(keepHTML)

Quill.register(CrossNavBlot, true)

var Parchment = Quill.import('parchment')
var dataId = new Parchment.Attributor.Attribute('test', 'test', {
    scope: Parchment.Scope.BLOCK
})
Quill.register(dataId)

var Font = Quill.import('formats/font')
Font.whitelist = ['mirza', 'roboto', 'arial', 'aref-ruqua', 'roboto', 'inconsolata', 'sans-serif', 'serif', 'monospace']
Quill.register(Font, true)

export default defineComponent({
    name: 'text-widget-editor',
    components: { TieredMenu, TagsDialog, Editor },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        activeIndex: { type: Number, required: true },
        drivers: { type: Array as PropType<IDashboardDriver[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        dashboardId: { type: String, required: true }
    },
    data() {
        return {
            toolbarMenuItems: [] as any[],
            tagsDialogMode: '' as string,
            tagsDialogVisible: false,
            cursorPosition: null,
            quill: {} as any
        }
    },
    watch: {},
    mounted() {
        // this.quill = new Quill('#editor-container', {
        //     modules: {
        //         toolbar: [[{ header: [1, 2, false] }], ['bold', 'italic', 'underline'], ['image', 'code-block']]
        //     },
        //     placeholder: 'Compose an epic...',
        //     theme: 'snow'
        // })
        // this.quill.on('text-change', this.onTextChange)
        // this.quill.clipboard.addMatcher('SPAN', function(node, delta) {
        //     console.log('>>>>>>>> NODE: ', node)
        //     console.log('>>>>>>>> NODE ATTRIBUTES: ', node.getAttribute('kn-cross'))
        //     console.log('>>>>>>>> NODE INNER HTML: ', node.innerHTML)
        //     console.log('>>>>>>>> NODE DATA: ', node.data)
        //     if (node.getAttribute('kn-cross') !== null) {
        //         return new Delta().retain(delta.length()).insert({
        //             tag: node.innerHTML
        //         })
        //     } else {
        //         return new Delta().insert(node.innerHTML)
        //     }
        // })

        // this.quill.clipboard.dangerouslyPasteHTML(0, this.widgetModel.settings.editor.text, 'user')
        // this.quill.clipboard.dangerouslyPasteHTML(0, '<p>Test</p>', 'user')
        //this.quill.setContents(this.quill.clipboard.convert('<div>' + this.widgetModel.settings.editor.text + '</div>'))
        console.log('>>>>>> QUILL IMPORTS:', Quill.imports)
    },
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
                }
            )
        },
        onTextChange(event: any) {
            console.log('>>>>>>> ON TEXT CHANGE: ', event)
            console.log('>>>>>>> ON TEXT CHANGE: ', this.quill.root.innerHTML)
            this.widgetModel.settings.editor.text = this.quill.root.innerHTML
        },
        openTagsDialog(mode: string) {
            this.tagsDialogMode = mode
            this.tagsDialogVisible = true
        },
        closeTagsDialog() {
            this.tagsDialogVisible = false
        },
        onInsert(value: string, mode?: string) {
            console.log('>>> ON INSERT: ', value)
            console.log('>>> ON INSERT MODE: ', mode)
            //  this.quill.insertEmbed(this.quill.getLength(), 'crossNav', 'test')
            //  this.quill.clipboard.dangerouslyPasteHTML(this.quill.getLength(), this.widgetModel.settings.editor.text)
            // this.quill.insertEmbed(0, 'span', 'test 2')
            this.widgetModel.settings.editor.text += '<p>' + value + '</p>'
            console.log('>>>>>>>>> QUIL: ', this.quill)
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

#editor-container {
    font-family: 'Arial';
    font-size: 18px;
    height: 375px;
}

#toolbar-container .ql-font span[data-label='Aref Ruqaa']::before {
    font-family: 'Aref Ruqaa';
}
#toolbar-container .ql-font span[data-label='Mirza']::before {
    font-family: 'Mirza';
}
#toolbar-container .ql-font span[data-label='Roboto']::before {
    font-family: 'Roboto';
}
#toolbar-container .ql-font span[data-label='Sans Serif']::before {
    font-family: 'Sans Serif';
}
#toolbar-container .ql-font span[data-label='Inconsolata']::before {
    font-family: 'Inconsolata';
}
#toolbar-container .ql-font span[data-label='Arial']::before {
    font-family: 'Arial';
}
#toolbar-container .ql-font span[data-label='Arial']::before {
    font-family: 'Serif';
}
#toolbar-container .ql-font span[data-label='Arial']::before {
    font-family: 'Monospace';
}

.ql-font-aref-ruqua {
    font-family: 'Aref Ruqaa';
}
.ql-font-mirza {
    font-family: 'Mirza';
}
.ql-font-roboto {
    font-family: 'Roboto';
}
.ql-font-sans-serif {
    font-family: 'Sans Serif';
}
.ql-font-inconsolata {
    font-family: 'Inconsolata';
}
.ql-font-arial {
    font-family: 'Arial';
}
.ql-font-serif {
    font-family: 'Arial';
}
.ql-font-monospace {
    font-family: 'Monospace';
}
</style>
