<template>

    <li role="none" :style="item.style" :title="item.desc || item.label" @mouseenter="toggleSubMenu" @mouseleave="toggleSubMenu" >
        <router-link v-if="item.to && !item.disabled" :to="item.to" custom v-slot="{navigate, href, isActive}" exact>
            <a :href="href" @click="onClick($event, navigate)"  role="menuitem" :class="isActive && 'router-link-active'">
                <span v-if="item.iconCls" :class="['p-menuitem-icon', item.iconCls]"></span>
                <img v-if="item.custIcon" :src="item.custIcon" />
                <span v-if="!item.iconCls && !item.custIcon" class="p-menuitem-icon fas fa-file"></span>
                <span class="p-menuitem-text">{{item.label}}</span>
                <i v-if="item.items" class="pi pi-fw pi-angle-right"></i>
            </a>
        </router-link>
        <a v-else :href="item.url" @click="onClick" :target="item.target" role="menuitem" :tabindex="item.disabled ? null : '0'">
            <span v-if="item.iconCls" :class="['p-menuitem-icon', item.iconCls]"></span>
            <img v-if="item.custIcon" :src="item.custIcon" />
            <span v-if="!item.iconCls && !item.custIcon" class="p-menuitem-icon fas fa-file"></span>
            <span class="p-menuitem-text">{{item.label}}</span>
            <i v-if="item.items" class="pi pi-fw pi-angle-right"></i>
        </a>
        <ul v-if="item.items" v-show="openedLi">
            <template v-for="(subitem, i) of item.items" :key="i">
                <kn-menu-item :item="subitem" @click="itemClick"></kn-menu-item>
            </template>
        </ul>
    </li>

</template>

<script lang="ts">
   import { defineComponent } from 'vue'

    export default defineComponent({
        name: 'kn-menu-item',
        emits: ['click'],
        props: {
            item: null
        },
        data(){
            return {
                openedLi: false
            }
        },
        methods: {
            onClick(event, navigate) : void {
                this.$emit('click', {
                    originalEvent: event,
                    item: this.item,
                    navigate: navigate
                });
            },
            toggleSubMenu() {
                this.openedLi = !this.openedLi
            }
        },
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
        color: $mainmenu-icon-color;
        display: block;
        width: 100%;
        transition: background-color .3s, border-left-color .3s;
        overflow: hidden;
        border-left: 4px solid transparent;
        outline: none;
        cursor: pointer;
        user-select: none;
        .p-menuitem-text, i {
            display: none;
        }
        &:hover {
            background-color: lighten($mainmenu-background-color, 10%);
        }
        &.router-link-active {
            border-left: 3px solid $mainmenu-highlight-color;
        }
        img {
            width: 20px;
            height: 20px;
        }
    }
    & > ul {
        margin:0;
        padding: 0;
        list-style: none;
        box-shadow: $mainmenu-box-shadow;
        background-color: lighten($mainmenu-background-color, 15%);
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
                background-color: $mainmenu-panel-color;
                color: $mainmenu-panel-text-color;
                &:hover {
                    background-color: darken($mainmenu-panel-color,10%);
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
