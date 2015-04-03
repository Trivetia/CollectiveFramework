package com.austinv11.collectiveframework.minecraft.config;

import com.austinv11.collectiveframework.minecraft.asm.EarlyTransformer;
import com.austinv11.collectiveframework.utils.ArrayUtils;
import com.austinv11.collectiveframework.utils.ReflectionUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A registry for configs
 */
public class ConfigRegistry {
	
	private static List<ConfigProxy> earlyConfigs = new ArrayList<ConfigProxy>();
	private static List<ConfigProxy> standardConfigs = new ArrayList<ConfigProxy>();
	
	private static List<ConfigProxy> configs = new ArrayList<ConfigProxy>();
	private static List<IConfigProxy> proxies = new ArrayList<IConfigProxy>();
	
	static {
		registerConfigProxy(new DefaultProxy());
	}
	
	/**
	 * Registers a config with the registry
	 * @param config The config to register
	 * @throws ConfigException
	 */
	public static void registerConfig(Object config) throws ConfigException {
		if (!config.getClass().isAnnotationPresent(Config.class))
			throw new ConfigException("Config "+config.toString()+" does not contain a Config annotation!");
		Config configAnnotation = config.getClass().getAnnotation(Config.class);
		try {
			ConfigProxy configProxy = new ConfigProxy(configAnnotation, config);
			
			if (configAnnotation.earlyInit())
				earlyConfigs.add(configProxy);
			else
				standardConfigs.add(configProxy);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigException(e.getMessage());
		}
	}
	
	/**
	 * Registers a config proxy
	 * @param proxy The config proxy
	 */
	public static void registerConfigProxy(IConfigProxy proxy) {
		proxies.add(proxy);
	}
	
	/**
	 * Only meant for internal use
	 */
	public static void earlyInit() {
		for (ConfigProxy config : earlyConfigs)
			initialize(config);
		earlyConfigs.clear();
	}
	
