package com.norcode.bukkit.metalcore;

import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MetalCoreLogger extends Logger {

	public MetalCoreLogger() {
		super(MetalCorePlugin.class.getCanonicalName(), null);
		setParent(Bukkit.getServer().getLogger());
		setLevel(Level.ALL);
	}

	@Override
	public void log(LogRecord logRecord) {
		logRecord.setMessage("[MetalCore] " + logRecord.getMessage());
		super.log(logRecord);
	}
}
