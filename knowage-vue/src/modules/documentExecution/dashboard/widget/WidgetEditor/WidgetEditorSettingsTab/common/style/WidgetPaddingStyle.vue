<template>
    <div v-if="paddingStyleModel" class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12 p-grid p-ai-center">
            <div class="p-d-flex p-jc-center p-ai-center">
                <i
                    :class="paddingStyleModel.properties.unlinked ? 'fa fa-link' : 'fa fa-unlink'"
                    class="kn-cursor-pointer p-mr-2"
                    v-tooltip="paddingStyleModel.properties.unlinked ? $t('dashboard.widgetEditor.padding.linkAllHint') : $t('dashboard.widgetEditor.padding.unlinkAllHint')"
                    @click="onLinkIconClicked"
                ></i>
            </div>

            <div id="padding-left-container" class="p-col-11 p-md-5 p-lg-2 p-d-flex p-flex-column kn-flex p-px-2">
                <label class="kn-material-input-label p-mr-2">{{ paddingStyleModel.properties.unlinked ? $t('dashboard.widgetEditor.padding.paddingLeft') : $t('dashboard.widgetEditor.padding.title') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="paddingStyleModel.properties['padding-left']" :disabled="paddingStyleDisabled" @input="onPaddingLeftInputChange" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
            <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column p-px-2" v-if="paddingStyleModel.properties.unlinked">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.padding.paddingTop') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="paddingStyleModel.properties['padding-top']" :disabled="paddingStyleDisabled" @change="paddingStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
            <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column p-px-2" v-if="paddingStyleModel.properties.unlinked">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.padding.paddingRight') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="paddingStyleModel.properties['padding-right']" :disabled="paddingStyleDisabled" @change="paddingStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
            <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column p-px-2" v-if="paddingStyleModel.properties.unlinked">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.padding.paddingBottom') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="paddingStyleModel.properties['padding-bottom']" :disabled="paddingStyleDisabled" @change="paddingStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetPaddingStyle } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'widget-padding-style',
    components: { InputSwitch },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            paddingStyleModel: null as IWidgetPaddingStyle | null,
            widgetType: '' as string
        }
    },
    computed: {
        paddingStyleDisabled() {
            return !this.paddingStyleModel || !this.paddingStyleModel.enabled
        }
    },
    created() {
        this.loadPaddingStyle()
    },
    methods: {
        loadPaddingStyle() {
            if (!this.widgetModel) return
            this.widgetType = this.widgetModel.type
            if (this.widgetModel.settings?.style?.padding) this.paddingStyleModel = this.widgetModel.settings.style.padding
        },
        paddingStyleChanged() {
            emitter.emit('paddingStyleChanged', this.paddingStyleModel)
            switch (this.widgetType) {
                case 'table':
                    emitter.emit('refreshTable', this.widgetModel.id)
                    break
                case 'selector':
                    emitter.emit('refreshSelector', this.widgetModel.id)
                    break
                case 'selection':
                    emitter.emit('refreshSelection', this.widgetModel.id)
            }
        },
        onLinkIconClicked() {
            if (!this.paddingStyleModel) return
            this.paddingStyleModel.properties.unlinked = !this.paddingStyleModel.properties.unlinked
            this.linkAllPaddingValues()
            this.paddingStyleChanged()
        },
        onPaddingLeftInputChange() {
            this.linkAllPaddingValues()
            this.paddingStyleChanged()
        },
        linkAllPaddingValues() {
            if (!this.paddingStyleModel) return
            if (!this.paddingStyleModel.properties.unlinked) {
                this.paddingStyleModel.properties['padding-top'] = this.paddingStyleModel.properties['padding-left']
                this.paddingStyleModel.properties['padding-right'] = this.paddingStyleModel.properties['padding-left']
                this.paddingStyleModel.properties['padding-bottom'] = this.paddingStyleModel.properties['padding-left']
            }
        }
    }
})
</script>

<style lang="scss" scoped>
#padding-left-container {
    max-width: 300px;
}
</style>
