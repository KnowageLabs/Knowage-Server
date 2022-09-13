<template>
    <div v-if="paddingStyleModel">
        <div class="p-d-flex p-flex-row p-ai-center p-mt-2 p-mb-4">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.padding.enablePadding') }}</label>
                <InputSwitch v-model="paddingStyleModel.enabled" @change="paddingStyleChanged"></InputSwitch>
            </div>
        </div>

        <div class="p-d-flex p-flex-row p-ai-center">
            <i
                :class="paddingStyleModel.properties.unlinked ? 'fa fa-link' : 'fa fa-unlink'"
                class="kn-cursor-pointer p-mr-2"
                v-tooltip="paddingStyleModel.properties.unlinked ? $t('dashboard.widgetEditor.padding.linkAllHint') : $t('dashboard.widgetEditor.padding.unlinkAllHint')"
                @click="onLinkIconClicked"
            ></i>
            <div id="padding-left-container" class="p-d-flex p-flex-column kn-flex p-mx-2">
                <label class="kn-material-input-label p-mr-2">{{ paddingStyleModel.properties.unlinked ? $t('dashboard.widgetEditor.padding.paddingLeft') : $t('dashboard.widgetEditor.padding.title') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="paddingStyleModel.properties['padding-left']" :disabled="paddingStyleDisabled" @change="onPaddingLeftInputChange" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
            <div class="p-d-flex p-flex-column kn-flex p-mx-2" v-if="paddingStyleModel.properties.unlinked">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.padding.paddingTop') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="paddingStyleModel.properties['padding-top']" :disabled="paddingStyleDisabled" @change="paddingStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
            <div class="p-d-flex p-flex-column kn-flex p-mx-2" v-if="paddingStyleModel.properties.unlinked">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.padding.paddingRight') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="paddingStyleModel.properties['padding-right']" :disabled="paddingStyleDisabled" @change="paddingStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
            <div class="p-d-flex p-flex-column kn-flex p-mx-2" v-if="paddingStyleModel.properties.unlinked">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.padding.paddingBottom') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="paddingStyleModel.properties['padding-bottom']" :disabled="paddingStyleDisabled" @change="paddingStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetPaddingStyle } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-padding-style',
    components: { InputSwitch },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            descriptor,
            paddingStyleModel: null as ITableWidgetPaddingStyle | null
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
            if (this.widgetModel?.settings?.style?.padding) this.paddingStyleModel = this.widgetModel.settings.style.padding
        },
        paddingStyleChanged() {
            emitter.emit('paddingStyleChanged', this.paddingStyleModel)
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
