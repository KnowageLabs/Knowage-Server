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
                        <Button
                            :style="{ 'background-image': 'url(' + require('@/assets/images/olap/mdx.png') + ')' }"
                            class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"
                            v-tooltip.top="$t('documentExecution.olap.sidebar.mdxQuery')"
                            @click="$emit('openMdxQueryDialog')"
                        ></Button>
                    </div>
                    <div class="p-col-4">
                        <Button
                            :style="{ 'background-image': 'url(' + require('@/assets/images/olap/reload16.png') + ')' }"
                            class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"
                            v-tooltip.top="$t('documentExecution.olap.sidebar.reloadSchema')"
                            @click="$emit('reloadSchema')"
                        ></Button>
                    </div>
                    <div class="p-col-4">
                        <Button
                            :style="{ 'background-image': 'url(' + require('@/assets/images/olap/cross-navigation.png') + ')' }"
                            class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"
                            v-tooltip.top="$t('documentExecution.olap.sidebar.enableCrossNavigation')"
                            @click="onEnableCrossNavigationClick"
                        ></Button>
                    </div>
                    <div class="p-col-4"></div>
                </div>
            </div>

            <div>
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.tableFunctions') }}</label>
                <div class="p-grid">
                    <div class="p-col-4">
                        <Button
                            :style="{ 'background-image': 'url(' + require('@/assets/images/olap/show_parent_members.png') + ')' }"
                            class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"
                            v-tooltip.top="$t('documentExecution.olap.sidebar.showParentMembers')"
                            @click="onShowParentMemberClick"
                        ></Button>
                    </div>
                    <div class="p-col-4">
                        <Button :style="{ 'background-image': 'url(' + require('@/assets/images/olap/cc.png') + ')' }" class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.calculatedFieldWizard')"></Button>
                    </div>
                    <div class="p-col-4">
                        <Button
                            :style="{ 'background-image': 'url(' + require('@/assets/images/olap/hide_spans.png') + ')' }"
                            class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"
                            v-tooltip.top="$t('documentExecution.olap.sidebar.hideSpans')"
                            @click="onHideSpansClick"
                        ></Button>
                    </div>
                    <div class="p-col-4">
                        <Button
                            :style="{ 'background-image': 'url(' + require('@/assets/images/olap/sorting-settings.png') + ')' }"
                            class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"
                            v-tooltip.top="$t('documentExecution.olap.sidebar.sortingSettings')"
                            @click="$emit('openSortingDialog')"
                        ></Button>
                    </div>
                    <div class="p-col-4">
                        <Button
                            :style="{ 'background-image': 'url(' + require('@/assets/images/olap/show_props.png') + ')' }"
                            class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"
                            v-tooltip.top="$t('documentExecution.olap.sidebar.showProperties')"
                            @click="onShowPropertiesClick"
                        ></Button>
                    </div>
                    <div class="p-col-4">
                        <Button
                            :style="{ 'background-image': 'url(' + require('@/assets/images/olap/empty_rows.png') + ')' }"
                            class="olap-sidebar-image-buttons p-button-sm kn-button kn-button--secondary"
                            v-tooltip.top="$t('documentExecution.olap.sidebar.suppressEmptyRowsColumns')"
                            @click="onSuppressRowsColumnsClick"
                        ></Button>
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
    emits: ['openCustomViewDialog', 'drillTypeChanged', 'showParentMemberChanged', 'hideSpansChanged', 'suppressEmptyChanged', 'showPropertiesChanged', 'openSortingDialog', 'openMdxQueryDialog', 'reloadSchema', 'enableCrossNavigation'],
    data() {
        return {
            olapSidebarDescriptor,
            drillOn: 'position',
            showParentMembers: false,
            hideSpans: false,
            suppressEmpty: false,
            showProperties: false,
            crossNavigation: false,
            mode: 'designer'
        }
    },
    watch: {
        olap() {
            this.loadOlapModelConfigValues()
        }
    },
    created() {
        this.loadOlapModelConfigValues()
    },
    methods: {
        loadOlapModelConfigValues() {
            if (this.olap) {
                this.drillOn = this.olap.modelConfig.drillType
                this.showParentMembers = this.olap.modelConfig.showParentMembers
                this.hideSpans = this.olap.modelConfig.hideSpans
                this.suppressEmpty = this.olap.modelConfig.suppressEmpty
                this.showProperties = this.olap.modelConfig.showProperties
                this.crossNavigation = this.olap.modelConfig?.crossNavigation?.buttonClicked
            }
        },
        onShowParentMemberClick() {
            this.showParentMembers = !this.showParentMembers
            this.$emit('showParentMemberChanged', this.showParentMembers)
        },
        onHideSpansClick() {
            this.hideSpans = !this.hideSpans
            this.$emit('hideSpansChanged', this.hideSpans)
        },
        onSuppressRowsColumnsClick() {
            this.suppressEmpty = !this.suppressEmpty
            this.$emit('suppressEmptyChanged', this.suppressEmpty)
        },
        onShowPropertiesClick() {
            this.showProperties = !this.showProperties
            this.$emit('showPropertiesChanged', this.showProperties)
        },
        onEnableCrossNavigationClick() {
            this.crossNavigation = !this.crossNavigation
            this.$emit('enableCrossNavigation', this.crossNavigation)
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
