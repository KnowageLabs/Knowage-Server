<template>
	<div class="kn-page">
		<div class="kn-page-content p-grid p-m-0">
			<div class="p-col-4 p-sm-4 p-md-3 p-p-0 kn-page">
				<Toolbar class="kn-toolbar kn-toolbar--primary">
					<template #left>
						{{ $t('modules.workspace.title') }}
					</template>
				</Toolbar>
				<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
				<Tree id="folders-tree" :value="nodes" selectionMode="single" :expandedKeys="expandedKeys" :filter="true" filterMode="lenient" data-test="functionality-tree" class="kn-tree kn-flex p-flex-column foldersTree" v-model:selectionKeys="selectedKeys" @node-select="goto($event)"></Tree>
			</div>
			<div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
				<router-view />
			</div>
		</div>
	</div>
</template>

<script>
	import { defineComponent } from 'vue'
	/* 	import axios from 'axios' */
	import descriptor from './WorkspaceDescriptor.json'
	import ProgressBar from 'primevue/progressbar'
	import Tree from 'primevue/tree'
	import Toolbar from 'primevue/toolbar'
	export default defineComponent({
		name: 'workspace',
		components: { ProgressBar, Toolbar, Tree },
		props: {
			visibility: Boolean
		},
		data() {
			return { descriptor, loading: false, expandedKeys: {}, selectedKeys: null, nodes: [] }
		},
		emits: ['update:visibility'],
		mounted() {
			this.nodes = descriptor.nodes.root
		},
		methods: {
			goto(e) {
				console.log(e)
				this.$router.push('/workspace/data-preparation')
			}
		}
	})
</script>
