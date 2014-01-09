package com.norcode.bukkit.metalcore.datastore;

import org.bukkit.configuration.ConfigurationOptions;

public class DirtyableConfigurationOptions extends ConfigurationOptions {
		protected DirtyableConfigurationOptions(DirtyableConfiguration configuration)
		{
			super(configuration);
		}

		public DirtyableConfiguration configuration()
		{
			return (DirtyableConfiguration)super.configuration();
		}

		public DirtyableConfigurationOptions copyDefaults(boolean value)
		{
			super.copyDefaults(value);
			return this;
		}

		public DirtyableConfigurationOptions pathSeparator(char value)
		{
			super.pathSeparator(value);
			return this;
		}

}
