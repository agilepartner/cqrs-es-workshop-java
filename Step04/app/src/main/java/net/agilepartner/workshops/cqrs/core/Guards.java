package net.agilepartner.workshops.cqrs.core;

public final class Guards {
    private Guards() {}
  
  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public static String checkNotNullOrEmpty(String value) {
    checkNotNull(value);
    if (value == "")
      throw new IllegalArgumentException("String should not be empty");

    return value;
  }

  public static void checkArgument(boolean b, String errorMessageTemplate, int p1, int p2) {
    if (!b) {
      throw new IllegalArgumentException(String.format(errorMessageTemplate, p1, p2));
    }
  }

  public static RuntimeException propagate(Throwable throwable) {
    throwIfUnchecked(throwable);
    throw new RuntimeException(throwable);
  }

  public static void throwIfUnchecked(Throwable throwable) {
    checkNotNull(throwable);
    if (throwable instanceof RuntimeException) {
      throw (RuntimeException) throwable;
    }
    if (throwable instanceof Error) {
      throw (Error) throwable;
    }
  }
}