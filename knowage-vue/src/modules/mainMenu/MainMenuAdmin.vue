<template>
	<li role="none" @click="toggleAdminMenu">
		<span :class="['p-menuitem-icon', 'fas fa-cog']"></span>
		<ul class="p-megamenu-panel" v-if="model" v-show="openedPanel">
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
	</li>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Message from 'primevue/message'

	export default defineComponent({
		name: 'kn-admin-menu',
		components: { Message },
		emits: ['click'],
		props: {
			model: Array
		},
		data() {
			return {
				openedPanel: false,
				searchText: '',
				tmpModel: new Array<any>()
			}
		},
		mounted() {
			this.tmpModel = this.model || []
		},
		methods: {
			toggleAdminMenu() {
				this.openedPanel = !this.openedPanel
			},
			filter() {
				const modelToFilter = JSON.parse(JSON.stringify(this.model)) || []

				this.tmpModel = modelToFilter.filter((groupItem: any) => {
					let childItems = groupItem.items.filter((item) =>
						this.$t(item.label)
							.toLowerCase()
							.includes(this.searchText.toLowerCase())
					)
					groupItem.items = childItems
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
	li {
		position: relative;
		& > span {
			text-decoration: none;
			text-align: center;
			padding: 15px;
			padding-left: 12px;
			color: $mainmenu-icon-color;
			display: block;
			width: 100%;
			transition: background-color 0.3s, border-left-color 0.3s;
			overflow: hidden;
			border-left: 4px solid transparent;
			outline: none;
			cursor: pointer;
			user-select: none;
			&:hover {
				background-color: lighten($mainmenu-background-color, 10%);
			}
			&.router-link-active {
				border-left: 3px solid $mainmenu-highlight-color;
			}
		}
		.p-megamenu-panel {
			padding: 16px;
			box-shadow: $mainmenu-box-shadow;
			position: absolute;
			z-index: 9;
			top: 0;
			left: 100%;
			background-color: $mainmenu-panel-color;
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
	}
</style>
