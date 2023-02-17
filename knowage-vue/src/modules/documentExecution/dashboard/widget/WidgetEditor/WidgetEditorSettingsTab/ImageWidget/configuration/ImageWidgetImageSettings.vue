<template>
    <div v-if="imageSettings" class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.height') }}</label>
            <InputText v-model="imageSettings.style.height" class="kn-material-input p-inputtext-sm" @change="imageSettingsChanged" />
        </div>

        <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.width') }}</label>
            <InputText v-model="imageSettings.style.width" class="kn-material-input p-inputtext-sm" @change="imageSettingsChanged" />
        </div>

        <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.horizontalAlign') }}</label>
            <Dropdown v-model="imageSettings.style['background-position-x']" class="kn-material-input kn-flex" :options="descriptor.horizontalAlignmentOptions" option-value="value" @change="imageSettingsChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.horizontalAlignmentOptions, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>

        <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.verticalAlign') }}</label>

            <Dropdown v-model="imageSettings.style['background-position-y']" class="kn-material-input kn-flex" :options="descriptor.verticalAlignmentOptions" option-value="value" @change="imageSettingsChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.verticalAlignmentOptions, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { IImageWidgetImageSettings } from '@/modules/documentExecution/dashboard/interfaces/DashboardImageWidget'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../ImageWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'image-widget-image-settings',
    components: { Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            imageSettings: null as IImageWidgetImageSettings | null,
            getTranslatedLabel
        }
    },
    created() {
        this.loadImageSettings()
    },
    methods: {
        loadImageSettings() {
            if (this.widgetModel.settings.configuration?.image) this.imageSettings = this.widgetModel.settings.configuration.image
        },
        imageSettingsChanged() {
            emitter.emit('refreshImageWidget', this.widgetModel.id)
        }
    }
})
</script>
