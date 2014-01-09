package com.norcode.bukkit.metalcore.datastore;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class DirtyableSaveQueue implements Queue<DirtyableConfiguration> {

	LinkedList<DirtyableConfiguration> configs = new LinkedList<DirtyableConfiguration>();
	HashSet<UUID> keys = new HashSet<UUID>();

	@Override
	public int size() {
		return keys.size();
	}

	@Override
	public boolean isEmpty() {
		return keys.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof DirtyableConfiguration) {
			return keys.contains(((DirtyableConfiguration)o).getUniqueId());
		} else if (o instanceof UUID) {
			return keys.contains((UUID) o);
		}
		return false;
	}

	@Override
	public Iterator<DirtyableConfiguration> iterator() {
		final Iterator<DirtyableConfiguration> it = configs.iterator();
		return new Iterator<DirtyableConfiguration>() {
			private DirtyableConfiguration current;
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public DirtyableConfiguration next() {
				current = it.next();
				return current;
			}

			@Override
			public void remove() {
				it.remove();
				keys.remove(current.getUniqueId());
			}
		};
	}

	@Override
	public Object[] toArray() {
		return configs.toArray(new DirtyableConfiguration[0]);
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return configs.toArray(a);
	}

	@Override
	public boolean add(DirtyableConfiguration dirtyableConfiguration) {
		if (keys.contains(dirtyableConfiguration.getUniqueId())) {
			return false;
		}
		keys.add(dirtyableConfiguration.getUniqueId());
		configs.add(dirtyableConfiguration);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		boolean removed = configs.remove(o);
		if (removed) {
			keys.remove(((DirtyableConfiguration) o).getUniqueId());
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return configs.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends DirtyableConfiguration> c) {
		boolean changed = false;
		for (DirtyableConfiguration cfg: c) {
			if (!keys.contains(cfg.getUniqueId())) {
				keys.add(cfg.getUniqueId());
				configs.add(cfg);
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object o: c) {
			if (remove(c)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (configs.retainAll(c)) {
			keys.clear();
			for (DirtyableConfiguration cfg: configs) {
				keys.add(cfg.getUniqueId());
			}
			return true;
		}
		return false;
	}

	@Override
	public void clear() {
		keys.clear();
		configs.clear();
	}

	@Override
	public boolean offer(DirtyableConfiguration dirtyableConfiguration) {
		return add(dirtyableConfiguration);
	}

	@Override
	public DirtyableConfiguration remove() {
		DirtyableConfiguration c = configs.remove();
		keys.remove(c.getUniqueId());
		return c;
	}

	@Override
	public DirtyableConfiguration poll() {
		DirtyableConfiguration c = configs.poll();
		keys.remove(c.getUniqueId());
		return c;
	}

	@Override
	public DirtyableConfiguration element() {
		return configs.element();
	}

	@Override
	public DirtyableConfiguration peek() {
		return configs.peek();
	}

}
