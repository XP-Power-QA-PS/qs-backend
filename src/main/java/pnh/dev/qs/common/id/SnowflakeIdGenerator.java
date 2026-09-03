package pnh.dev.qs.common.id;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;

import java.lang.reflect.Member;
import java.util.EnumSet;

/**
 * Twitter Snowflake-inspired 64-bit ID generator.
 * Layout: 1 bit sign | 41 bits timestamp | 10 bits worker | 12 bits sequence
 */
public class SnowflakeIdGenerator implements BeforeExecutionGenerator {

    private static final long CUSTOM_EPOCH = 1724630400000L; // 2024-08-26T00:00:00Z
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);  // 1023
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);    // 4095
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;           // 12
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS; // 22

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(SnowflakeId annotation, Member member, GeneratorCreationContext context) {
        this.workerId = Long.parseLong(
            System.getProperty("app.snowflake.worker-id",
                System.getenv().getOrDefault("SNOWFLAKE_WORKER_ID", "1")));
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("Worker ID must be between 0 and " + MAX_WORKER_ID);
        }
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }

    @Override
    public synchronized Object generate(SharedSessionContractImplementor session,
                                         Object owner, Object currentValue, EventType eventType) {
        long currentTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;

        // Clock drift handling
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            if (offset <= 5) {
                try { Thread.sleep(offset); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Clock drift wait interrupted", e);
                }
                currentTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
            } else {
                throw new RuntimeException("Clock moved backwards by " + offset + "ms. Refusing to generate ID.");
            }
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return (currentTimestamp << TIMESTAMP_SHIFT)
             | (workerId << WORKER_ID_SHIFT)
             | sequence;
    }

    private long waitNextMillis(long lastTs) {
        long ts = System.currentTimeMillis() - CUSTOM_EPOCH;
        while (ts <= lastTs) {
            ts = System.currentTimeMillis() - CUSTOM_EPOCH;
        }
        return ts;
    }
}
