package org.gate.engine;

public interface TestConstraint {

    /*
     * TS_PROCESSING  test case removed from the testSuites
     * TS_SUCCESS not error or failure when executing.
     * TS_FAILURE assert failure or Exception when model executing
     * TS_ERROR dependent test case failed,error or shutdown. case itself have some issue.
     * */

    String TS_PROCESSING = "processing";
    String TS_SUCCESS = "success";
    String TS_FAILURE = "failure";
    String TS_ERROR = "error";


}
