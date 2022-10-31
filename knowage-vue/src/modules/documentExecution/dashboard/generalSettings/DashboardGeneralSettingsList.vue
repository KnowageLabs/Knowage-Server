<template>
    <div class="dashboard-editor-list-card-container p-m-3">
        <div class="dashboard-editor-list-card">
            <Listbox
                class="kn-list kn-list-no-border-right dashboard-editor-list"
                :options="descriptor.settingsList"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                filterMatchMode="contains"
                :filterFields="descriptor.settingsListFilterFields"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="selectOption"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" :style="descriptor.listStyle.listItem">
                        <i v-if="slotProps.option.icon" class="p-mx-2" :style="descriptor.listStyle.listIcon" :class="slotProps.option.icon"></i>
                        <div class="kn-list-item-text">
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'
import descriptor from './DashboardGeneralSettingsDescriptor.json'
export default defineComponent({
    name: 'general-settings-list',
    components: { Listbox },
    emits: ['selectedOption'],
    data() {
        return {
            descriptor
        }
    },
    setup() {},
    created() {},
    methods: {
        selectOption(event: any) {
            if (event.value) this.$emit('selectedOption', event.value.value)
        }
    }
})
</script>

<style lang="scss">
.dashboard-editor-list-card-container {
    display: flex;
    flex-direction: column;
    width: 300px;
    background: #ffffff;
    color: rgba(0, 0, 0, 0.87);
    box-shadow: 0 2px 1px -1px rgb(0 0 0 / 20%), 0 1px 1px 0 rgb(0 0 0 / 14%), 0 1px 3px 0 rgb(0 0 0 / 12%);
    border-radius: 4px;
    .dashboard-editor-list-card,
    .dashboard-editor-list {
        display: flex;
        flex-direction: column;
        flex: 1;
        min-height: 0;
        border-radius: 4px !important;
        .kn-list-item-text {
            text-overflow: ellipsis;
            max-width: 190px;
            overflow: hidden;
            white-space: nowrap;
        }
    }
}
</style>
