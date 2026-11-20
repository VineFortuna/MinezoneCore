package anthony.util;

import java.util.Optional;

public class UtilClassName {
    private static final StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private static Optional<String> getClassName(Integer howManySkips) {
        Optional<Class<?>> finder = stackWalker.walk(frames ->
                        frames.skip(howManySkips)
                        .findFirst()
                        .map(StackWalker.StackFrame::getDeclaringClass));

        return finder.map(Class::getName);
    }

    public static Optional<String> getCallingClassName() {
        return getClassName(2);
        // Two, since we need to skip UtilClassName, and also the class hosting the method being called.
    }

    public static Optional<String> getCurrentClassName() {
        return getClassName(1);
        // Just one, skip the UtilClassName.
    }
}
