package sparkanalysis.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AsyncUtils {
    public static void runAsync(Runnable task, Consumer<Throwable> errorHandler)
    {
        CompletableFuture.runAsync(task)
                .exceptionally(ex->{
                    errorHandler.accept(ex);
                    return null;
                });
    }
}
