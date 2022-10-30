package dev.s7a.mseds;

public class MinecraftServerExceptionDiscordSender {
    public static void setup() {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
    }

    public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            System.out.println("uncaught exception");
        }
    }
}
