package com.csd.client;

public class ClientAPIFactory {
    public enum ClientType {
        NETWORK,
        LOCAL,
        MOCK
    }

    public static ClientAPI createClient(ClientType type, String serverAddress) {
        switch (type) {
            case NETWORK: return new NetworkClient(serverAddress);
            default:
                break;
        };

        return null;
    }
}