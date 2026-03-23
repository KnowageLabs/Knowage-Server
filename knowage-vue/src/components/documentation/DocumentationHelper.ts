export function findFoldersWithLabel(items: any[] | undefined, label: string, result: any[] = []) {
  if (!items) return result;
  for (const node of items) {
    if (node.label === label) result.push(node);
    if (node.children) findFoldersWithLabel(node.children, label, result);
  }
  return result;
}

export function findNodesByLabelPath(items: any[] | undefined, path: string[], index = 0): any[] {
  if (!items || !path || path.length === 0) return [];

  const startItems = index === 0 && items[0] && items[0].children ? items[0].children : items;

  const label = path[index];
  const result: any[] = [];

  for (const node of startItems) {
    if (node.label !== label) continue;

    if (index === path.length - 1) {
      result.push(node);
    } else if (node.children) {
      const childMatches = findNodesByLabelPath(node.children, path, index + 1);
      if (childMatches && childMatches.length) result.push(...childMatches);
    }
  }

  return result;
}

export function mapToQTreeNodes(items: any[], level = 1): any[] {
  if (!items) return [];
  return items.map((item) => {
    const id = (crypto as any).randomUUID() as string;
    const isSection = level === 1 || item.header === true;
    return {
      id,
      label: item.label,
      path: item.path,
      level,
      ...(isSection ? { header: "section" } : {}),
      ...(item.content?.length ? { children: mapToQTreeNodes(item.content, level + 1) } : {}),
    };
  });
}
