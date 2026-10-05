# User & Programmer's Manual
 
 Knowage: User & Programmer's Manual
 
## Table of Contents

  * [CKAN](CKAN/README.md)
  * [Javascript SDK](JS/README.md)
  * [NGSI](NGSI/README.md)

## QBE business domains

In the Business Model Catalogue, open Metaweb and use the **Business Domain**
section to create, rename or delete logical groups of business classes and views.
Each domain has a name, an optional description and at least one assigned entity.
Domain names must be unique within the business model.

Save the domain dialog, then save Metaweb and generate the datamart from the
catalogue. Reopen the QBE to load the generated model. Domain changes configure
the model, not a single query or an already open QBE session.

The QBE displays domains as expandable folders, with a 10 px indentation for each
child level to distinguish domains, entities and fields. Only the row contents
are indented; colored bars stay anchored to the left edge. Entities and fields inside them
retain their normal drag-and-drop, filter and relationship actions; folders
cannot be used as query fields. Entities without a domain remain at the root.
If an entity belongs to multiple domains, it appears in the first domain in
model order. Empty domains are omitted from the QBE tree, including domains
whose entities are not visible to the current user.

Generated model JARs include `groups.json`. Models without this resource retain
the original flat tree. Invalid group metadata is reported as an error rather
than silently discarding the configured domains.
