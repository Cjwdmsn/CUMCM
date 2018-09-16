class CNCLog {
    private int time;
    private int operation;
    private int index;
    private boolean isFirstStepCNC;

    int getTime() {
        return time;
    }

    void setTime(int time) {
        this.time = time;
    }

    int getOperation() {
        return operation;
    }

    void setOperation(int operation) {
        this.operation = operation;
    }

    int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    boolean isFirstStepCNC() {
        return isFirstStepCNC;
    }

    void setIsFirstStepCNC(boolean firstStepCNC) {
        isFirstStepCNC = firstStepCNC;
    }
}
