package com.norcode.bukkit.metalcore.datastore;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Map;

public class DirtyableSection extends MemorySection {

	protected DirtyableSection()
	{
		super();
	}

	public DirtyableSection(ConfigurationSection parent, String path) {
		super(parent, path);
	}

	public String saveToString() {
		DirtyableConfiguration root = ((DirtyableConfiguration) this.getRoot());
		String header = "";
		String dump = root.getYaml().dump(getValues(false));

		if (dump.equals("{}\n")) {
			dump = "";
		}
		return header + dump;
	}


	public void loadFromString(String contents) throws InvalidConfigurationException {
		Map input;
		try {
			input = (Map) ((DirtyableConfiguration) this.getRoot()).getYaml().load(contents);
		} catch (YAMLException e) {
			throw new InvalidConfigurationException(e);
		} catch (ClassCastException e) {
			throw new InvalidConfigurationException("Top level is not a Map.");
		}
		String header = parseHeader(contents);
		if (input != null) {
			convertMapsToSections(input, this);
		}
	}

	protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
		for (Map.Entry entry : input.entrySet()) {
			String key = entry.getKey().toString();
			Object value = entry.getValue();

			if ((value instanceof Map)) {
				convertMapsToSections((Map) value, section.createSection(key));
			} else {
				section.set(key, value);
			}
		}
	}

	protected String parseHeader(String input) {
		String[] lines = input.split("\r?\n", -1);
		StringBuilder result = new StringBuilder();
		boolean readingHeader = true;
		boolean foundHeader = false;

		for (int i = 0; (i < lines.length) && (readingHeader); i++) {
			String line = lines[i];

			if (line.startsWith("# ")) {
				if (i > 0) {
					result.append("\n");
				}

				if (line.length() > "# ".length()) {
					result.append(line.substring("# ".length()));
				}

				foundHeader = true;
			} else if ((foundHeader) && (line.length() == 0)) {
				result.append("\n");
			} else if (foundHeader) {
				readingHeader = false;
			}
		}
		return result.toString();
	}
	public ConfigurationSection createSection(String path) {
		Validate.notEmpty(path, "Cannot create section at empty path");
		Configuration root = getRoot();
		if (root == null) {
			throw new IllegalStateException("Cannot create section without a root");
		}

		char separator = root.options().pathSeparator();

		int i1 = -1;
		ConfigurationSection section = this;
		int i2;
		while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
			String node = path.substring(i2, i1);
			ConfigurationSection subSection = section.getConfigurationSection(node);
			if (subSection == null)
				section = section.createSection(node);
			else {
				section = subSection;
			}
		}

		String key = path.substring(i2);
		if (section == this) {
			ConfigurationSection result = new DirtyableSection(this, key);
			this.map.put(key, result);
			return result;
		}
		return section.createSection(key);
	}

	public void setDirty() {
		DirtyableConfiguration root = (DirtyableConfiguration) getParent();
		if (root != null) {
			root.setDirty();
		}
	}
}
