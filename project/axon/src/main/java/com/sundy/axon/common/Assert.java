package com.sundy.axon.common;

public abstract class Assert {

	private Assert(){}
	
	/**
     * Asserts that the value of <code>state</code> is true. If not, an IllegalStateException is thrown.
     *
     * @param state   the state validation expression
     * @param message The message that the exception contains if state evaluates to false
     */
    public static void state(boolean state, String message) {
        if (!state) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Asserts that the given <code>expression</code> is true. If not, an IllegalArgumentException is thrown.
     *
     * @param expression the state validation expression
     * @param message    The message that the exception contains if state evaluates to false
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Asserts that the given <code>expression</code> is false. If not, an IllegalArgumentException is thrown.
     *
     * @param expression the state validation expression
     * @param message    The message that the exception contains if state evaluates to true
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the given <code>value</code> is not <code>null</code>. If not, an IllegalArgumentException is
     * thrown.
     *
     * @param value   the value not to be <code>null</code>
     * @param message The message to add to the exception when the assertion fails
     */
    public static void notNull(Object value, String message) {
        isTrue(value != null, message);
    }

    /**
     * Assert that the given <code>value</code> is not <code>null</code> or empty. If not, an IllegalArgumentException
     * is thrown.
     *
     * @param value   the value to contain at least one character
     * @param message The message to add to the exception when the assertion fails
     */
    public static void notEmpty(String value, String message) {
        notNull(value, message);
        isFalse(value.isEmpty(), message);
    }
	
}