	/**
	 * Only meant for internal use
	 */
	public static void init() {
		for (String s : EarlyTransformer.configClasses)
			try {
				registerConfig(Class.forName(s).newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		for (ConfigProxy config : standardConfigs)
			initialize(config);
		standardConfigs.clear();
	}
	
	private static void initialize(ConfigProxy configProxy) {
		IConfigurationHandler handler = configProxy.handler;
		handler.loadFile(configProxy.fileName, configProxy.config, configProxy.fields);
		configs.add(configProxy);
	}
	
	/**
	 * Gets the key for the passed object
	 * @param o The object
	 * @return The key
	 */
	public static String getKey(Object o) {
		for (IConfigProxy proxy : proxies)
			if (proxy.canSerializeObject(o))
				return proxy.getKey(o);
		return "@NULL@";
	}
	
	/**
	 * Serializes the passed object into a string
	 * @param o The object to serialize
	 * @return The serialized string
	 * @throws ConfigException
	 */
	public static String serialize(Object o) throws ConfigException {
		for (IConfigProxy proxy : proxies)
			if (proxy.canSerializeObject(o))
				return proxy.serialize(o);
		return "@NULL@";
	}
	
	/**
	 * Deserializes the passed string with the passed key
	 * @param key The key representing the object type
	 * @param string The serialized string
	 * @return The deserialized object
	 * @throws ConfigException
	 */
	public static Object deserialize(String key, String string) throws ConfigException {
		for (IConfigProxy proxy : proxies)
			if (proxy.isKeyUsable(key))
				return proxy.deserialize(key, string);
		return null;
	}
	
	/**
	 * The default {@link com.austinv11.collectiveframework.minecraft.config.IConfigurationHandler} for configs
	 */
	public static class DefaultConfigurationHandler implements IConfigurationHandler {
		
		private HashMap<String, HashMap<String, Field>> current = new HashMap<String,HashMap<String, Field>>();
		
		private File cachedFile;
		
		@Override
		public void setValue(String configValue, String category, Object value, Object config) {
			HashMap<String, Field> fields = current.containsKey(category) ? current.get(category) : new HashMap<String,Field>();
			Field field = fields.containsKey(configValue) ? fields.get(configValue) : ReflectionUtils.getDeclaredOrNormalField(configValue, config.getClass());
			try {
				field.set(config, value);
			} catch (IllegalAccessException e) {
				e.printStackTrace(); //This should never be reached
			}
			try {
				writeFile(cachedFile, config);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!current.containsKey(category) || !fields.containsKey(configValue)) {
				fields.put(configValue, field);
				current.put(category, fields);
			}
		}
		
		@Override
		public Object getValue(String configValue, String category, Object config) {
			if (hasValue(configValue, category)) {
				if (current.containsKey(category) && current.get(category).containsKey(configValue))
					return current.get(category).get(configValue);
			}
			return null;
		}
		
		@Override
		public void loadFile(String fileName, Object config, HashMap<String, HashMap<String, Field>> hint) {
			current = hint;
			cachedFile = new File("./config/"+fileName);
			if (cachedFile.exists())
				try {
					readFile(cachedFile, config);
				} catch (Exception e) {
					e.printStackTrace();
				}
			else {
				try {
					cachedFile.createNewFile();
					writeFile(cachedFile, config);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public File getConfigFile(String fileName, Object config) {
			return cachedFile;
		}
		
		@Override
		public boolean hasValue(String configValue, String category) {
			return current.containsKey(category) && current.get(category).containsKey(configValue);
		}
		
		private void writeFile(File file, Object config) throws IOException, IllegalAccessException, ConfigException {
			PrintStream writer = new PrintStream(file);
			for (String category : current.keySet()) {
				writer.println(category + " {");
				for (String field : current.get(category).keySet()) {
					Field f = current.get(category).get(field);
					f.setAccessible(true);
					String comment = f.isAnnotationPresent(Description.class) ? f.getAnnotation(Description.class).comment() : "None! Tell the mod author to include a comment!";
					writer.println("\t"+comment);
					writer.println("\t"+getKey(f.get(config))+":"+field+"="+serialize(f.get(config)));
					writer.println();
				}
				writer.println("}");
			}
			writer.flush();
			writer.close();
		}
		
		private void readFile(File file, Object config) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			boolean reachedBracket = false;
			int lineCount = 0;
			while ((line = reader.readLine()) != null) {
				if (!reachedBracket && line.contains("{")) {
					reachedBracket = true;
					continue;
				}
				if (reachedBracket && line.equals("}")) {
					reachedBracket = false;
					continue;
				}
				if (reachedBracket) {
					if (lineCount < 1) {
						lineCount++;
					} else if (lineCount == 1) {
						String field = line.substring(line.indexOf(":")+1, line.indexOf("="));
						String key = line.substring(0, line.indexOf(":")).replace("\t", "");
						line = line.substring(line.indexOf("=")+1);
						Field f = ReflectionUtils.getDeclaredOrNormalField(field, config.getClass());
						if (f != null) {
							try {
								f.set(config, deserialize(key, line));
							} catch (ConfigException e) {
								e.printStackTrace();
							}
						}
						lineCount++;
					} else {
						lineCount = 0;
					}
				}
			}
			try {
				writeFile(file, config);
			} catch (ConfigException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class ConfigProxy {
		
		public Object config;
		public IConfigurationHandler handler;
		public String fileName;
		public HashMap<String, HashMap<String, Field>> fields = new HashMap<String, HashMap<String, Field>>();
		
		public ConfigProxy(Config annotation, Object config) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
			this.config = config;
			this.handler = (IConfigurationHandler) Class.forName(annotation.handler()).newInstance();
			this.fileName = annotation.fileName().equals("@NULL@") ? config.getClass().getSimpleName()+".cfg" : annotation.fileName();
			
			Field[] declared = config.getClass().getDeclaredFields();
			for (Field f : declared) {
				f.setAccessible(true);
				if (ArrayUtils.indexOf(annotation.exclude(), f.getName()) == -1) {
					if (f.isAnnotationPresent(Description.class))
						addToCategory(f.getAnnotation(Description.class).category(), f);
					else
						addToCategory("General", f);
					}
			}
			
			Field[] field = config.getClass().getFields();
			for (Field f : field) {
				f.setAccessible(true);
				if (ArrayUtils.indexOf(annotation.exclude(), f.getName()) == -1)
					if (f.isAnnotationPresent(Description.class))
						addToCategory(f.getAnnotation(Description.class).category(), f);
					else
						addToCategory("General", f);
			}
		}
		
		private void addToCategory(String category, Field f) {
			HashMap<String, Field> vals;
			if (fields.containsKey(category))
				vals = fields.get(category);
			else
				vals = new HashMap<String,Field>();
			vals.put(f.getName(), f);
			fields.put(category, vals);
		}
	}
}