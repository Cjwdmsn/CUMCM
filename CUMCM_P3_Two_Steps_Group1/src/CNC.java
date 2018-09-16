class CNC {
    //CNC第一步操作所需的时间
    static final int PROCESS_FOR_TWO_FIRST_STEP = 400;
    //CNC第二步操作所需的时间
    static final int PROCESS_FOR_TWO_SECOND_STEP = 378;

    //第一步操作CNC的下一步操作为等待被给生料
    static final int GIVE_SOMETHING_FIRST_TIME = 0;
    //第一步操作CNC的下一步操作为完成处理生料
    static final int FINISH_PROCESSING_FIRST_TIME = 1;
    //第一步操作CNC的下一步操作为等待被取出已处理生料
    static final int EJECT_FROM_FIRST_STEP_CNC = 2;
    //第二步操作CNC的下一步操作为等待被给中间产品
    static final int GIVE_SOMETHING_SECOND_TIME = 3;
    //第二步操作CNC的下一步操作为完成处理中间产品
    static final int FINISH_PROCESSING_SECOND_TIME = 4;
    //第二步操作CNC的下一步操作为等待被取出已处理的中间产品并清洗该熟料
    static final int EJECT_AND_WASH = 5;
    static final int BREAK_FIRST_STEP_CNC = 6;
    static final int REPAIRED_FIRST_STEP_CNC = 7;
    static final int BREAK_SECOND_STEP_CNC = 8;
    static final int REPAIRED_SECOND_STEP_CNC = 9;

    //记录操作剩余时间
    private int processRemainingTime = 0;
    //记录CNC的下一步操作
    private int nextStep;
    //记录CNC的下一步操作的所需时间
    private int timeForDoingNextStep = 0;
    //分辨该CNC是否被用作第一步操作
    private boolean isForFirstStep;
    //记录CNC处理完的产品数量
    private int nProducts = 0;

    //该类的构造器，以被用于初始化下一步操作
    CNC(int nextStep) {
        this.nextStep = nextStep;
    }

    //以下为Private变量的getter和setter
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
