package nl.kii.act;

/**
 * Thrown to indicate that the stack for the recursive calls is at its maximum and that it should
 * be rewound.
 */
@SuppressWarnings("all")
public class AtMaxProcessDepth extends Throwable {
}
