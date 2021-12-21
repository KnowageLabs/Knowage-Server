<template>
    <div id="olap-sidebar">
        <Toolbar id="kn-parameter-sidebar-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('common.settings') }}
            </template>
        </Toolbar>

        <div v-if="olap" class="p-d-flex p-flex-column p-m-2">
            <div class="p-m-2">
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.drillOnDimension') }}</label>
                <SelectButton v-model="drillOn" :options="olapSidebarDescriptor.drillOnOptions" @click="$emit('drillTypeChanged', drillOn)"></SelectButton>
            </div>

            <div class="p-d-flex p-flex-column p-m-2">
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.drillOnData') }}</label>
                <Button id="drill-through-button" class="p-button-sm kn-button kn-button--secondary"> {{ $t('documentExecution.olap.sidebar.drillThrough') }}</Button>
            </div>

            <div>
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.olapFunctions') }}</label>
                <div class="p-grid">
                    <div class="p-col-4">
                        <Button :style="{ 'background-image': 'url(' + require('@/assets/images/olap/mdx.png') + ')' }" class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"></Button>
                    </div>
                    <div class="p-col-4">
                        <Button :style="{ 'background-image': 'url(' + require('@/assets/images/olap/reload16.png') + ')' }" class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"></Button>
                    </div>
                    <div class="p-col-4"></div>
                </div>
            </div>

            <div>
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.tableFunctions') }}</label>
                <div class="p-grid">
                    <div class="p-col-4">
                        <Button :style="{ 'background-image': 'url(' + require('@/assets/images/olap/show_parent_members.png') + ')' }" class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"></Button>
                    </div>
                    <div class="p-col-4">
                        <Button :style="{ 'background-image': 'url(' + require('@/assets/images/olap/cc.png') + ')' }" class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"></Button>
                    </div>
                    <div class="p-col-4">
                        <Button :style="{ 'background-image': 'url(' + require('@/assets/images/olap/hide_spans.png') + ')' }" class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"></Button>
                    </div>
                    <div class="p-col-4">
                        <Button :style="{ 'background-image': 'url(' + require('@/assets/images/olap/sorting-settings.png') + ')' }" class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"></Button>
                    </div>
                    <div class="p-col-4">
                        <Button :style="{ 'background-image': 'url(' + require('@/assets/images/olap/show_props.png') + ')' }" class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"></Button>
                    </div>
                    <div class="p-col-4">
                        <Button :style="{ 'background-image': 'url(' + require('@/assets/images/olap/empty_rows.png') + ')' }" class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"></Button>
                    </div>
                    <div class="p-col-4">
                        <Button
                            :style="{ 'background-image': 'url(' + require('@/assets/images/olap/savesuboject.png') + ')' }"
                            class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"
                            v-tooltip.top="$t('documentExecution.olap.sidebar.showCustomizedView')"
                            @click="$emit('openCustomViewDialog')"
                        ></Button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import olapSidebarDescriptor from './OlapSidebarDescriptor.json'
import SelectButton from 'primevue/selectbutton'

export default defineComponent({
    name: 'olap-sidebar',
    components: { SelectButton },
    props: { olap: { type: Object } },
    emits: ['openCustomViewDialog', 'drillTypeChanged'],
    data() {
        return {
            olapSidebarDescriptor,
            drillOn: 'position'
        }
    },
    watch: {
        olap() {
            this.loadDrillOn()
        }
    },
    created() {
        this.loadDrillOn()
    },
    methods: {
        loadDrillOn() {
            this.drillOn = this.olap?.modelConfig.drillType
        }
    }
})
</script>

<style lang="scss">
#olap-sidebar {
    z-index: 100;
    background-color: white;
    height: 100%;
    width: 350px;
    position: absolute;
    top: 0;
    right: 0;
    display: flex;
    flex-direction: column;
}

#drill-through-button {
    margin: auto;
    max-width: 100px;
    padding: 0.3rem;
}

.olap-sidebar-image-buttons {
    background-repeat: no-repeat;
    background-position: center;
    background-color: white !important;
}

.olap-sidebar-image-buttons:hover {
    background-repeat: no-repeat !important;
    background-position: center !important;
    background-color: white !important;
}
</style>
