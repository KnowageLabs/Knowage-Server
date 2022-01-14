<template>
    <ul class="p-megamenu-panel" v-if="model && openedPanelEvent" ref="megamenu">
        <div class="p-grid p-mb-1">
            <span class="p-input-icon-left p-col">
                <i class="pi pi-search" />
                <InputText type="text" v-model="searchText" :placeholder="$tc('common.search')" @click="focusInput($event)" @keyup="filter()" />
            </span>
        </div>
        <div style="overflow-y: auto">
            <Message v-if="tmpModel.length === 0" severity="warn" style="min-width: 400px" :closable="false">{{ $t('common.info.emptySearch') }}</Message>
            <div class="p-megamenu-data">
                <div v-for="(column, columnIndex) of tmpModel" :key="column.label + '_column_' + columnIndex" class="menuColumn p-mb-3">
                    <ul class="p-megamenu-submenu">
                        <li role="presentation" class="kn-truncated" v-tooltip.top="$t(column.label)">{{ $t(column.label) }}</li>
                        <template v-for="(item, i) of column.items" :key="item.label + i.toString()">
                            <li role="none" :style="item.style">
                                <router-link v-if="item.to && !item.disabled" :to="item.to" custom v-slot="{ navigate, href }">
                                    <a :href="href" role="menuitem" @click="onLeafClick($event, item, navigate)">
                                        <span class="p-menuitem-text">{{ $t(item.label) }}</span>
                                    </a>
                                </router-link>
                                <a v-else :href="item.url" :target="item.target" @click="onLeafClick($event, item, navigate)" role="menuitem" :tabindex="item.disabled ? null : '0'">
                                    <span class="p-menuitem-text">{{ item.label }}</span>
                                </a>
                            </li>
                        </template>
                    </ul>
                </div>
            </div>
        </div>
    </ul>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Message from 'primevue/message'

export default defineComponent({
    name: 'kn-admin-menu',
    components: { Message },
    emits: ['click'],
    props: {
        model: Array,
        openedPanelEvent: Object
    },
    data() {
        return {
            searchText: '',
            tmpModel: new Array<any>()
        }
    },
    mounted() {
        this.tmpModel = this.model || []
    },
    updated() {
        console.log('update')
        //@ts-ignore
        if (this.$refs.megamenu) this.$refs.megamenu.style.top = this.openedPanelEvent.target.getBoundingClientRect().top + 'px'
    },
    methods: {
        filter() {
            const modelToFilter = this.model || []
            this.tmpModel = modelToFilter.filter((groupItem: any) => {
                let childItems = groupItem.items.filter((item) => item.label.toLowerCase().includes(this.searchText.toLowerCase()))
                return childItems.length > 0
            })
        },
        focusInput(e) {
            e.stopImmediatePropagation()
        },
        onLeafClick(event, item, navigate) {
            if (item.disabled) {
                event.preventDefault()
                return
            }
            if (item.to && navigate) {
                this.$emit('click', {
                    originalEvent: event,
                    navigate: navigate,
                    item: item
                })
            }
            if (item.command) {
                this.$emit('click', {
                    originalEvent: event,
                    navigate: navigate,
                    item: item
                })
            }
        }
    },
    computed: {}
})
</script>

<style lang="scss" scoped>
.p-megamenu-panel {
    padding: 16px;
    box-shadow: $mainmenu-box-shadow;
    position: absolute;
    z-index: 9;
    top: 0;
    left: 100%;
    background-color: $mainmenu-panel-color;
    transform: translateY(-14px);
    ul {
        list-style: none;
        padding: 0;
    }
    li[role='presentation'] {
        font-weight: light;
        text-transform: uppercase;
        white-space: nowrap;
    }
    li:not([role='presentation']) {
        padding: 2px;
        transition: all 0.5s cubic-bezier(0.075, 0.82, 0.165, 1);
        a {
            text-decoration: none;
            color: $mainmenu-panel-text-color;
            display: inline-block;
            height: 100%;
            width: 100%;
            cursor: pointer;
        }
        &:hover {
            background-color: darken($mainmenu-panel-color, 10%);
        }
        &.searched {
            background-color: yellow;
        }
    }
    .p-input-icon-left > i:first-of-type {
        left: 1.25rem;
    }
    .p-inputtext {
        width: 100%;
        border-radius: 0;
    }
    .p-megamenu-data {
        overflow: hidden;
        display: block;
        column-count: 5;
        column-gap: 8px;
        .menuColumn {
            width: 100%;
            -webkit-column-break-inside: avoid;
            page-break-inside: avoid;
            break-inside: avoid;
        }
    }
}
</style>
