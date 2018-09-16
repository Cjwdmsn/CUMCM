class CNC {
    static final int PROCESS = 545;
    static final int GIVE_SOMETHING = 0;
    static final int FINISH_PROCESSING = 1;
    static final int EJECT_AND_WASH = 2;
    static final int BREAK = 3;
    static final int REPAIRED = 4;

    private int processRemainingTime = 0;
    private int nextStep = GIVE_SOMETHING;
    private int timeForDoingNextStep = 0;

    private int nProducts = 0;

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
}
