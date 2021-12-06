<template>
    <template v-if="buttons.length > 2">
        <Button icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain" @click="toggleMenu" />
        <Menu id="buttons_menu" ref="buttons_menu" :model="buttons" :popup="true">
            <template #item="{item}">
                <a class="p-menuitem-link" role="menuitem" tabindex="0" @click="clickedButton($event, item)">
                    <span class="p-menuitem-icon" :class="item.icon"></span>
                    <span class="p-menuitem-text">{{ $t(item.label) }}</span>
                </a>
            </template>
        </Menu>
    </template>
    <template v-else>
        <Button v-for="(button, index) in buttons" :key="index" :icon="button.icon" class="p-button-text p-button-rounded p-button-plain" @click="clickedButton($event, button)" v-tooltip.bottom="$t(button.label)" :data-test="'delete-button-' + index" />
    </template>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Menu from 'primevue/menu'

export default defineComponent({
    name: 'kn-list-button-renderer',
    components: { Menu },
    props: {
        buttons: Array
    },
    emits: ['click'],
    created() {},
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
