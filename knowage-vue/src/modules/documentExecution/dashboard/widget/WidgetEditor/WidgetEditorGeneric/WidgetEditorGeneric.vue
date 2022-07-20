<template>
    <div v-if="model">
        <div v-for="(row, index) in descriptor[model.type]" :key="index" :class="row.cssClasses">
            <template v-for="(component, tempIndex) in row.components" :key="tempIndex">
                <WidgetEditorInputText v-if="component.type === 'inputText'" :label="component.label" :class="component.cssClass" @input="onInputTextInput($event, component)" @change="onInputTextChange($event, component)"></WidgetEditorInputText>
                <WidgetEditorInputSwitch v-if="component.type === 'inputSwitch'" :class="component.cssClass" :inputClass="component.inputClass" :label="component.label" @change="onInputSwitchChange($event, component)"></WidgetEditorInputSwitch>
            </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import descriptor from './WidgetEditorGenericDescriptor.json'
import WidgetEditorInputSwitch from './components/WidgetEditorInputSwitch.vue'
import WidgetEditorInputText from './components/WidgetEditorInputText.vue'

export default defineComponent({
    name: 'widget-editor-generic',
    components: { WidgetEditorInputSwitch, WidgetEditorInputText },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IWidget | null
        }
    },
    watch: {
        widgetModel() {
            this.loadModel()
        }
    },
    async created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel
            console.log('LOADED MODEL: ', this.model)
        },
        onInputTextInput(value: string, component: any) {
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onInputTextChange(value: string, component: any) {
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onInputSwitchChange(value: string, component: any) {
            if (component.property) this.updateModelProperty(value, component.property)
        },
        updateModelProperty(value: any, propertyPath: string) {
            if (!this.model) return
            const stack = propertyPath.split('.')
            if (!stack || stack.length === 0) return

            let property = null as any
            let tempModel = this.model
            while (stack.length > 1) {
                property = stack.shift()
                if (property && this.model) tempModel = tempModel[property]
            }
            property = stack.shift()
            tempModel[property] = value

            console.log('UPDATED MODEL: ', this.model)
        }
        // onInputTextInput(component: any) {
        //     if (this[component.callback]) {
        //         console.log(this[component.callback])
        //     }
        // },
        // testFunction() {
        //     console.log('WOOOOOOOOOOOOOOOOORKS!')
        // }
    }
})
</script>
