package com.norcode.bukkit.metalcore.datastore;

import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;

public class DirtyableConfiguration extends DirtyableSection implements Configuration {

	private boolean dirty;
	private UUID uuid;

	protected Configuration defaults;
	protected DirtyableConfigurationOptions options;

	protected final DumperOptions yamlOptions = new DumperOptions();
	protected final Representer yamlRepresenter = new YamlRepresenter();
	protected final Yaml yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);

	public DumperOptions getYamlOptions() {
		return yamlOptions;
	}

	public Representer getYamlRepresenter() {
		return yamlRepresenter;
	}

	public Yaml getYaml() {
		return yaml;
	}

	private Datastore datastore;

	public DirtyableConfiguration(Datastore datastore, UUID uuid) {
		this.datastore = datastore;
		this.uuid = uuid;
		this.yamlOptions.setIndent(2);
		this.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
	}

	public void setDirty() {
		this.setDirty(true);

	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		if (dirty) {
			datastore.flagDirty(this);
		}
	}

	public boolean isDirty() {
		return dirty;
	}

	public void addDefault(String path, Object value) {
		Validate.notNull(path, "Path may not be null");

		if (this.defaults == null) {
			this.defaults = new MemoryConfiguration();
		}

		this.defaults.set(path, value);
	}

	public void addDefaults(Map<String, Object> defaults) {
		Validate.notNull(defaults, "Defaults may not be null");

		for (Map.Entry entry : defaults.entrySet())
			addDefault((String) entry.getKey(), entry.getValue());
	}

	public void addDefaults(Configuration defaults) {
		Validate.notNull(defaults, "Defaults may not be null");

		addDefaults(defaults.getValues(true));
	}

	public void setDefaults(Configuration defaults) {
		Validate.notNull(defaults, "Defaults may not be null");

		this.defaults = defaults;
	}

	public Configuration getDefaults() {
		return this.defaults;
	}

	public ConfigurationSection getParent() {
		return null;
	}

	public DirtyableConfigurationOptions options() {
		if (this.options == null) {
			this.options = new DirtyableConfigurationOptions(this);
		}
		return this.options;
	}



	public void load(File file)
			throws FileNotFoundException, IOException, InvalidConfigurationException
	{
		Validate.notNull(file, "File cannot be null");

		load(new FileInputStream(file));
	}

	public void load(InputStream stream)
			throws IOException, InvalidConfigurationException
	{
		Validate.notNull(stream, "Stream cannot be null");

		InputStreamReader reader = new InputStreamReader(stream);
		StringBuilder builder = new StringBuilder();
		BufferedReader input = new BufferedReader(reader);
		try
		{
			String line;
			while ((line = input.readLine()) != null) {
				builder.append(line);
				builder.append((char)'\n');
			}
		} finally {
			input.close();
		}

		loadFromString(builder.toString());
	}

	public void save(File file)
			throws IOException
	{
		Validate.notNull(file, "File cannot be null");

		Files.createParentDirs(file);

		String data = saveToString();

		FileWriter writer = new FileWriter(file);
		try
		{
			writer.write(data);
		} finally {
			writer.close();
		}
	}

	public UUID getUniqueId() {
		return uuid;
	}
}
