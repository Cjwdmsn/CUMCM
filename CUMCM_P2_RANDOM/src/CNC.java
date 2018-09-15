class CNC {
    static final int PROCESS_FOR_TWO_FIRST_STEP = 378;
    static final int PROCESS_FOR_TWO_SECOND_STEP = 353;
    static final int GIVE_SOMETHING_FIRST_TIME = 0;
    static final int FINISH_PROCESSING_FIRST_TIME = 1;
    static final int GIVE_SOMETHING_SECOND_TIME = 2;
    static final int FINISH_PROCESSING_SECOND_TIME = 3;
    static final int EJECT_AND_WASH = 4;

    private int processRemainingTime = 0;
    private int nextStep;
    private int timeForDoingNextStep = 0;
    private boolean isForFirstStep;

    private int nProducts = 0;

    CNC(int nextStep) {
        this.nextStep = nextStep;
    }

    int getProcessRemainingTime() {
        return processRemainingTime;
    }

    void setProcessRemainingTime(int processRemainingTime) {
        this.processRemainingTime = processRemainingTime;
    }

    int getNextStep() {
        return nextStep;
    }

    void setNextStep(int nextStep) {
        this.nextStep = nextStep;
    }

    int getTimeForDoingNextStep() {
        return timeForDoingNextStep;
    }

    void setTimeForDoingNextStep(int timeForDoingNextStep) {
        this.timeForDoingNextStep = timeForDoingNextStep;
    }

    int getNProducts() {
        return nProducts;
    }

    void setNProducts(int nProducts) {
        this.nProducts = nProducts;
    }

    boolean isForFirstStep() {
        return isForFirstStep;
    }

    void setForFirstStep(boolean forFirstStep) {
        isForFirstStep = forFirstStep;
    }
}
