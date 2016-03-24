/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.meta.model.olap.impl;

import it.eng.knowage.meta.model.olap.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class OlapModelFactoryImpl extends EFactoryImpl implements OlapModelFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static OlapModelFactory init() {
		try {
			OlapModelFactory theOlapModelFactory = (OlapModelFactory)EPackage.Registry.INSTANCE.getEFactory("http:///it/eng/spagobi/meta/model/olapl.ecore"); 
			if (theOlapModelFactory != null) {
				return theOlapModelFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new OlapModelFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OlapModelFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case OlapModelPackage.OLAP_MODEL: return createOlapModel();
			case OlapModelPackage.CUBE: return createCube();
			case OlapModelPackage.DIMENSION: return createDimension();
			case OlapModelPackage.HIERARCHY: return createHierarchy();
			case OlapModelPackage.LEVEL: return createLevel();
			case OlapModelPackage.MEASURE: return createMeasure();
			case OlapModelPackage.CALCULATED_MEMBER: return createCalculatedMember();
			case OlapModelPackage.NAMED_SET: return createNamedSet();
			case OlapModelPackage.VIRTUAL_CUBE: return createVirtualCube();
			case OlapModelPackage.VIRTUAL_CUBE_DIMENSION: return createVirtualCubeDimension();
			case OlapModelPackage.VIRTUAL_CUBE_MEASURE: return createVirtualCubeMeasure();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OlapModel createOlapModel() {
		OlapModelImpl olapModel = new OlapModelImpl();
		return olapModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cube createCube() {
		CubeImpl cube = new CubeImpl();
		return cube;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Dimension createDimension() {
		DimensionImpl dimension = new DimensionImpl();
		return dimension;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Hierarchy createHierarchy() {
		HierarchyImpl hierarchy = new HierarchyImpl();
		return hierarchy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Level createLevel() {
		LevelImpl level = new LevelImpl();
		return level;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Measure createMeasure() {
		MeasureImpl measure = new MeasureImpl();
		return measure;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CalculatedMember createCalculatedMember() {
		CalculatedMemberImpl calculatedMember = new CalculatedMemberImpl();
		return calculatedMember;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NamedSet createNamedSet() {
		NamedSetImpl namedSet = new NamedSetImpl();
		return namedSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VirtualCube createVirtualCube() {
		VirtualCubeImpl virtualCube = new VirtualCubeImpl();
		return virtualCube;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VirtualCubeDimension createVirtualCubeDimension() {
		VirtualCubeDimensionImpl virtualCubeDimension = new VirtualCubeDimensionImpl();
		return virtualCubeDimension;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VirtualCubeMeasure createVirtualCubeMeasure() {
		VirtualCubeMeasureImpl virtualCubeMeasure = new VirtualCubeMeasureImpl();
		return virtualCubeMeasure;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OlapModelPackage getOlapModelPackage() {
		return (OlapModelPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static OlapModelPackage getPackage() {
		return OlapModelPackage.eINSTANCE;
	}

} //OlapModelFactoryImpl
