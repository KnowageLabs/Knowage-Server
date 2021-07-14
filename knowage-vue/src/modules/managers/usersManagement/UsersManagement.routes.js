let routes = [
  {
    path: '/users-management',
    name: 'users-management',
    component: () =>
      import('@/modules/managers/usersManagement/UsersManagement.vue')
  }
];

export default routes;