<template>
    <template v-if="filteredButtons.length > 2">
        <Button icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain" @click="toggleMenu" />
        <Menu id="buttons_menu" ref="buttons_menu" :model="filteredButtons" :popup="true">
            <template #item="{item}">
                <a class="p-menuitem-link" role="menuitem" tabindex="0" @click="clickedButton($event, item)">
                    <span class="p-menuitem-icon" :class="item.icon"></span>
                    <span class="p-menuitem-text">{{ $t(item.label) }}</span>
                </a>
            </template>
        </Menu>
    </template>
    <template v-else>
        <Button v-for="(button, index) in filteredButtons" :key="index" :icon="button.icon" class="p-button-text p-button-rounded p-button-plain" @click="clickedButton($event, button)" v-tooltip.bottom="$t(button.label)" :data-test="'delete-button-' + index" />
    </template>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import Menu from 'primevue/menu'
import { IKnListBoxOptions, Ibutton } from './KnListBox'

export default defineComponent({
    name: 'kn-list-button-renderer',
    components: { Menu },
    props: {
        buttons: Array as PropType<Array<Ibutton>>,
        selectedItem: Object as PropType<IKnListBoxOptions>
    },
    data() {
        return {
            filteredButtons: [] as Array<Ibutton>
        }
    },
    emits: ['click'],
    mounted() {
        this.filteredButtons = this.buttons!.filter((x) => {
            return !x.condition || this.selectedItem?.[x.condition]
        })
    },
    methods: {
        clickedButton(e, item) {
            e.item = item
            this.$emit('click', e)
        },
        toggleMenu(e) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.buttons_menu.toggle(e)
        }
    }
})
</script>
