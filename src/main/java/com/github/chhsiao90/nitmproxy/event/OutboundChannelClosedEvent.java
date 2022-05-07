package com.github.chhsiao90.nitmproxy.event;

import com.github.chhsiao90.nitmproxy.ConnectionContext;

public class OutboundChannelClosedEvent {
    private ConnectionContext connectionContext;
    private boolean client;

    public OutboundChannelClosedEvent(ConnectionContext connectionContext, boolean client) {
        this.connectionContext = connectionContext;
    }

    public ConnectionContext getConnectionInfo() {
        return connectionContext;
    }

    public boolean isClient() {
        return client;
    }

    @Override
    public String toString() {
        return String.format("%s : channelClosed", connectionContext);
    }
}
