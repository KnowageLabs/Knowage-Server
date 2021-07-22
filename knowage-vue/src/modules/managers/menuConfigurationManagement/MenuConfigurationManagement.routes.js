let routes = [
  {
    path: '/menu-configuration',
    name: 'menu-configuration',
    component: () =>
      import('@/modules/managers/menuConfigurationManagement/MenuConfiguration.vue')
  }
];

export default routes;