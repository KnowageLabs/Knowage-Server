const routes = [
  {
    path: '/profile-attributes-management',
    name: 'profile-attributes-management',
    component: () =>
      import('@/modules/managers/profileAttributesManagement/ProfileAttributesManagement.vue')
  }
];

export default routes;