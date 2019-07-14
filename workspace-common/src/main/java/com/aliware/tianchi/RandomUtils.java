package com.aliware.tianchi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ./ignore 2019-07-12
 */
public class RandomUtils {

    private static final int POOL_SIZE = 100000;

    private static ThreadLocal<List<Integer>> randomPool = ThreadLocal.withInitial(() -> new ArrayList<>(POOL_SIZE));

    private static ThreadLocal<AtomicInteger> poolIndex = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    public static int nextInt(int bound) {
        List<Integer> threadRandomPool = randomPool.get();
        if (threadRandomPool.size() >= POOL_SIZE) {
            return threadRandomPool.get(poolIndex.get().getAndIncrement() % POOL_SIZE) % bound;
        }
        int random = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        threadRandomPool.add(random);
        return random % bound;
    }
}
