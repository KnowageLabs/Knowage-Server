<template>
    <Listbox
        class="kn-list knListBox"
        :options="sortedOptions"
        :class="{ noSorting: !settings.sortFields }"
        list-style="max-height:calc(100% - 62px)"
        :filter="true"
        :filter-placeholder="$t('common.search')"
        filter-match-mode="contains"
        :filter-fields="settings.filterFields"
        :empty-filter-message="$t('common.info.noDataFound')"
        data-test="list"
    >
        <template v-if="settings.sortFields" #header>
            <Button v-tooltip.bottom="$t('common.sort')" icon="fas fa-sort-amount-down-alt" class="p-button-text p-button-rounded p-button-plain headerButton" @click="toggleSort" />
            <Menu id="sortMenu" ref="sortMenu" :model="settings.sortFields" :popup="true">
                <template #item="{item}">
                    <a class="p-menuitem-link" role="menuitem" tabindex="0" @click="sort($event, item.name || item)">
                        <span v-if="selectedDirection === 'asc'" class="p-menuitem-icon fas" :class="{ 'fa-sort-amount-up-alt': selectedSort === (item.name || item) }"></span>
                        <span v-else class="p-menuitem-icon fas" :class="{ 'fa-sort-amount-down-alt': selectedSort === (item.name || item) }"></span>
                        <span class="p-menuitem-text">{{ $t(item.label || item) }}</span>
                    </a>
                </template>
            </Menu>
        </template>
        <template #option="slotProps">
            <router-link v-if="settings.interaction.type === 'router'" class="kn-decoration-none" :to="{ name: settings.interaction.path, params: { id: slotProps.option.id } }" exact>
                <div v-tooltip="slotProps.option[settings.tooltipField || 'description']" class="kn-list-item" :class="getBorderClass(slotProps.option)" data-test="list-item">
                    <Avatar v-if="settings.avatar && settings.avatar.values[slotProps.option[settings.avatar.property]]" :icon="settings.avatar.values[slotProps.option[settings.avatar.property]].icon" shape="circle" :style="settings.avatar.values[slotProps.option[settings.avatar.property]].style" />
                    <div class="kn-list-item-text">
                        <span v-if="settings.titleField !== false">{{ slotProps.option[settings.titleField || 'label'] }}</span>
                        <span v-if="settings.textField !== false" class="kn-list-item-text-secondary kn-truncated">{{ slotProps.option[settings.textField || 'name'] }}</span>
                    </div>
                    <Badge v-if="settings.badgeField" :value="slotProps.option[settings.badgeField]" :severity="settings.badgeSeverity || 'info'"></Badge>
                    <Badge v-if="settings.badgeIcon && slotProps.option[settings.badgeIcon] === true" :severity="settings.badgeSeverity || 'info'">
                        <i class="fas fa-check"></i>
                    </Badge>
                    <KnListButtonRenderer :buttons="settings.buttons" @click="clickedButton($event, slotProps.option)" />
                </div>
            </router-link>
            <div
                v-if="!settings.interaction || settings.interaction.type === 'event'"
                v-tooltip="slotProps.option[settings.tooltipField || 'description']"
                class="kn-list-item"
                :class="[{ 'router-link-active': isItemSelected(slotProps.option) }, getBorderClass(slotProps.option)]"
                data-test="list-item"
                @click="clickedButton($event, slotProps.option)"
            >
                <Avatar v-if="settings.avatar && settings.avatar.values[slotProps.option[settings.avatar.property]]" :icon="settings.avatar.values[slotProps.option[settings.avatar.property]].icon" shape="circle" :style="settings.avatar.values[slotProps.option[settings.avatar.property]].style" />
                <div class="kn-list-item-text">
                    <span v-if="settings.titleField !== false">{{ slotProps.option[settings.titleField || 'label'] }}</span>
                    <span v-if="settings.textField !== false && !settings.textFieldType" class="kn-list-item-text-secondary kn-truncated">{{ slotProps.option[settings.textField || 'name'] }}</span>
                    <span v-if="settings.textField !== false && settings.textFieldType && settings.textFieldType === 'date'" class="kn-list-item-text-secondary kn-truncated">{{ getTime(slotProps.option[settings.textField || 'name']) }}</span>
                </div>
                <Badge v-if="settings.badgeField && slotProps.option[settings.badgeField]" :value="slotProps.option[settings.badgeField]" :severity="settings.badgeSeverity || 'info'"></Badge>
                <Badge v-if="settings.badgeIcon && slotProps.option[settings.badgeIcon] === true" :severity="settings.badgeSeverity || 'info'">
                    <i class="fas fa-check"></i>
                </Badge>
                <KnListButtonRenderer :buttons="settings.buttons" :selected-item="slotProps.option" @click="clickedButton($event, slotProps.option)" />
            </div>
        </template>
    </Listbox>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Avatar from 'primevue/avatar'
