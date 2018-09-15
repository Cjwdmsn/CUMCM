class RGV {
    private static final int MOVE_ONCE = 20;
    private static final int MOVE_TWICE = 35;
    private static final int MOVE_THIRD_TIMES = 50;
    private static final int GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC = 28;
    private static final int GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC = 33;
    private static final int WASH = 27;

    private int position = 0;
    private boolean hasIntermediateProductOnHand = false;

    int process(CNC[] CNCs, int remainingTime) {
        calculateNextStepTime(position, CNCs);

        int shortestTimeForDoingNextStep = Integer.MAX_VALUE;
        int shortestTimeForDoingNextStepCNCIndex = 0;

        //Find the shortest next step time and its corresponding CNC index
        if(hasIntermediateProductOnHand) {
            //Can only give the intermediate product to the second step CNC first
            for(int i = 0; i < CNCs.length; i++) {
                if(!CNCs[i].isForFirstStep() && CNCs[i].getTimeForDoingNextStep() < shortestTimeForDoingNextStep) {
                    shortestTimeForDoingNextStep = CNCs[i].getTimeForDoingNextStep();
                    shortestTimeForDoingNextStepCNCIndex = i;
                }
            }
        } else {
            for(int i = 0; i < CNCs.length; i++) {
                if(CNCs[i].isForFirstStep() && CNCs[i].getTimeForDoingNextStep() < shortestTimeForDoingNextStep) {
                    shortestTimeForDoingNextStep = CNCs[i].getTimeForDoingNextStep();
                    shortestTimeForDoingNextStepCNCIndex = i;
                }
            }
            for(int i = 0; i < CNCs.length; i++) {
                if(!CNCs[i].isForFirstStep() && CNCs[i].getNextStep() != CNC.GIVE_SOMETHING_SECOND_TIME && CNCs[i].getTimeForDoingNextStep() < shortestTimeForDoingNextStep) {
                    shortestTimeForDoingNextStep = CNCs[i].getTimeForDoingNextStep();
                    shortestTimeForDoingNextStepCNCIndex = i;
                }
            }
        }

        /*System.out.println("Shortest index: " + shortestTimeForDoingNextStepCNCIndex);
        System.out.println("Is for first step? " + CNCs[shortestTimeForDoingNextStepCNCIndex].isForFirstStep());
        System.out.println("Shortest time: " + shortestTimeForDoingNextStep);
        System.out.println("Has intermediate product on hand? " + hasIntermediateProductOnHand);*/
        if(remainingTime < shortestTimeForDoingNextStep) {
            return 0;
        }

        doSomething(CNCs[shortestTimeForDoingNextStepCNCIndex]);

        timeLapse(CNCs, shortestTimeForDoingNextStepCNCIndex, shortestTimeForDoingNextStep);
        position = shortestTimeForDoingNextStepCNCIndex % Constraint.CNCS_COUNT_ONE_ROW;

        /*System.out.println("Now is: " + remainingTime + " Position: " + position);*/
        return remainingTime - shortestTimeForDoingNextStep;
    }

    private int getMoveTime(int position, int index) {
        int moveTime;
        switch (Math.abs(position - index % Constraint.CNCS_COUNT_ONE_ROW)) {
            case 1:
                moveTime = MOVE_ONCE;
                break;
            case 2:
                moveTime = MOVE_TWICE;
                break;
            case 3:
                moveTime = MOVE_THIRD_TIMES;
                break;
            default:
                moveTime = 0;
        }
        return moveTime;
    }

    private void calculateNextStepTime(int rgvPosition, CNC[] CNCs) {
        for (int i = 0; i < CNCs.length; i++) {
            int moveTime = getMoveTime(rgvPosition, i);
            switch (CNCs[i].getNextStep()) {
                case CNC.GIVE_SOMETHING_FIRST_TIME:
                    //Not doing anything now
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                    } else {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                    }
                    break;
                case CNC.FINISH_PROCESSING_FIRST_TIME:
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                    }
                    CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                            + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                    break;
                case CNC.GIVE_SOMETHING_SECOND_TIME:
                    //Not doing anything now
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                    } else {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                    }
                    break;
                case CNC.FINISH_PROCESSING_SECOND_TIME:
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC + WASH);
                    } else {
                        CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC + WASH);
                    }
                    break;
                case CNC.EJECT_AND_WASH:
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC + WASH);
                    } else {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC + WASH);
                    }
                    break;
            }
            /*System.out.println("Position: " + i + " Next step time: " + CNCs[i].getTimeForDoingNextStep()
                    + " Processing remaining time: " + CNCs[i].getProcessRemainingTime() + " Next step: " + CNCs[i].getNextStep());*/
        }
    }

    private void timeLapse(CNC[] CNCs, int CNCIndex, int shortestElapsedTime) {
        for (int i = 0; i < CNCs.length; i++) {
            if(i != CNCIndex) {
                if (CNCs[i].getNextStep() == CNC.FINISH_PROCESSING_FIRST_TIME) {
                    if(CNCs[i].getProcessRemainingTime() - shortestElapsedTime == 0) {
                        CNCs[i].setProcessRemainingTime(CNC.PROCESS_FOR_TWO_FIRST_STEP);
                    } else if(CNCs[i].getProcessRemainingTime() - shortestElapsedTime > 0) {
                        CNCs[i].setProcessRemainingTime(CNCs[i].getProcessRemainingTime() - shortestElapsedTime);
                    } else {
                        CNCs[i].setProcessRemainingTime(0);
                        CNCs[i].setNextStep(CNC.GIVE_SOMETHING_FIRST_TIME);
                    }
                } else if(CNCs[i].getNextStep() == CNC.FINISH_PROCESSING_SECOND_TIME) {
                    if(CNCs[i].getProcessRemainingTime() - shortestElapsedTime == 0) {
                        CNCs[i].setProcessRemainingTime(0);
                        CNCs[i].setNextStep(CNC.EJECT_AND_WASH);
                    } else if(CNCs[i].getProcessRemainingTime() - shortestElapsedTime > 0){
                        CNCs[i].setProcessRemainingTime(CNCs[i].getProcessRemainingTime() - shortestElapsedTime);
                    } else {
                        CNCs[i].setProcessRemainingTime(0);
                        CNCs[i].setNextStep(CNC.EJECT_AND_WASH);
                    }
                }
            }
        }
    }

    private void doSomething(CNC cnc) {
        switch (cnc.getNextStep()) {
            case CNC.GIVE_SOMETHING_FIRST_TIME:
                cnc.setNextStep(CNC.FINISH_PROCESSING_FIRST_TIME);
                cnc.setProcessRemainingTime(CNC.PROCESS_FOR_TWO_FIRST_STEP);
                break;
            case CNC.FINISH_PROCESSING_FIRST_TIME:
                cnc.setProcessRemainingTime(CNC.PROCESS_FOR_TWO_FIRST_STEP);
                hasIntermediateProductOnHand = true;
                break;
            case CNC.GIVE_SOMETHING_SECOND_TIME:
                cnc.setNextStep(CNC.FINISH_PROCESSING_SECOND_TIME);
                cnc.setProcessRemainingTime(CNC.PROCESS_FOR_TWO_SECOND_STEP);
                hasIntermediateProductOnHand = false;
                break;
            case CNC.FINISH_PROCESSING_SECOND_TIME:
                cnc.setNextStep(CNC.GIVE_SOMETHING_SECOND_TIME);
                cnc.setProcessRemainingTime(0);
                break;
            case CNC.EJECT_AND_WASH:
                cnc.setNextStep(CNC.GIVE_SOMETHING_SECOND_TIME);
                cnc.setProcessRemainingTime(0);
                cnc.setNProducts(cnc.getNProducts() + 1);
        }
    }
}
