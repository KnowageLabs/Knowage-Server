<template >
    <li role="none" @click="toggleAdminMenu">
        <span :class="['p-menuitem-icon', 'fas fa-cog']"></span>
        <ul class="p-megamenu-panel" v-if="model" v-show="openedPanel">
            <div class="p-grid p-mb-1">
                <span class="p-input-icon-left p-col">
                    <i class="pi pi-search" />
                    <InputText type="text" v-model="searchText" :placeholder="$tc('common.search')"  @click="focusInput($event)"/>
                </span>
            </div>
            <div class="p-grid">
                <div v-for="(column,columnIndex) of model" :key="column.label + '_column_' + columnIndex" :class="getColumnClassName(model)">
                    <ul class="p-megamenu-submenu">
                        <li role="presentation">{{column.label}}</li>
                        <template v-for="(item, i) of column.items" :key="item.label + i.toString()">
                            <li role="none" :style="item.style" :class="searched(item.label)">
                                <router-link v-if="item.to && !item.disabled" :to="item.to" custom v-slot="{navigate, href}">
                                    <a :href="href" role="menuitem"  @click="onLeafClick($event, item, navigate)">
                                        a<span class="p-menuitem-text">{{item.label}}</span>
                                    </a>
                                </router-link>
                                <a v-else :href="item.url" :target="item.target"  role="menuitem" :tabindex="item.disabled ? null : '0'">
                                    b<span class="p-menuitem-text">{{item.label}}</span>
                                </a>
                            </li>
                        </template>
                    </ul>
                </div>
            </div>
        </ul>
    </li>
    
</template>

<script lang="ts">
 import { defineComponent } from 'vue'

    export default defineComponent({
        name: 'kn-admin-menu',
        emits: ['click'],
        props: {
            model: Array
        },
        data() {
            return {
                openedPanel: false,
                searchText: ''
            }
        },
        methods: {
            toggleAdminMenu(){
                this.openedPanel = !this.openedPanel
            },
            focusInput(e){
                e.stopImmediatePropagation()
            },
            getColumnClassName(model){
                let length = model ? model.length: 0;
                let columnClass;
                switch(length) {
                    case 2:
                        columnClass= 'p-col-6';
                    break;
                    case 3:
                        columnClass= 'p-col-4';
                    break;
                    case 4:
                        columnClass= 'p-col-3';
                    break;
                    case 5:
                        columnClass= 'p-col-4';
                    break;
                    case 6:
                        columnClass= 'p-col-4';
                    break;
                    case 7:
                        columnClass= 'p-col-3';
                    break;
                    case 8:
                        columnClass= 'p-col-3';
                    break;
                    default:
                        columnClass= 'p-col-12';
                    break;
                }
                return columnClass;
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
                    });
                }
            },
            searched(label){
                return this.searchText!== '' && label.toLowerCase().includes(this.searchText) ? 'searched' : ''
            }
        },
        computed: {
        }
    })
</script>

<style lang="scss" scoped>
li {
    position: relative;
    & > span {
        text-decoration: none;
        text-align: center;
        padding: 15px;
        padding-left: 12px;
        color: white;
        display: block;
        width: 100%;
        transition: background-color .3s, border-left-color .3s;
        overflow: hidden;
        border-left: 4px solid transparent;
        outline: none;
        cursor: pointer;
        user-select: none;
        &:hover {
            background-color: lighten(#43749E, 10%);
        }
        &.router-link-active {
            border-left: 3px solid #CF0854;
        }
    }
    .p-megamenu-panel {
        padding: 16px;
        box-shadow: 0 4px 8px rgb(72, 72, 72);
        position: absolute;
        z-index: 9;
        top: 0;
        left: 100%;
        background-color: #f0f8ff;
        min-width: 900px;
        min-height: 200px;
        ul {
            list-style: none;
            padding:0;
        }
        li[role='presentation']{
            font-weight: light;
            text-transform: uppercase;
            white-space: nowrap;
        }
        li:not([role='presentation']){
            padding: 2px;
            transition: all .5s cubic-bezier(0.075, 0.82, 0.165, 1);
            a {
                text-decoration: none;
                color: #43749e;
            }
            &:hover {
                background-color: darken(#f0f8ff,10%);
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
        
    }
}
</style>
