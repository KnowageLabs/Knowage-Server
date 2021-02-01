<template>
	<ul v-if="items" role="menu">
        <template v-for="(item,i) of items" :key="i">
            <li role="none">
                <router-link v-if="item.to" :to="item.to" :style="item.style" :class="[item.class, 'p-ripple', {'p-disabled': item.disabled}]" :target="item.target" exact role="menuitem">
                    <i :class="['layout-menuitem-icon', item.icon]"></i>
                </router-link>
                <a v-if="!item.to" :href="item.url||'#'" :style="item.style" :class="[item.class, 'p-ripple', {'p-disabled': item.disabled}]" :target="item.target" role="menuitem">
                    <i :class="['layout-menuitem-icon', item.icon]"></i>
                </a>
            </li>
        </template>
	</ul>
</template>

<script>

export default {
	name: 'AppSubmenu',
	props: {
		items: Array
    }
}
</script>

<style lang="scss" scoped>
    ul[role=menu] {
        margin: 0;
        padding: 0;
        list-style: none;
        li {
            height: 58px;
            display: flex;
            justify-content: center;
            align-items: center;
            a {
                color: white;
            }
        }
    }
</style>
