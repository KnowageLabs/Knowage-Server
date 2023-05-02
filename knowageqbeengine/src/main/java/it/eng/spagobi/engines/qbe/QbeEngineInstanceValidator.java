package it.eng.spagobi.engines.qbe;

import java.util.List;

import it.eng.qbe.datasource.IDataSource;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.AuditColumnType;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.Column;
import it.eng.spagobi.engines.qbe.template.QbeTemplate;
import it.eng.spagobi.engines.qbe.template.QbeTemplateValidationException;

public class QbeEngineInstanceValidator {

	static void validateTemplate(QbeEngineInstance qbeEngineInstance) {

		QbeTemplate template = qbeEngineInstance.getTemplate();

		if (template.hasRegistryConfiguration()) {
			IDataSource dataSource = qbeEngineInstance.getDataSource();
			RegistryConfiguration registryConfiguration = qbeEngineInstance.getRegistryConfiguration();
			String keyColumn = dataSource.getPersistenceManager().getKeyColumn(registryConfiguration);

			List<Column> columns = registryConfiguration.getColumns();
			for (Column column : columns) {
				validateKeyColumn(keyColumn, column);

				validateDefaultValue(column);
			}

			validateAuditColumns(registryConfiguration);
		}
	}

	private static void validateAuditColumns(RegistryConfiguration registryConfiguration) {
		checkIfAuditColumnIsEditable(registryConfiguration, AuditColumnType.USER_INSERT);
		checkIfAuditColumnIsEditable(registryConfiguration, AuditColumnType.USER_UPDATE);
		checkIfAuditColumnIsEditable(registryConfiguration, AuditColumnType.USER_DELETE);
		checkIfAuditColumnIsEditable(registryConfiguration, AuditColumnType.TIME_INSERT);
		checkIfAuditColumnIsEditable(registryConfiguration, AuditColumnType.TIME_UPDATE);
		checkIfAuditColumnIsEditable(registryConfiguration, AuditColumnType.TIME_DELETE);

		checkIfAuditColumnIsRepeated(registryConfiguration, AuditColumnType.USER_INSERT);
		checkIfAuditColumnIsRepeated(registryConfiguration, AuditColumnType.USER_UPDATE);
		checkIfAuditColumnIsRepeated(registryConfiguration, AuditColumnType.USER_DELETE);
		checkIfAuditColumnIsRepeated(registryConfiguration, AuditColumnType.TIME_INSERT);
		checkIfAuditColumnIsRepeated(registryConfiguration, AuditColumnType.TIME_UPDATE);
		checkIfAuditColumnIsRepeated(registryConfiguration, AuditColumnType.TIME_DELETE);
		checkIfAuditColumnIsRepeated(registryConfiguration, AuditColumnType.IS_DELETED);
	}

	protected static void checkIfAuditColumnIsEditable(RegistryConfiguration registryConfiguration, AuditColumnType type) {
		registryConfiguration.getAuditColumn(type).ifPresent(column -> {
			if (column.isVisible() && column.isEditable()) {
				throw new QbeTemplateValidationException(String.format("Audit column [%s] cannot be visible and editable.", type.name()));
			}
		});
	}

	protected static void checkIfAuditColumnIsRepeated(RegistryConfiguration registryConfiguration, AuditColumnType type) {
		if (registryConfiguration.getColumns().stream().filter(column -> column.isAudit() && column.getAuditColumnType().equals(type)).count() > 1) {
			throw new QbeTemplateValidationException(String.format("More than one audit column type [%s] were found. Only one is permitted.", type.name()));
		}
	}

	private static void validateDefaultValue(Column column) {
		boolean hasDefaultValue = column.getDefaultValue() != null;
		if (hasDefaultValue) {
			boolean hasSubEntity = column.getSubEntity() != null && !column.getSubEntity().isEmpty();
			boolean hasForeignKey = column.getForeignKey() != null && !column.getForeignKey().isEmpty();
			if (hasForeignKey || hasSubEntity) {
				throw new QbeTemplateValidationException(String.format("Attribute defaultValue is not allowed for filed [%s]", column.getField()));
			}

//			DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
//			DateTime dateTime = parser.parseDateTime(String.valueOf(column.getDefaultValue()));
//			if (dateTime == null)
//				throw new QbeTemplateValidationException(String.format(
//						"defaultValue attribute [%] is not valid for the column [%s]. Please modify the template changing its value to be ISO8601 compliant (yyyy-MM-dd'T'HH:mm:ssZ).",
//						column.getDefaultValue(), column.getField()));
		}

	}

	private static void validateKeyColumn(String keyColumn, Column column) {
		if (keyColumn.equals(column.getField()) && column.isEditable()) {
			throw new QbeTemplateValidationException(String.format(
					"Primary Key Column [%s] cannot be editable. Please modify the template adding editable=\"false\" to [%s] column configuration.", keyColumn,
					keyColumn));
		}

	}
}
