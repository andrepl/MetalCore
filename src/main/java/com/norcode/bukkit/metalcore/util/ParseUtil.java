package com.norcode.bukkit.metalcore.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtil {

	public static Pattern TIMEDELTA_PATTERN = Pattern.compile("(\\d+)\\s?(d|h|m|s|ms)", Pattern.CASE_INSENSITIVE);

	public static long timeDeltaToMillis(String s) {
		Matcher m = TIMEDELTA_PATTERN.matcher(s);
		long millis = 0;
		TimeUnit unit;
		while (m.find()) {
			if (m.group(2).toLowerCase().equals("d")) {
				unit = TimeUnit.DAYS;
			} else if (m.group(2).toLowerCase().equals("h")) {
				unit = TimeUnit.HOURS;
			} else if (m.group(2).toLowerCase().equals("m")) {
				unit = TimeUnit.MINUTES;
			} else if (m.group(2).toLowerCase().equals("s")) {
				unit = TimeUnit.SECONDS;
			} else if (m.group(2).toLowerCase().equals("ms")) {
				unit = TimeUnit.MILLISECONDS;
			} else {
				continue;
			}
			millis += unit.toMillis(Long.parseLong(m.group(1)));
		}
		return millis;
	}

	public static String millisToTimeDelta(long millis) {
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
		millis -= TimeUnit.SECONDS.toMillis(seconds);
		String out = "";
		if (days > 0) {
			out += days + "d ";
		}
		if (hours > 0) {
			out += hours + "h ";
		}
		if (minutes > 0) {
			out += minutes + "m ";
		}
		if (seconds > 0) {
			out += seconds + "s ";
		}
		if (millis > 0) {
			out += millis + "ms";
		}
		return out.trim();
	}

	public static String locationToString(Location l, boolean includeYawPitch) {
		String out = l.getWorld().getName() + ";";
		out += l.getX() + ";" + l.getY() + ";" + l.getZ();
		if (includeYawPitch) {
			out += ";" + l.getYaw() + ";" + l.getPitch();
		}
		return out;
	}

	public static String BlockLocationToString(Location l, boolean includeYawPitch) {
		String out = l.getWorld().getName() + ";";
		out += l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ();
		if (includeYawPitch) {
			out += ";" + l.getYaw() + ";" + l.getPitch();
		}
		return out;
	}

	public static Location parseLocation(String locationString) {
		String[] parts = locationString.split(";");
		World w = Bukkit.getServer().getWorld(parts[0]);
		try {
			if (parts.length == 6) {
				return new Location(w,
						Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
						Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
			} else {
				return new Location(w, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
			}
		} catch (IllegalArgumentException ex) {
			return null;
		} catch (ArrayIndexOutOfBoundsException ex) {
			return null;
		}
	}


}
