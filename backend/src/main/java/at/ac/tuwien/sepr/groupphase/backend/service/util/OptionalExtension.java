package at.ac.tuwien.sepr.groupphase.backend.service.util;

import java.util.Optional;
import java.util.function.Supplier;

public class OptionalExtension {
    public static <T> Optional<T> ofThrowable(Supplier<T> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
