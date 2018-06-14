package de.tobiasroeser.lambdatest.internal;

import java.text.MessageFormat;
import java.util.Arrays;

import static de.tobiasroeser.lambdatest.Expect.expectTrue;

public class ExpectCommon {
  public static void internalCheck(final boolean cond, final String msg, final Object[] args) {
    if (!cond) {
      String error;
      if (args == null || args.length == 0) {
        error = msg;
      } else {
        final Object[] niceArgs = new Object[args.length];
        for (int i = 0; i < args.length; ++i) {
          final Object arg = args[i];
          if (arg != null && arg.getClass().isArray()) {
            niceArgs[i] = Util.mkString(Arrays.asList((Object[]) arg), "[", ",", "]");
          } else {
            niceArgs[i] = arg;
          }
        }
        error = MessageFormat.format(msg, niceArgs);
      }
      expectTrue(false, error);
    }
  }
}