import Badge from 'primevue/badge'
import Listbox from 'primevue/listbox'
import KnListButtonRenderer from './KnListButtonRenderer.vue'
import Menu from 'primevue/menu'
import { formatDateWithLocale } from '@/helpers/commons/localeHelper'

export default defineComponent({
    name: 'kn-list-box',
    components: {
        Avatar,
        Badge,
        KnListButtonRenderer,
        Listbox,
        Menu
    },
    props: {
        settings: {
            type: Object,
            required: true
        },
        options: Array,
        selected: Object
    },
    emits: ['click'],
    data() {
        return {
            selectedSort: 'label',
            selectedDirection: '',
            sortedOptions: [] as Array<any>
        }
    },
    computed: {},
    created() {
        this.selectedSort = this.settings.defaultSortField || 'label'
        this.sort(null, this.selectedSort, true)
    },
    updated() {
        this.sort(null, this.selectedSort, true)
    },
    methods: {
        clickedButton(e, item) {
            const emits = e.item && e.item.emits
            e.item = item
            this.$emit(emits || 'click', e)
        },
        getBorderClass(item): string {
            if (this.settings.statusBorder) {
                return 'kn-list-item-' + this.settings.statusBorder.values[item[this.settings.statusBorder.property]]
            } else return ''
        },
        isItemSelected(option) {
            if (this.selected) {
                if (this.settings.selectProperty && this.selected[this.settings.selectProperty]) {
                    return this.selected[this.settings.selectProperty] == option[this.settings.selectProperty]
                } else {
                    return this.selected == option
                }
            } else return false
        },
        toggleSort(e) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.sortMenu.toggle(e)
        },
        sort(e, item, desc?) {
            this.sortedOptions = this.options ? this.options : []
            if (this.selectedSort === item) this.selectedDirection = this.selectedDirection === 'desc' ? 'asc' : 'desc'
            else {
                this.selectedSort = item
                this.selectedDirection = 'desc'
            }

            if (e || (!e && this.selectedDirection === '')) {
                if (this.selectedDirection === '') this.selectedDirection = 'desc'
                if (desc || this.selectedDirection === 'desc') this.sortedOptions.sort((a: any, b: any) => (a[this.selectedSort] > b[this.selectedSort] ? 1 : -1))
                else this.sortedOptions.sort((a: any, b: any) => (a[this.selectedSort] > b[this.selectedSort] ? -1 : 1))
            }
        },
        getTime(ms) {
            return formatDateWithLocale(ms)
        }
    }
})
</script>
<style lang="scss">
.knListBox {
    position: relative;
    flex: 1;
    overflow-y: auto;

    .headerButton {
        position: absolute;
        right: 8px;
        top: 16px;
    }
    &.noSorting {
        .p-listbox-header {
            .p-listbox-filter-container {
                width: 100%;
            }
        }
    }
    .p-listbox-header {
        .p-listbox-filter-container {
            width: calc(100% - 36px);
        }
    }
}
</style>
