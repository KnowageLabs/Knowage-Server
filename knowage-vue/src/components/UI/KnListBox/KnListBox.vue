<template>
    <Listbox
        class="kn-list knListBox"
        :options="options"
        :class="{ noSorting: !settings.sortFields }"
        listStyle="max-height:calc(100% - 62px)"
        :filter="true"
        :filterPlaceholder="$t('common.search')"
        filterMatchMode="contains"
        :filterFields="settings.filterFields"
        :emptyFilterMessage="$t('common.info.noDataFound')"
        data-test="list"
    >
        <template #header v-if="settings.sortFields">
            <Button icon="fas fa-sort-amount-down-alt" class="p-button-text p-button-rounded p-button-plain headerButton" @click="toggleSort" v-tooltip.bottom="$t('common.sort')" />
            <Menu id="sortMenu" ref="sortMenu" :model="settings.sortFields" :popup="true">
                <template #item="{item}">
                    <a class="p-menuitem-link" role="menuitem" tabindex="0" @click="sort($event, item)">
                        <span v-if="selectedDirection === 'asc'" class="p-menuitem-icon fas" :class="{ 'fa-sort-amount-up-alt': selectedSort === item }"></span>
                        <span v-else class="p-menuitem-icon fas" :class="{ 'fa-sort-amount-down-alt': selectedSort === item }"></span>
                        <span class="p-menuitem-text">{{ $t(item) }}</span>
                    </a>
                </template>
            </Menu>
        </template>
        <template #option="slotProps">
            <router-link class="kn-decoration-none" :to="{ name: settings.interaction.path, params: { id: slotProps.option.id } }" exact v-if="settings.interaction.type === 'router'">
                <div class="kn-list-item" v-tooltip="slotProps.option[settings.tooltipField || 'description']" :class="getBorderClass(slotProps.option)" data-test="list-item">
                    <Avatar
                        v-if="settings.avatar && settings.avatar.values[slotProps.option[settings.avatar.property]]"
                        :icon="settings.avatar.values[slotProps.option[settings.avatar.property]].icon"
                        shape="circle"
                        size="medium"
                        :style="settings.avatar.values[slotProps.option[settings.avatar.property]].style"
                    />
                    <div class="kn-list-item-text">
                        <span v-if="settings.titleField !== false">{{ slotProps.option[settings.titleField || 'label'] }}</span>
                        <span class="kn-list-item-text-secondary kn-truncated" v-if="settings.textField !== false">{{ slotProps.option[settings.textField || 'name'] }}</span>
                    </div>
                    <Badge v-if="settings.badgeField" :value="slotProps.option[settings.badgeField]" :severity="settings.badgeSeverity || 'info'"></Badge>
                    <Badge v-if="settings.badgeIcon && slotProps.option[settings.badgeIcon] === true" :severity="settings.badgeSeverity || 'info'">
                        <i class="fas fa-check"></i>
                    </Badge>
                    <KnListButtonRenderer :buttons="settings.buttons" @click="clickedButton($event, slotProps.option)" />
                </div>
            </router-link>
            <div
                class="kn-list-item"
                v-tooltip="slotProps.option[settings.tooltipField || 'description']"
                v-if="!settings.interaction || settings.interaction.type === 'event'"
                @click="clickedButton($event, slotProps.option)"
                :class="[{ 'router-link-active': selected && selected == slotProps.option }, getBorderClass(slotProps.option)]"
                data-test="list-item"
            >
                <Avatar
                    v-if="settings.avatar && settings.avatar.values[slotProps.option[settings.avatar.property]]"
                    :icon="settings.avatar.values[slotProps.option[settings.avatar.property]].icon"
                    shape="circle"
                    size="medium"
                    :style="settings.avatar.values[slotProps.option[settings.avatar.property]].style"
                />
                <div class="kn-list-item-text">
                    <span v-if="settings.titleField !== false">{{ slotProps.option[settings.titleField || 'label'] }}</span>
                    <span v-if="settings.textField !== false && !settings.textFieldType" class="kn-list-item-text-secondary kn-truncated">{{ slotProps.option[settings.textField || 'name'] }}</span>
                    <span v-if="settings.textField !== false && settings.textFieldType && settings.textFieldType === 'date'" class="kn-list-item-text-secondary kn-truncated">{{ getTime(slotProps.option[settings.textField || 'name']) }}</span>
                </div>
                <Badge v-if="settings.badgeField && slotProps.option[settings.badgeField]" :value="slotProps.option[settings.badgeField]" :severity="settings.badgeSeverity || 'info'"></Badge>
                <Badge v-if="settings.badgeIcon && slotProps.option[settings.badgeIcon] === true" :severity="settings.badgeSeverity || 'info'">
                    <i class="fas fa-check"></i>
                </Badge>
                <KnListButtonRenderer :buttons="settings.buttons" @click="clickedButton($event, slotProps.option)" />
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
        data() {
            return {
                selectedSort: 'label',
                selectedDirection: 'desc'
            }
        },
        emits: ['click'],
        created() {
            this.selectedSort = this.settings.defaultSortField || 'label'
        },
        mounted() {
            this.sort(null, this.selectedSort)
        },
        computed: {
            getTime(ms) {
                return formatDateWithLocale(ms)
            }
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
            toggleSort(e) {
                // eslint-disable-next-line
                // @ts-ignore
                this.$refs.sortMenu.toggle(e)
            },
            sort(e, item) {
                if (this.selectedSort === item) this.selectedDirection = this.selectedDirection === 'desc' ? 'asc' : 'desc'
                else {
                    this.selectedSort = item
                    this.selectedDirection = 'desc'
                }
                if (this.selectedDirection === 'desc') this.options?.sort((a: any, b: any) => (a[this.selectedSort] > b[this.selectedSort] ? 1 : -1))
                else this.options?.sort((a: any, b: any) => (a[this.selectedSort] > b[this.selectedSort] ? -1 : 1))
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
