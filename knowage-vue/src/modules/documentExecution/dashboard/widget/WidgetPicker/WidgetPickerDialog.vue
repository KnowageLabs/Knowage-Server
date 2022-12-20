<template>
    <Dialog class="dialog-no-padding" :visible="visible" :style="descriptor.style.dialog" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #start>
                    {{ $t('documentExecution.olap.showMdxQuery') }}
                </template>
                <template #end>
                    <Button icon="pi pi-times" class="p-button-link" @click="$emit('closeWidgetPicker')" />
                </template>
            </Toolbar>
        </template>

        <div id="widget-card-container" class="p-grid p-jc-center p-m-2">
            <WidgetCard v-for="(widget, index) in widgetTypes" :key="index" :widget="widget" @click="openWidgetEditor(widget)" />
        </div>
    </Dialog>
</template>

<script lang="ts">
/**
 * ! this component is in charge of opening the correct widget editor and containing all the cards
 */
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { IWidgetPickerType } from '../../Dashboard'
import { mapState } from 'pinia'
import appStore from '@/App.store'
import Dialog from 'primevue/dialog'
import WidgetCard from './WidgetPickerCard.vue'
import descriptor from './WidgetPickerDescriptor.json'

export default defineComponent({
    name: 'widget-picker-dialog',
    components: { Dialog, WidgetCard },
    emits: ['closeWidgetPicker', 'openNewWidgetEditor'],
    inject: [],
    props: { visible: { type: Boolean } },
    data() {
        return {
            descriptor,
            widgetTypes: [] as IWidgetPickerType[]
        }
    },
    computed: {
        ...mapState(appStore, {
            user: 'user'
        })
    },
    created() {
        this.getWidgetTypes()
    },
    methods: {
        async getWidgetTypes() {
            await this.$http.get(import.meta.env.VITE_DASHBOARD_PATH + `1.0/engine/widget`).then((response: AxiosResponse<any>) => (this.widgetTypes = response.data))
        },
        openWidgetEditor(widget) {
            // TODO widgetChange
            // if (widget.type === 'chart') widget.type = this.user?.enterprise ? 'highcharts' : 'chartJS'
            if (widget.type === 'chart') widget.type = false ? 'highcharts' : 'chartJS'
            this.$emit('openNewWidgetEditor', widget)
        }
    }
})
</script>
<style lang="scss">
.dialog-no-padding.p-dialog .p-dialog-header,
.dialog-no-padding.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}
</style>
