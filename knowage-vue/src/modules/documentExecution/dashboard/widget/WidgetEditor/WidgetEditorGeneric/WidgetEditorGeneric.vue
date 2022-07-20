<template>
    <div v-if="model">
        <div v-for="(row, index) in descriptor[model.type]" :key="index">
            <div v-for="(component, index) in row.components" :key="index" :class="row.cssClasses">
                <div v-if="component.type === 'inputText'">
                    <label v-if="component.labelText" :class="component.labelCssClasses"> {{ $t(component.labelText) }}</label>
                    <InputText :class="component.cssClasses" v-model="model[component.property]" @input="onInputTextInput(component)" />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import descriptor from './WidgetEditorGenericDescriptor.json'

export default defineComponent({
    name: 'widget-editor-generic',
    components: {},
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
        onInputTextInput(component: any) {
            if (this[component.callback]) {
                console.log(this[component.callback])
            }
        },
        testFunction() {
            console.log('WOOOOOOOOOOOOOOOOORKS!')
        }
    }
})
</script>
