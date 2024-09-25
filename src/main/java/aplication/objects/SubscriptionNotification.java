package aplication.objects;

import aplication.update.Updated;
import aplication.update.Updater;

import javax.management.Notification;

//public class SubscriptionNotification<T extends Updater<T, U>, U extends  Updated> extends Notification {
//    private final SubscriptionEvent<?, ?> type;
//    public SubscriptionNotification(SubscriptionEvent<?, ?> type, GameObject source, long sequenceNumber, long timeStamp, String message) {
//        super(type.getClass().getTypeName(), source, sequenceNumber, timeStamp, message);
//        this.type = type;
//    }
//
//    public SubscriptionEvent<?, ?> type() {
//        return type;
//    }
//
//    public record Subscribed<T extends Updater<T, U>, U extends Updated>(Updater<T, U> updater) implements SubscriptionEvent<T, U> {}
//    public record Unsubscribed<T extends Updater<T, U>, U extends Updated>(Updater<T, U> updater) implements SubscriptionEvent<T, U> {}
//
//    public sealed interface SubscriptionEvent<T extends Updater<T, U>, U extends Updated> {
//        Updater<T, U> updater();
//    }
//}
