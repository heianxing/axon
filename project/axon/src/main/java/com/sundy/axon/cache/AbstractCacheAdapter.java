package com.sundy.axon.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractCacheAdapter<L> implements Cache {

	private final ConcurrentMap<EntryListener, L> registeredAdapters = new ConcurrentHashMap<EntryListener, L>();
	
	protected abstract L createListenerAdapter(EntryListener cacheEntryListener);
	
	public void registerCacheEntryListener(EntryListener entryListener) {
		final L adapter = createListenerAdapter(entryListener);
		if(registeredAdapters.putIfAbsent(entryListener, adapter)==null){
			doRegisterListener(adapter);
		}

	}

	public void unregisterCacheEntryListener(EntryListener entryListener) {
		final L adapter = registeredAdapters.remove(entryListener);
		if(adapter != null){
			doUnRegisterListener(adapter);
		}
	}
	
	public abstract void doRegisterListener(L adapter);

	public abstract void doUnRegisterListener(L adapter);

}
