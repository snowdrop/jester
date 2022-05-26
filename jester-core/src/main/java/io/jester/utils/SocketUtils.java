package io.jester.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.jester.api.PortResolutionStrategy;
import io.jester.api.Service;

public final class SocketUtils {

    private static final AtomicInteger CURRENT_MIN_PORT = new AtomicInteger(0);
    private static final Random RND = new Random(System.nanoTime());

    private SocketUtils() {

    }

    public static synchronized int findAvailablePort(Service service) {
        if (service.getConfiguration().getPortResolutionStrategy() == PortResolutionStrategy.RANDOM) {
            return findRandomAvailablePort(service);
        }

        return findNextAvailablePort(service);
    }

    public static int findRandomAvailablePort(Service service) {
        int portRange = portRangeMax(service) - portRangeMin(service);
        int candidatePort;
        int searchCounter = 0;
        do {
            if (searchCounter > portRange) {
                throw new IllegalStateException(
                        String.format("Could not find an available port in the range [%d, %d] after %d attempts",
                                portRangeMin(service), portRangeMax(service), searchCounter));
            }

            candidatePort = portRangeMin(service) + RND.nextInt((portRangeMax(service) - portRangeMin(service)) + 1);
            searchCounter++;
        } while (!isPortAvailable(service, candidatePort));

        return candidatePort;
    }

    public static synchronized int findNextAvailablePort(Service service) {
        int portRangeMin = portRangeMin(service);
        int candidate;
        do {
            if (CURRENT_MIN_PORT.get() < portRangeMin) {
                CURRENT_MIN_PORT.set(portRangeMin);
            }

            candidate = CURRENT_MIN_PORT.incrementAndGet();
            if (isPortAvailable(service, candidate)) {
                return candidate;
            }
        } while (candidate <= portRangeMax(service));

        throw new IllegalStateException(String.format("Could not find an available port in the range [%d, %d]",
                portRangeMin, portRangeMax(service)));
    }

    private static boolean isPortAvailable(Service service, int port) {
        if (port < portRangeMin(service) || port > portRangeMax(service)) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
            // do nothing: port not available
        }

        return false;
    }

    private static int portRangeMin(Service service) {
        return service.getConfiguration().getPortRangeMin();
    }

    private static int portRangeMax(Service service) {
        return service.getConfiguration().getPortRangeMax();
    }

}
