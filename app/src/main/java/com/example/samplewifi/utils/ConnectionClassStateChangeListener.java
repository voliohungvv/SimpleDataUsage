package com.example.samplewifi.utils;

import com.facebook.network.connectionclass.ConnectionQuality;

public interface ConnectionClassStateChangeListener {
  public void onBandwidthStateChange(ConnectionQuality bandwidthState);
}