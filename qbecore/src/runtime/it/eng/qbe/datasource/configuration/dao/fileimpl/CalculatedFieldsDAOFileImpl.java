/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.configuration.dao.fileimpl;

import it.eng.qbe.datasource.configuration.dao.DAOException;
import it.eng.qbe.datasource.configuration.dao.ICalculatedFieldsDAO;
import it.eng.qbe.model.structure.ModelCalculatedField;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot.IMappedValuesDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot.MappedValuesPunctualDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot.MappedValuesRangeDescriptor;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class CalculatedFieldsDAOFileImpl implements ICalculatedFieldsDAO {
	
	protected File modelJarFile;

	public static final String CFIELDS_FROM_META_FILE_NAME = "cfields_meta.xml";
	public static final String CFIELDS_FROM_USER_FILE_NAME = "cfields_user.xml";
	
	public final static String ROOT_TAG = "CFIELDS";
	
	public final static String FIELD_TAG = "CFIELD";
	public final static String FIELD_TAG_ENTIY_ATTR = "entity";
	public final static String FIELD_TAG_NAME_ATTR = "name";
	public final static String FIELD_TAG_TYPE_ATTR = "type";
	public final static String FIELD_TAG_NATURE_ATTR = "nature";
	public final static String FIELD_TAG_IN_LINE_ATTR = "isInLine";
	
	public final static String EXPRESSION_TAG = "EXPRESSION";
	public final static String SLOTS_TAG = "SLOTS";
	public final static String SLOT_TAG = "SLOT";
	
	public final static String VALUESET_TAG = "VALUESET";
	public final static String FROM_TAG = "FROM";
	public final static String TO_TAG = "TO";
	public final static String VALUE_TAG = "VALUE";
	
	
	
	
	public static transient Logger logger = Logger.getLogger(CalculatedFieldsDAOFileImpl.class);
	
	public CalculatedFieldsDAOFileImpl(File modelJarFile) {
		this.modelJarFile = modelJarFile;
	}
	
	
	// =============================================================================
	// LOAD
	// =============================================================================
	
	public Map<String, List<ModelCalculatedField>> loadCalculatedFields() {
		File calculatedFieldsFile;
		Map<String, List<ModelCalculatedField>> calculatedFiledsMap;
		
		calculatedFiledsMap = new HashMap<String, List<ModelCalculatedField>>();
		
		calculatedFieldsFile = getMetaCalculatedFieldsFile();
		loadCalculatedFieldsFromFile(calculatedFieldsFile, calculatedFiledsMap);
		
		calculatedFieldsFile = getUserCalculatedFieldsFile();
		loadCalculatedFieldsFromFile(calculatedFieldsFile, calculatedFiledsMap);
		
		return calculatedFiledsMap;
	}
	
	private void loadCalculatedFieldsFromFile(File calculatedFieldsFile, Map<String, List<ModelCalculatedField>> calculatedFiledsMap) {
		
		FileInputStream in;
		SAXReader reader;
		Document document;
		String entity;
		String name;
		String type;
		String nature;
		Boolean inlineCalculatedField;
		String expression;
		ModelCalculatedField calculatedField;
		List calculatedFieldNodes;
		Iterator it;
		Node calculatedFieldNode;
		List<ModelCalculatedField> calculatedFileds;
		
		logger.debug("IN");
		
		in = null;	
		
		try {
		
			logger.debug("Load calculated fields from file [" + calculatedFieldsFile + "]");
			
			document = guardedRead(calculatedFieldsFile);
			
			if(document != null) {
				
				calculatedFieldNodes = document.selectNodes("//" + ROOT_TAG + "/" + FIELD_TAG + "");
				logger.debug("Found [" + calculatedFieldNodes.size() + "] calculated field/s");
				
				it = calculatedFieldNodes.iterator();				
				while (it.hasNext()) {
					calculatedFieldNode = (Node) it.next();
					entity = calculatedFieldNode.valueOf("@" + FIELD_TAG_ENTIY_ATTR);
					name = calculatedFieldNode.valueOf("@" + FIELD_TAG_NAME_ATTR);
					type = calculatedFieldNode.valueOf("@" + FIELD_TAG_TYPE_ATTR);
					nature = calculatedFieldNode.valueOf("@" + FIELD_TAG_NATURE_ATTR);
					inlineCalculatedField = new Boolean(calculatedFieldNode.valueOf("@" + FIELD_TAG_IN_LINE_ATTR));					
					expression = loadExpression(calculatedFieldNode);
					calculatedField = new ModelCalculatedField(name, type, expression, inlineCalculatedField.booleanValue());
					calculatedField.setNature(nature);
					
					// parse slots
					List<ModelCalculatedField.Slot> slots = loadSlots(calculatedFieldNode);
					calculatedField.addSlots(slots);
					if(slots.size() > 0) {
						String defaultSlotValue = loadDefaultSlotValue(calculatedFieldNode);
						calculatedField.setDefaultSlotValue(defaultSlotValue);
					}
					
					
					if(!calculatedFiledsMap.containsKey(entity)) {
						calculatedFiledsMap.put(entity, new ArrayList());
					}
					calculatedFileds = calculatedFiledsMap.get(entity);
					ModelCalculatedField calculatedFieldToRemove = null;
					for(ModelCalculatedField cf : calculatedFileds) {
						if(cf.getName().equals(calculatedField.getName())) {
							calculatedFieldToRemove = cf;
							break;
						}
					}
					
					if(calculatedFieldToRemove != null) {
						boolean removed = calculatedFileds.remove(calculatedFieldToRemove);
						logger.debug("Calculated field [" + calculatedFieldToRemove.getName() + "] already defined. The old version will be replaced with this one");
					}
					calculatedFileds.add(calculatedField);
					
					logger.debug("Calculated filed [" + calculatedField.getName() + "] loaded succesfully");
				}	
			} else {
				logger.debug("File [" + calculatedFieldsFile + "] does not exist. No calculated fields have been loaded.");
			}
		} catch(Throwable t){
			if(t instanceof DAOException) throw (DAOException)t;
			throw new DAOException("An unpredicted error occurred while loading calculated fields on file [" + calculatedFieldsFile + "]", t);
		}finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					throw new DAOException("Impossible to properly close stream to file file [" + calculatedFieldsFile + "]", e);
				}
			}
			logger.debug("OUT");
		}
	}
	
	private String loadExpression(Node calculatedFieldNode) {
		String expression;
		
		expression = null;
		
		Node expressionNode = calculatedFieldNode.selectSingleNode(EXPRESSION_TAG);
		if(expressionNode != null) {
			expression = expressionNode.getStringValue();
		} else { // for back compatibility
			expression = calculatedFieldNode.getStringValue();
		}
		
		return expression;
	}	
	
	private List<ModelCalculatedField.Slot> loadSlots(Node calculatedFieldNode) {
		
		List<ModelCalculatedField.Slot> slots = new ArrayList<ModelCalculatedField.Slot>();
		
		Node slotBlock = calculatedFieldNode.selectSingleNode(SLOTS_TAG);
		if(slotBlock != null) {
			List<Node> slotNodes = slotBlock.selectNodes(SLOT_TAG);
			
			for(Node slotNode : slotNodes) {
				ModelCalculatedField.Slot slot = loadSlot(slotNode);
				slots.add(slot);
			}
		}
		
		return slots;
	}
		
	private String loadDefaultSlotValue(Node calculatedFieldNode) {
			
		String defaultSlotValue = null;
			
		Node slotBlock = calculatedFieldNode.selectSingleNode(SLOTS_TAG);
		if(slotBlock != null) {
			defaultSlotValue = slotBlock.valueOf("@defaultSlotValue");	
		}
			
		return defaultSlotValue;
	}
	
	
	private ModelCalculatedField.Slot loadSlot(Node slotNode) {
		ModelCalculatedField.Slot slot;
		
		String slotValue = slotNode.valueOf("@value");	
		slot = new ModelCalculatedField.Slot(slotValue);
		
		List<Node> mappedValues = slotNode.selectNodes(VALUESET_TAG);
		for(Node mappedValuesNode:  mappedValues) {
			ModelCalculatedField.Slot.IMappedValuesDescriptor descriptor = loadDescriptor(mappedValuesNode);
			slot.addMappedValuesDescriptors(descriptor);
		}
		
		return slot;
	}
	
	private ModelCalculatedField.Slot.IMappedValuesDescriptor loadDescriptor(Node mappedValuesNode) {
		ModelCalculatedField.Slot.IMappedValuesDescriptor descriptor = null;
		
		String descriptorType = mappedValuesNode.valueOf("@type");	
		if(descriptorType.equalsIgnoreCase("range")) {
			descriptor = loadRangeDescriptor(mappedValuesNode);			
		} else if(descriptorType.equalsIgnoreCase("punctual")) {
			descriptor = loadPunctualDescriptor(mappedValuesNode);
		}
		
		return descriptor;
	}
	
	
	private ModelCalculatedField.Slot.MappedValuesPunctualDescriptor loadPunctualDescriptor(Node mappedValuesNode) { 
		ModelCalculatedField.Slot.MappedValuesPunctualDescriptor punctualDescriptor;
		
		punctualDescriptor = new ModelCalculatedField.Slot.MappedValuesPunctualDescriptor();
		List<Node> punctualValueNodes = mappedValuesNode.selectNodes(VALUE_TAG);
		for(Node punctualValueNode : punctualValueNodes) {
			String punctualValue = punctualValueNode.valueOf("@value");
			punctualDescriptor.addValue( punctualValue );
		}
		
		return punctualDescriptor;
	}
	
	private ModelCalculatedField.Slot.MappedValuesRangeDescriptor loadRangeDescriptor(Node mappedValuesNode) { 
		ModelCalculatedField.Slot.MappedValuesRangeDescriptor rangeDescriptor = null;
		
		Node fomrNode = mappedValuesNode.selectSingleNode(FROM_TAG);
		String fromValue = fomrNode.valueOf("@value");
		Node toNode = mappedValuesNode.selectSingleNode(TO_TAG);
		String toValue = toNode.valueOf("@value");
		rangeDescriptor = new ModelCalculatedField.Slot.MappedValuesRangeDescriptor(fromValue, toValue);
		String includeValue = null;
		includeValue = fomrNode.valueOf("@include");
		if(includeValue != null && (includeValue.equalsIgnoreCase("TRUE") || includeValue.equalsIgnoreCase("FALSE"))) {
			rangeDescriptor.setIncludeMinValue(Boolean.parseBoolean(includeValue));
		}
		includeValue = toNode.valueOf("@include");
		if(includeValue != null && (includeValue.equalsIgnoreCase("TRUE") || includeValue.equalsIgnoreCase("FALSE"))) {
			rangeDescriptor.setIncludeMaxValue(Boolean.parseBoolean(includeValue));
		}
		
		return rangeDescriptor;
	}
	
	
	

	// =============================================================================
	// SAVE
	// =============================================================================
	
	public void saveCalculatedFields(Map<String, List<ModelCalculatedField>> calculatedFields) {
		
		File calculatedFieldsFile;
		Iterator it;
		String entityName;
		List fields;
		Document document;
		Element root;
		ModelCalculatedField modelCalculatedField;
		
		logger.debug("IN");
		
		calculatedFieldsFile = null;
		
		try {
			Assert.assertNotNull(calculatedFields, "Input parameter [calculatedFields] cannot be null");
			
			calculatedFieldsFile = getUserCalculatedFieldsFile();
			Assert.assertNotNull(calculatedFieldsFile, "Destination file cannot be null");
			logger.debug("Calculated fields will be saved on file [" + calculatedFieldsFile + "]");
			
			if( !calculatedFieldsFile.getParentFile().exists() ) {
				DAOException e = new DAOException("Destination file folder [" + calculatedFieldsFile.getPath()+ "] does not exist");
				e.addHint("Check if [" + calculatedFieldsFile.getPath()+ "] folder exist on your server filesystem. If not create it.");
				throw e;
			}
			
			if( calculatedFieldsFile.exists() ) {
				logger.warn("File [" + calculatedFieldsFile + "] already exists. New settings will override the old ones.");
			}
			
			document = DocumentHelper.createDocument();
	        root = document.addElement( ROOT_TAG );
	        			
			logger.debug("In the target model there are [" + calculatedFields.keySet() + "] entity/es that contain calculated fields" );
			it = calculatedFields.keySet().iterator();
			while(it.hasNext()) {
				entityName = (String)it.next();
				logger.debug("Serializing [" + calculatedFields.size() + "] calculated fields for entity [" + entityName + "]");
				fields = (List)calculatedFields.get(entityName);
				for(int i = 0; i < fields.size(); i++) {
					modelCalculatedField = (ModelCalculatedField)fields.get(i);
					logger.debug("Serializing calculated field [" + modelCalculatedField.getName() + "] for entity [" + entityName + "]");
					Element fieldElement = root.addElement( FIELD_TAG )
		            	.addAttribute( FIELD_TAG_ENTIY_ATTR, entityName )
		            	.addAttribute( FIELD_TAG_NAME_ATTR, modelCalculatedField.getName() )
		            	.addAttribute( FIELD_TAG_TYPE_ATTR, modelCalculatedField.getType() )
		            	.addAttribute( FIELD_TAG_NATURE_ATTR, modelCalculatedField.getNature() )
		            	.addAttribute( FIELD_TAG_IN_LINE_ATTR, "" + modelCalculatedField.isInLine() );
		            
					
					fieldElement.addElement( EXPRESSION_TAG ).addCDATA( modelCalculatedField.getExpression() );
					
					List<Slot> slots = modelCalculatedField.getSlots();
					if(slots != null && slots.size() > 0) {
						Element slotsElement = fieldElement.addElement( SLOTS_TAG );
						if(modelCalculatedField.getDefaultSlotValue() != null) {
							slotsElement.addAttribute("defaultSlotValue", modelCalculatedField.getDefaultSlotValue());
						}
						
						for(Slot slot : slots) {
							Element slotElement = slotsElement.addElement(SLOT_TAG);
							slotElement.addAttribute("value", slot.getName());
							saveValueSets(slot, slotElement);							
						}
					}
				}
			}
			
			guardedWrite(document, calculatedFieldsFile);

		} catch(Throwable t){
			if(t instanceof DAOException) throw (DAOException)t;
			throw new DAOException("An unpredicetd error occurred while saving calculated fields on file [" + calculatedFieldsFile + "]");
		} finally {
			logger.debug("OUT");
		}
	}
	
	public void saveValueSets(Slot slot, Element slotElement) {
		List<IMappedValuesDescriptor> descriptors = slot.getMappedValuesDescriptors();
		for(IMappedValuesDescriptor descriptor : descriptors) {
			Element valuesetElement = slotElement.addElement(VALUESET_TAG);
			
			if(descriptor instanceof MappedValuesPunctualDescriptor) {
				MappedValuesPunctualDescriptor punctualDescriptor = (MappedValuesPunctualDescriptor)descriptor;
				valuesetElement.addAttribute("type", "punctual");
				Set<String> values = punctualDescriptor.getValues();
				for(String value : values) {
					valuesetElement.addElement(VALUE_TAG).addAttribute("value", value);
				}
			} else if(descriptor instanceof MappedValuesRangeDescriptor) {
				MappedValuesRangeDescriptor rangeDescriptor = (MappedValuesRangeDescriptor)descriptor;
				valuesetElement.addAttribute("type", "range");
				valuesetElement.addElement(FROM_TAG)
					.addAttribute("value", rangeDescriptor.getMinValue())
					.addAttribute("include", "" + rangeDescriptor.isIncludeMinValue());
				valuesetElement.addElement(TO_TAG)
					.addAttribute("value", rangeDescriptor.getMaxValue())
					.addAttribute("include", "" + rangeDescriptor.isIncludeMaxValue());
			} else {
				throw new DAOException("An unpredicetd error occurred while saving valueset of slot [" + slot.getName() + "]");
			}
			
		}
	}
	
	private File getUserCalculatedFieldsFile() {
		File calculatedFieldsFile = null;
		calculatedFieldsFile = new File(modelJarFile.getParentFile(), CFIELDS_FROM_USER_FILE_NAME);
		return calculatedFieldsFile;
	}
	
	private File getMetaCalculatedFieldsFile() {
		File calculatedFieldsFile = null;
		calculatedFieldsFile = new File(modelJarFile.getParentFile(), CFIELDS_FROM_META_FILE_NAME);
		return calculatedFieldsFile;
	}
	
	
	
	// ------------------------------------------------------------------------------------------------------
	// Guarded actions. see -> http://java.sun.com/docs/books/tutorial/essential/concurrency/guardmeth.html
	// ------------------------------------------------------------------------------------------------------
	
	private boolean locked = false;
	private synchronized void getLock() {
		while(locked) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		locked = true;
	}
	
	private synchronized void releaseLock() {
		locked = false;
	    notifyAll();
	}
	
	private Document guardedRead(File file) {
		InputStream in;
		SAXReader reader;
		Document document;
		
		logger.debug("IN");
		
		in = null;
		reader = null;
		
		try {
			
			logger.debug("acquiring lock...");
			getLock();
			logger.debug("Lock acquired");
			
			try {
				if(file.exists()) {
					in = new FileInputStream(file);
				} else {
					ZipEntry zipEntry;
					
					zipEntry = null;
					try {
						JarFile jarFile = new JarFile( modelJarFile );	
						zipEntry = jarFile.getEntry( file.getName() );
						
						if(zipEntry != null) {
							in = jarFile.getInputStream(zipEntry);
						} else {
							return null;
						}
						
					}catch(IOException ioe){
						throw new SpagoBIRuntimeException("Impossible to load properties from file [" + zipEntry + "]");
					}
				}
				
			} catch (FileNotFoundException fnfe) {
				DAOException e = new DAOException("Impossible to load calculated fields from file [" + file.getName() + "]", fnfe);
				e.addHint("Check if [" + file.getPath()+ "] folder exist on your server filesystem. If not create it.");
				throw e;
			}
			Assert.assertNotNull(in, "Input stream cannot be null");				
			
			reader = new SAXReader();
			try {
				document = reader.read(in);
			} catch (DocumentException de) {
				DAOException e = new DAOException("Impossible to parse file [" + file.getName() + "]", de);
				e.addHint("Check if [" + file + "] is a well formed XML file");
				throw e;
			}
			Assert.assertNotNull(document, "Document cannot be null");
		} catch(Throwable t) {
			if(t instanceof DAOException) throw (DAOException)t;
			throw new DAOException("An unpredicetd error occurred while writing on file [" + file + "]");
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					throw new DAOException("Impossible to properly close stream to file [" + file + "]", e);
				}
			}
			logger.debug("releasing lock...");
			releaseLock();
			logger.debug("lock released");
			
			logger.debug("OUT");
		}	
		
		return document;
	}
	private void guardedWrite(Document document, File file) {
		Writer out;
		OutputFormat format;
		XMLWriter writer;
		
		logger.debug("IN");
		
		out = null;
		writer = null;
		
		try {
			
			logger.debug("acquiring lock...");
			getLock();
			logger.debug("Lock acquired");
			
			out = null;
			try {
				out = new FileWriter( file );
			} catch (IOException e) {
				throw new DAOException("Impossible to open file [" + file + "]", e);
			}
			Assert.assertNotNull(out, "Output stream cannot be null");
					
			format = OutputFormat.createPrettyPrint();
			format.setEncoding("ISO-8859-1");
			format.setIndent("    ");
			writer = new XMLWriter(out , format );
	        try {
	        	
				writer.write( document );
				writer.flush();
			} catch (IOException e) {
				throw new DAOException("Impossible to write to file [" + file + "]", e);
			}
		} catch(Throwable t) {
			if(t instanceof DAOException) throw (DAOException)t;
			throw new DAOException("An unpredicetd error occurred while writing on file [" + file + "]");
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					throw new DAOException("Impossible to properly close stream to file file [" + file + "]", e);
				}
			}
			logger.debug("releasing lock...");
			releaseLock();
			logger.debug("lock released");
			
			logger.debug("OUT");
		}
		
	}
	
	
	
}
