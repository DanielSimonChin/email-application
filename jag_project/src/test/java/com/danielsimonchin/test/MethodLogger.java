package com.danielsimonchin.test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rules or actions required to log whenever a test method is run.
 * Log the name of every test that runs or fails.
 * @author Daniel Simon Chin 1836462
 */
public class MethodLogger extends TestWatcher {
    
    private Logger log;
    
    public MethodLogger() {
        super();
    }
    
    /**
     * Whenever a test starts, this method logs the name of the test method as
     * provided by the description.
     *
     * @param description describes a test which is to be run or has been run
     */
    @Override
    protected void starting(Description description) {
        prepareLogger(description);
        log.info("Starting test [{}]", description.getMethodName());
    }
    
    /**
     * Whenever a test fails, this method logs the name of the test method as
     * provided by the description and the exception thrown.
     *
     * @param e
     * @param description describes a test which is to be run or has been run
     */
    @Override
    protected void failed(Throwable e, Description description) {
        log.info("Failed test [{}]", description.getMethodName() + "\n" + e.getMessage());
    }
    
    /**
     * If logger is not initialized, initialize it with the name of the test
     * class as provided by the description.
     *
     * @param description describes a test which is to be run or has been run
     *
     */
    private void prepareLogger(Description description) {
        if (log == null) {
            log = LoggerFactory.getLogger(description.getClassName());
        }
    }
}
