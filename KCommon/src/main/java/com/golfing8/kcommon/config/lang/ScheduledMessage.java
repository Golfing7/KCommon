package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import com.golfing8.kcommon.struct.helper.terminable.TerminableConsumer;
import com.golfing8.kcommon.struct.time.TimeLength;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A message that can be schedule to send.
 */
@NoArgsConstructor
public class ScheduledMessage implements CASerializable, Terminable {
    /** The initial delay of sending, in ticks */
    private @NotNull TimeLength delay = new TimeLength(0);
    /** The period of sending, in ticks */
    private @NotNull TimeLength period = new TimeLength(0);
    /** The message to send */
    private @Nullable Message message;
    /** The consumer of the message for how it's meant to be sent */
    @Setter
    private transient @Nullable Consumer<Message> onMessageSend = Message::broadcast;
    private transient BukkitTask task;

    public ScheduledMessage(@NotNull TimeLength delay, @NotNull TimeLength period, @Nullable Message message) {
        this.delay = delay;
        this.period = period;
        this.message = message;
    }

    private void run() {
        // Never continue if the message is null.
        if (message == null) {
            if (task != null) {
                task.cancel();
                task = null;
            }
            return;
        }

        if (onMessageSend != null) {
            onMessageSend.accept(message);
        }
    }

    @Override
    public void close() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Starts running this message with the given consumer.
     *
     * @param consumer the consumer.
     */
    public void start(Consumer<Message> onMessageSend, TerminableConsumer consumer) {
        task = Bukkit.getScheduler().runTaskTimer(KCommon.getInstance(), this::run, this.delay.getDurationTicks(), this.period.getDurationTicks());
        this.onMessageSend = onMessageSend;
        consumer.bind(this);
    }
}
