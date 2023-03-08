<template>
    <div class="toolbar-context-menu">
        <div v-for="(option, index) in options" :key="index" class="toolbar-context-menu-option">
            <div class="toolbar-context-item kn-cursor-pointer" @click="setSelectedValue(option.value)">
                <div class="toolbar-item-text">
                    {{ $t(option.label) }}
                </div>
                <i v-if="option.value === 'input'" class="pi pi-angle-right p-ml-auto p-mr-5"></i>
            </div>
            <InputText v-if="inputVisible" v-model="inputValue" class="toolbar-context-menu-input kn-material-input p-inputtext-sm" @input="onInputChanged" @change="onInputChanged" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import descriptor from './WidgetEditorStyleToolbarDescriptor.json'

export default defineComponent({
    name: 'widget-editor-toolbar-context-menu',
    components: {},
    props: { option: { type: Object as PropType<any>, required: true } },
    emits: ['selected', 'inputChanged'],
    data() {
        return {
            descriptor,
            modelValue: '' as any,
            inputValue: '' as any,
            inputVisible: false
        }
    },
    computed: {
        options() {
            switch (this.option.type) {
                case 'font-size':
                    return descriptor.fontSizeOptions
                case 'justify-content':
                    return descriptor.cellAlignmentOptions
                case 'text-align':
                    return descriptor.textAlignmentOptions
                case 'font-family':
                    return descriptor.fontFamilyOptions
                default:
                    return []
            }
        }
    },
    async created() {},
    methods: {
        setSelectedValue(value: string) {
            if (value === 'input') {
                this.inputVisible = !this.inputVisible
            } else {
                this.inputVisible = false
                this.inputValue = ''
            }
            this.modelValue = value
            this.$emit('selected', this.modelValue)
        },
        onInputChanged() {
            this.$emit('inputChanged', this.inputValue)
        }
    }
})
</script>

<style lang="scss" scoped>
.toolbar-context-menu {
    padding: 0.25rem 0;
    background: #ffffff;
    color: #495057;
    border: 0 none;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    border-radius: 6px;
    width: 12.5rem;
    position: relative;
}

.toolbar-context-item {
    min-height: 40px;
    padding-left: 1rem;
    display: flex;
    flex: 1;
    flex-direction: row;
    justify-content: flex-start;
    align-items: center;
    overflow: hidden;
}

.toolbar-context-menu-input {
    position: absolute;
    min-height: 40px;
    top: 0px;
    left: 100%;
    z-index: 99999;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar-context-menu-option {
    border-bottom: 1px solid #c2c2c2;
}

.toolbar-context-menu-option:last-child {
    border-bottom: none;
}
</style>
