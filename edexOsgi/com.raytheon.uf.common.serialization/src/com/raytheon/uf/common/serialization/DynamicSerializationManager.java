/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.beans.BeanMap;

import org.geotools.coverage.grid.GridGeometry2D;

import com.raytheon.uf.common.serialization.BuiltInTypeSupport.CalendarSerializer;
import com.raytheon.uf.common.serialization.BuiltInTypeSupport.DateSerializer;
import com.raytheon.uf.common.serialization.BuiltInTypeSupport.TimestampSerializer;
import com.raytheon.uf.common.serialization.adapters.ByteBufferAdapter;
import com.raytheon.uf.common.serialization.adapters.CoordAdapter;
import com.raytheon.uf.common.serialization.adapters.EnumSetAdapter;
import com.raytheon.uf.common.serialization.adapters.FloatBufferAdapter;
import com.raytheon.uf.common.serialization.adapters.GeometryTypeAdapter;
import com.raytheon.uf.common.serialization.adapters.GridGeometry2DAdapter;
import com.raytheon.uf.common.serialization.adapters.JTSEnvelopeAdapter;
import com.raytheon.uf.common.serialization.adapters.PointAdapter;
import com.raytheon.uf.common.serialization.adapters.StackTraceElementAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;
import com.raytheon.uf.common.serialization.thrift.ThriftSerializationContext;
import com.raytheon.uf.common.serialization.thrift.ThriftSerializationContextBuilder;
import com.raytheon.uf.common.util.ByteArrayOutputStreamPool;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Dynamic Serialization Manager provides a serialization capability that runs
 * purely at runtime based on annotations.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date			Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * Aug 13, 2008	#1448		chammack	Initial creation
 * 
 * </pre>
 * 
 * @author chammack
 * @version 1.0
 */
public class DynamicSerializationManager {
	private static Map<SerializationType, DynamicSerializationManager> instanceMap = new HashMap<SerializationType, DynamicSerializationManager>();

	private ISerializationContextBuilder builder;

	public static class SerializationMetadata {
		public List<String> serializedAttributes;

		public ISerializationTypeAdapter<?> serializationFactory;

		public Map<String, ISerializationTypeAdapter<?>> attributesWithFactories;

		public String adapterStructName;

	}

	private static Map<String, SerializationMetadata> serializedAttributes = new ConcurrentHashMap<String, SerializationMetadata>();

	private static final SerializationMetadata NO_METADATA = new SerializationMetadata();

