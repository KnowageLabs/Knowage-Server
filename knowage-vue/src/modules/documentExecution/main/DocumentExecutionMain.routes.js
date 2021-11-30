const routes = [
  {
    path: "/registry/:id",
    name: "document-execution-registry",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
    // children: [
    //     {
    //         path: 'registry/:id',
    //         name: 'document-execution-registry',
    //         component: () => import('@/modules/documentExecution/registry/Registry.vue'),
    //         props: true
    //     }
    // ]
  },
  {
    path: "/document-composite/:id",
    name: "document-execution-document-composite",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
  },
  {
    path: "/report/:id",
    name: "document-execution-report",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
  },
  {
    path: "/office-doc/:id",
    name: "document-execution-office-doc",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
  },
  {
    path: "/olap/:id",
    name: "document-execution-olap",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
  },
  {
    path: "/map/:id",
    name: "document-execution-map",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
  },
  {
    path: "/report/:id",
    name: "document-execution-report",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
  },
  {
    path: "/kpi/:id",
    name: "document-execution-kpi",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
  },
  {
    path: "/dossier/:id",
    name: "document-execution-dossier",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
  },
  {
    path: "/etl/:id",
    name: "document-execution-etl",
    component: () =>
      import("@/modules/documentExecution/main/DocumentExecution.vue"),
    props: true,
  },
];

export default routes
