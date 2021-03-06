
package org.fcrepo.utils.infinispan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.infinispan.container.InternalEntryFactory;
import org.infinispan.container.InternalEntryFactoryImpl;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.loaders.CacheLoaderException;
import org.infinispan.loaders.CacheStore;
import org.modeshape.common.logging.Logger;

public class StoreChunkOutputStream extends OutputStream {

    protected final Logger logger;

    // 1 MB
    public static final int CHUNKSIZE = 1024 * 1024 * 1;

    protected final CacheStore blobCache;

    protected final String keyPrefix;

    private final ByteArrayOutputStream chunkBuffer;

    private boolean closed;

    protected int chunkIndex;

    private final InternalEntryFactory entryFactory =
            new InternalEntryFactoryImpl();

    public StoreChunkOutputStream(final CacheStore blobCache,
            final String keyPrefix) {
        logger = Logger.getLogger(getClass());
        this.blobCache = blobCache;
        this.keyPrefix = keyPrefix;
        chunkBuffer = new ByteArrayOutputStream(1024);
    }

    /**
     * @return Number of chunks stored.
     */
    public int getNumberChunks() {
        return chunkIndex;
    }

    @Override
    public void write(final int b) throws IOException {
        if (chunkBuffer.size() == CHUNKSIZE) {
            storeBufferInBLOBCache();
        }
        chunkBuffer.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len)
            throws IOException {
        if (len + chunkBuffer.size() <= CHUNKSIZE) {
            chunkBuffer.write(b, off, len);
        } else {
            final int storeLength = CHUNKSIZE - chunkBuffer.size();
            write(b, off, storeLength);
            storeBufferInBLOBCache();
            write(b, off + storeLength, len - storeLength);
        }
    }

    @Override
    public void close() throws IOException {
        logger.debug("Close. Buffer size at close: {0}", chunkBuffer.size());
        if (closed) {
            logger.debug("Stream already closed.");
            return;
        }
        closed = true;
        // store last chunk
        if (chunkBuffer.size() > 0) {
            storeBufferInBLOBCache();
        }
    }

    private void storeBufferInBLOBCache() throws IOException {
        final byte[] chunk = chunkBuffer.toByteArray();
        try {
            final String chunkKey = keyPrefix + "-" + chunkIndex;
            final InternalCacheEntry c = blobCache.load(chunkKey);
            final InternalCacheEntry cacheEntry;
            if (c == null) {
                cacheEntry =
                        entryFactory.create(chunkKey, chunk,
                                (EntryVersion) null);
            } else {
                cacheEntry = entryFactory.create(chunkKey, chunk, c);
            }

            logger.debug("Store chunk {0}", chunkKey);
            blobCache.store(cacheEntry);
        } catch (final CacheLoaderException e) {
            throw new IOException(e);
        }

    }

}
