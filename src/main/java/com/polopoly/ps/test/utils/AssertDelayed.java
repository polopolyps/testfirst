package com.polopoly.ps.test.utils;


import junit.framework.AssertionFailedError;

/**
 * Delayed assertion.
 *
 * Usage example:
 * <pre>
 *     AssertDelayed.that(10*1000, 1*1000, new AssertDelayd.AssertFunction() {
 *          public void doAssert() {
 *              assertEquals("banas", tree.collectRipeFruit);
 *          }
 *     })
 * </pre>
 *
 *
 */
public class AssertDelayed
{
    public interface AssertFunction
    {
        public void doAssert() throws Exception;
    }

    /**
     * Try to assert every sleepTime millis. Returns if successful. If
     * AsseriontFailureError a new assertion will be done, unless
     * maxTime passed, then the assertion failure will be thrown.
     *
     * @param maxTimeMillis fail test if assert fails past max time
     * @param stepTimeMillis sleep time between asserts
     * @param func assert function
     * @exception Exception
     */
    public static void that(long maxTimeMillis, long stepTimeMillis, AssertDelayed.AssertFunction func)
            throws Exception
    {
        long endTime = System.currentTimeMillis() + maxTimeMillis;
        while (true) {
            try {
                func.doAssert();
                return;
            } catch (Error e) {
                if (!(e instanceof AssertionFailedError || e instanceof java.lang.AssertionError)) {
                    throw e;
                }
                long now = System.currentTimeMillis();
                if (now > endTime) {
                    throw e;
                }
            }
            Thread.sleep(stepTimeMillis);
        }
    }


}
