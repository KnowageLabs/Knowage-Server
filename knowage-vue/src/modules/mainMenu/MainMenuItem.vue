<template>
    <li role="menu" :style="[item.style, getVisibilityClass(item)]" :title="item.descr ? $internationalization($t(item.descr)) : $internationalization($t(item.label))">
        <router-link v-if="item.to && !item.disabled" :to="cleanTo" custom v-slot="{ navigate, isActive }" exact>
            <a @click="onClick($event, navigate)" role="menuitem" :class="isActive && 'router-link-active'">
                <Badge v-if="badge > 0" :value="badge" severity="danger"></Badge>
                <span v-if="item.iconCls" :class="['p-menuitem-icon', item.iconCls]"></span>
                <img v-if="item.custIcon" :src="item.custIcon" />
                <span v-if="!item.iconCls && !item.custIcon" class="p-menuitem-icon fas fa-file"></span>
                <span v-if="item.descr" class="p-menuitem-text">{{ $internationalization($t(item.descr)) }}</span>
                <span v-else class="p-menuitem-text">{{ $internationalization($t(item.label)) }}</span>
                <i v-if="item.items" class="pi pi-fw pi-angle-right"></i>
            </a>
        </router-link>
        <a v-else @click="onClick" :target="item.target" role="menuitem" :tabindex="item.disabled ? null : '0'">
            <Badge v-if="badge > 0" :value="badge" severity="danger"></Badge>
            <span v-if="item.iconCls && item.command != 'languageSelection'" :class="['p-menuitem-icon', item.iconCls]"></span>
            <img v-if="item.custIcon" :src="item.custIcon" />
            <img v-if="item.iconCls && item.command === 'languageSelection'" :src="require('@/assets/images/flags/' + locale.toLowerCase().substring(3, 5) + '.svg')" />
            <span v-if="!item.iconCls && !item.custIcon" class="p-menuitem-icon fas fa-file"></span>
            <span v-if="item.descr" class="p-menuitem-text">{{ $internationalization($t(item.descr)) }}</span>
            <span v-else class="p-menuitem-text">{{ $internationalization($t(item.label)) }}</span>
            <i v-if="item.items" class="pi pi-fw pi-angle-right"></i>
        </a>
    </li>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Badge from 'primevue/badge'
    import { mapState } from 'vuex'

    export default defineComponent({
        name: 'kn-menu-item',
        components: { Badge },
        emits: ['click'],
        props: {
            item: null,
            badge: null
        },
        data() {
            return {
                openedLi: false
            }
        },
        methods: {
            onClick(event, navigate): void {
                this.$emit('click', {
                    originalEvent: event,
                    item: this.item,
                    navigate: navigate
                })
            },
            toggleSubMenu() {
                this.openedLi = !this.openedLi
            },

            getVisibilityClass(item) {
                if (!item.conditionedView) return true

                return !item.visible ? 'display:none' : ''
            }
        },
        computed: {
            ...mapState({
                locale: 'locale'
            }),
            cleanTo(): any {
                return this.item.to.replace(/\\\//g, '/')
            }
        }
    })
</script>

<style lang="scss" scoped>
    li {
        position: relative;
        &:first-child {
            & > ul {
                top: 10px;
            }
        }
        & > a {
            text-decoration: none;
            text-align: center;
            padding: 12px 15px;
            padding-left: 12px;
            color: var(--kn-mainmenu-icon-color);
            display: block;
            width: 100%;
            transition: background-color 0.3s, border-left-color 0.3s;
            overflow: hidden;
            border-left: 4px solid transparent;
            outline: none;
            cursor: pointer;
            user-select: none;
            .p-badge {
                position: absolute;
                top: 0;
                right: 5px;
            }
            .p-menuitem-text,
            i {
                display: none;
            }
            &:hover {
                background-color: var(--kn-mainmenu-hover-background-color);
            }
            &.router-link-active {
                border-left: 3px solid var(--kn-mainmenu-highlight-color);
            }
            img {
                width: 20px;
                height: 20px;
            }
        }
        & > ul {
            margin: 0;
            padding: 0;
            list-style: none;
            box-shadow: var(--kn-mainmenu-box-shadow);
            background-color: var(--kn-mainmenu-hover-background-color);
            position: absolute;
            top: 0;
            left: 100%;
            min-width: 200px;
            max-height: none;
            min-width: 200px;
            max-height: none;
            & > li {
                a {
                    display: inline-flex;
                    align-items: center;
                    padding: 10px 5px 10px 10px;
                    background-color: var(--kn-mainmenu-panel-color);
                    color: var(--kn-mainmenu-panel-text-color);
                    &:hover {
                        background-color: var(--kn-mainmenu-hover-background-color);
                    }
                    .p-menuitem-text {
                        display: inline-block;
                        vertical-align: middle;
                        text-align: left;
                        white-space: nowrap;
                        flex: 1;
                    }
                    .p-menuitem-icon {
                        display: none;
                        vertical-align: middle;
                        margin-right: 10px;
                    }
                    i {
                        display: inline-block;
                    }
                }
            }
        }
    }
</style>
