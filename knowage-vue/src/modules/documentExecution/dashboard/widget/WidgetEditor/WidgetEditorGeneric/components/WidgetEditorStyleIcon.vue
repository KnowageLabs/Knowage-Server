<template>
    <div class="widget-editor-toolbar-icon-container kn-flex">
        <i :class="[icon.class, active ? 'active-icon' : '']" class="kn-cursor-pointer" @click="onIconClicked(icon)"></i>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getModelProperty } from '../WidgetEditorGenericHelper'

export default defineComponent({
    name: 'name',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, icon: { type: Object, required: true } },
    data() {
        return {
            active: false
        }
    },
    created() {
        this.iconIsActive()
        if (this.icon.watchers) {
            for (let i = 0; i < this.icon.watchers.length; i++) {
                this.$watch(
                    'widgetModel.' + this.icon.watchers[i],
                    () => this.iconIsActive(),

                    { deep: true }
                )
            }
        }
    },
    methods: {
        onIconClicked(icon: any) {
            if (!icon || !icon.function) return

            const tempFunction = getModelProperty(this.widgetModel, icon.function, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return tempFunction(this.widgetModel)
        },
        iconIsActive() {
            console.log('iconIsActive')
            if (!this.icon.isActiveFunction) return (this.active = false)

            const tempFunction = getModelProperty(this.widgetModel, this.icon.isActiveFunction, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.active = tempFunction(this.widgetModel))
        }
    }
})
</script>

<style lang="scss" scoped>
.widget-editor-toolbar-icon-container {
    text-align: center;
}

.active-icon {
    color: blue;
}
</style>