	static {
		SerializationMetadata md = new SerializationMetadata();
		md.serializationFactory = new CalendarSerializer();
		md.adapterStructName = GregorianCalendar.class.getName();
		serializedAttributes.put(GregorianCalendar.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new DateSerializer();
		md.adapterStructName = Date.class.getName();
		serializedAttributes.put(Date.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new TimestampSerializer();
		md.adapterStructName = Timestamp.class.getName();
		serializedAttributes.put(Timestamp.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new BuiltInTypeSupport.SqlDateSerializer();
		md.adapterStructName = java.sql.Date.class.getName();
		serializedAttributes.put(java.sql.Date.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new PointAdapter();
		md.adapterStructName = java.awt.Point.class.getName();
		serializedAttributes.put(java.awt.Point.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new CoordAdapter();
		md.adapterStructName = Coordinate.class.getName();
		serializedAttributes.put(Coordinate.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new BuiltInTypeSupport.BigDecimalSerializer();
		md.adapterStructName = BigDecimal.class.getName();
		serializedAttributes.put(BigDecimal.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new GeometryTypeAdapter();
		md.adapterStructName = Geometry.class.getName();
		serializedAttributes.put(Polygon.class.getName(), md);
		serializedAttributes.put(MultiPolygon.class.getName(), md);
		serializedAttributes.put(Point.class.getName(), md);
		serializedAttributes.put(Geometry.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new JTSEnvelopeAdapter();
		md.adapterStructName = Envelope.class.getName();
		serializedAttributes.put(Envelope.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new GridGeometry2DAdapter();
		md.adapterStructName = GridGeometry2D.class.getName();
		serializedAttributes.put(GridGeometry2D.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new EnumSetAdapter();
		md.adapterStructName = EnumSet.class.getName();
		serializedAttributes.put(EnumSet.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new StackTraceElementAdapter();
		md.adapterStructName = StackTraceElement.class.getName();
		serializedAttributes.put(StackTraceElement.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new FloatBufferAdapter();
		md.adapterStructName = FloatBuffer.class.getName();
		serializedAttributes.put(FloatBuffer.class.getName(), md);
		md = new SerializationMetadata();
		md.serializationFactory = new ByteBufferAdapter();
		md.adapterStructName = ByteBuffer.class.getName();
		serializedAttributes.put(ByteBuffer.class.getName(), md);
	}

	public enum EnclosureType {
		FIELD, COLLECTION
	};

	public static enum SerializationType {
		Thrift
	};

	/**
	 * Serialize an object to a byte array
	 * 
	 * @param obj
	 *            the object
	 * @return a byte array with a serialized version of the object
	 * @throws SerializationException
	 */
	public byte[] serialize(Object obj) throws SerializationException {

		ByteArrayOutputStream baos = ByteArrayOutputStreamPool.getInstance()
				.getStream();

		try {
			serialize(obj, baos);
			return baos.toByteArray();
		} finally {
			if (baos != null) {
				try {
					// return stream to pool
					baos.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	/**
	 * Serialize an object to a byte array
	 * 
	 * @param obj
	 *            the object
	 * @return a byte array with a serialized version of the object
	 * @throws SerializationException
	 */
	public void serialize(Object obj, OutputStream os)
			throws SerializationException {
		ISerializationContext ctx = this.builder.buildSerializationContext(os,
				this);
		ctx.writeMessageStart("dynamicSerialize");
		serialize(ctx, obj);
		ctx.writeMessageEnd();
	}

	/**
	 * Serialize an object using a context
	 * 
	 * This method is not intended to be used by end users.
	 * 
	 * @param ctx
	 *            the serialization context
	 * @param obj
	 *            the object to serialize
	 * @throws SerializationException
	 */
	public void serialize(ISerializationContext ctx, Object obj)
			throws SerializationException {
		BeanMap beanMap = null;

		if (obj != null && !obj.getClass().isArray()) {
			beanMap = SerializationCache.getBeanMap(obj);
		}
		try {
			SerializationMetadata metadata = null;
			if (obj != null) {
				metadata = getSerializationMetadata(obj.getClass().getName());
			}

			((ThriftSerializationContext) ctx).serializeMessage(obj, beanMap,
					metadata);
		} finally {
			if (beanMap != null) {
				SerializationCache.returnBeanMap(beanMap, obj);
			}
		}
	}

	/**
	 * Deserialize an object from a stream
	 * 
	 * @param istream
	 * @return
	 * @throws SerializationException
	 */
	public Object deserialize(InputStream istream)
			throws SerializationException {
		IDeserializationContext ctx = this.builder.buildDeserializationContext(
				istream, this);
		ctx.readMessageStart();
		Object obj = deserialize(ctx);
		ctx.readMessageEnd();
		return obj;

	}

	/**
	 * Deserialize from a context
	 * 
	 * Not intended to be used by end users
	 * 
	 * @param ctx
	 * @return
	 * @throws SerializationException
	 */
	public Object deserialize(IDeserializationContext ctx)
			throws SerializationException {
		return ((ThriftSerializationContext) ctx).deserializeMessage();
	}

	/**
	 * Inspect a class and return the metadata for the object
	 * 
	 * If the class has not been annotated, this will return null
	 * 
	 * The metadata is cached for performance
	 * 
	 * @param c
	 *            the class
	 * @return the metadata
	 */
	@SuppressWarnings("unchecked")
	public static SerializationMetadata inspect(Class<?> c) {

		// Check for base types

		SerializationMetadata attribs = serializedAttributes.get(c.getName());
		if (attribs != null) {
			return attribs;
		}

		attribs = new SerializationMetadata();
		attribs.serializedAttributes = new ArrayList<String>();
		attribs.attributesWithFactories = new HashMap<String, ISerializationTypeAdapter<?>>();

		DynamicSerializeTypeAdapter serializeAdapterTag = c
				.getAnnotation(DynamicSerializeTypeAdapter.class);

		// Check to see if there is an adapter
		if (serializeAdapterTag != null) {
			Class factoryTag = (serializeAdapterTag).factory();
			try {
				attribs.serializationFactory = (ISerializationTypeAdapter) factoryTag
						.newInstance();
				attribs.adapterStructName = c.getName();
			} catch (Exception e) {
				throw new RuntimeException("Factory could not be constructed: "
						+ factoryTag, e);
			}
		}

		// check to see if superclass has an adapter
		if (attribs.serializationFactory == null) {
			Class<?> superClazz = c.getSuperclass();
			while (superClazz != null && attribs.serializationFactory == null) {
				SerializationMetadata superMd = serializedAttributes
						.get(superClazz.getName());
				if (superMd != null && superMd.serializationFactory != null) {
					attribs.serializationFactory = superMd.serializationFactory;
					attribs.adapterStructName = c.getName();
				}
				superClazz = superClazz.getSuperclass();
			}
		}

		// Make sure the object is annotated or has an adapter. If not, return
		// null
		DynamicSerialize serializeTag = c.getAnnotation(DynamicSerialize.class);
		if (serializeTag == null && attribs.serializationFactory == null) {
			return null;
		}

		if (attribs.serializationFactory == null) {
			// Go through the class and find the fields with annotations
			Class<?> clazz = c;
			Set<String> getters = new HashSet<String>();
			Set<String> setters = new HashSet<String>();
			while (clazz != null && clazz != Object.class) {

				// Make sure a getter and setter has been defined, and throw an
				// exception if they haven't been

				getters.clear();
				setters.clear();
				Method[] methods = c.getMethods();
				for (Method m : methods) {
					String name = m.getName();
					if (name.startsWith("get")) {
						name = name.substring(3);
						getters.add(name.toLowerCase());
					} else if (name.startsWith("is")) {
						name = name.substring(2);
						getters.add(name.toLowerCase());
					} else if (name.startsWith("set")) {
						name = name.substring(3);
						setters.add(name.toLowerCase());
					}
				}

				java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
				for (java.lang.reflect.Field field : fields) {

					int modifier = field.getModifiers();
					if (Modifier.isFinal(modifier)) {
						continue;
					}

					DynamicSerializeElement annotation = field
							.getAnnotation(DynamicSerializeElement.class);
					if (annotation != null) {
						String fieldName = field.getName();

						attribs.serializedAttributes.add(field.getName());
						if (serializeAdapterTag == null) {
							serializeAdapterTag = field.getType()
									.getAnnotation(
											DynamicSerializeTypeAdapter.class);
						}
						if (serializeAdapterTag != null) {
							try {
								attribs.attributesWithFactories.put(fieldName,
										serializeAdapterTag.factory()
												.newInstance());
							} catch (Exception e) {
								throw new RuntimeException(
										"Factory could not be instantiated", e);
							}
						}
						// Throw a validation exception if necessary
						boolean foundGetter = false;
						boolean foundSetter = false;
						String lower = fieldName.toLowerCase();

						if (getters.contains(lower)) {
							foundGetter = true;
						}

						if (setters.contains(lower)) {
							foundSetter = true;
						}

						if (!foundGetter || !foundSetter) {
							String missing = "";
							if (!foundGetter && !foundSetter) {
								missing = "Getter and Setter";
							} else if (!foundGetter) {
								missing = "Getter";
							} else if (!foundSetter) {
								missing = "Setter";
							}

							throw new RuntimeException("Required " + missing
									+ " on " + clazz.getName() + ":"
									+ field.getName() + " is missing");
						}

					}
				}
				clazz = clazz.getSuperclass();
			}
		}

		// Sort to guarantee universal ordering
		Collections.sort(attribs.serializedAttributes);
		serializedAttributes.put(c.getName(), attribs);

		// inspect inner classes
		Class<?>[] innerClzs = c.getClasses();
		for (Class<?> innerClz : innerClzs) {
			inspect(innerClz);
		}

		return attribs;
	}

	public static synchronized DynamicSerializationManager getManager(
			SerializationType type) {
		DynamicSerializationManager mgr = instanceMap.get(type);
		if (mgr == null) {
			mgr = new DynamicSerializationManager(type);
			instanceMap.put(type, mgr);
		}

		return mgr;
	}

	private DynamicSerializationManager(SerializationType type) {
		if (type == SerializationType.Thrift) {
			builder = new ThriftSerializationContextBuilder();
		}
	}

	/**
	 * Get the serialization metadata. Build it if not found
	 * 
	 * @param name
	 * @return
	 */
	public SerializationMetadata getSerializationMetadata(String name) {
		// we can't synchronize on this because it's possible the
		// Class.forName() will trigger code that comes back into here and
		// then deadlocks
		SerializationMetadata sm = serializedAttributes.get(name);
		if (sm == null) {
			try {
				sm = inspect(Class.forName(name, true, getClass()
						.getClassLoader()));
				if (sm == null) {
					serializedAttributes.put(name, NO_METADATA);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (sm == NO_METADATA) {
			return null;
		}
		return sm;

	}

}