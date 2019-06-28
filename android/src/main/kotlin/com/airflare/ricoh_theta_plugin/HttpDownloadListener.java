package com.airflare.ricoh_theta_plugin;

/**
 * HTTP communication download listener class
 */
public interface HttpDownloadListener {
    /**
     * Total byte count
     */
    void onTotalSize(long totalSize);
    /**
     * Received byte count
     */
    void onDataReceived(int size);
}
