public class RGV {
    private static final int MOVE_ONCE = 20;
    private static final int MOVE_TWICE = 35;
    private static final int MOVE_THIRD_TIMES = 50;
    private static final int GIVE_OR_EJECT_SOMETHING_TO_ODD_CNC = 28;
    private static final int GIVE_OR_EJECT_SOMETHING_TO_EVEN_CNC = 33;
    private static final int WASH = 27;

    private int position = 0;
    private boolean hasIntermediateProductOnHand = false;

    int process(CNC[] oddCNC, CNC[] evenCNC, int remainingTime) {
        calculateNextStepTime(position, oddCNC, evenCNC);

        int shortestTimeForDoingNextStep;
        boolean isFinalCNCChoiceOdd = true;
        int shortestTimeForDoingNextStepCNCIndex = 0;

        //Find the shortest next step time and its corresponding CNC index
        if(hasIntermediateProductOnHand) {
            shortestTimeForDoingNextStep = evenCNC[0].getTimeForDoingNextStep();
            isFinalCNCChoiceOdd = false;
            //Can only give the intermediate product to the second step CNC first
            for(int i = 0; i < evenCNC.length; i++) {
                if(evenCNC[i].getTimeForDoingNextStep() < shortestTimeForDoingNextStep) {
                    shortestTimeForDoingNextStep = evenCNC[i].getTimeForDoingNextStep();
                    shortestTimeForDoingNextStepCNCIndex = i;
                }
            }
        } else {
            shortestTimeForDoingNextStep = oddCNC[0].getTimeForDoingNextStep();
            for(int i = 0; i < oddCNC.length; i++) {
                if(oddCNC[i].getTimeForDoingNextStep() < shortestTimeForDoingNextStep) {
                    shortestTimeForDoingNextStep = oddCNC[i].getTimeForDoingNextStep();
                    shortestTimeForDoingNextStepCNCIndex = i;
                }
            }
            for(int i = 0; i < evenCNC.length; i++) {
                if(evenCNC[i].getNextStep() != CNC.GIVE_SOMETHING_SECOND_TIME && evenCNC[i].getTimeForDoingNextStep() < shortestTimeForDoingNextStep) {
                    shortestTimeForDoingNextStep = evenCNC[i].getTimeForDoingNextStep();
                    shortestTimeForDoingNextStepCNCIndex = i;
                    isFinalCNCChoiceOdd = false;
                }
            }
        }

        System.out.println("Shortest index: " + shortestTimeForDoingNextStepCNCIndex);
        System.out.println("Is odd? " + isFinalCNCChoiceOdd);
        System.out.println("Shortest time: " + shortestTimeForDoingNextStep);
        System.out.println("Has intermediate product on hand? " + hasIntermediateProductOnHand);
        if(remainingTime < shortestTimeForDoingNextStep) {
            return 0;
        }

        if(isFinalCNCChoiceOdd) {
            doSomething(oddCNC[shortestTimeForDoingNextStepCNCIndex]);
        } else {
            doSomething(evenCNC[shortestTimeForDoingNextStepCNCIndex]);
        }

        timeLapse(oddCNC, evenCNC, isFinalCNCChoiceOdd, shortestTimeForDoingNextStepCNCIndex, shortestTimeForDoingNextStep);
        position = shortestTimeForDoingNextStepCNCIndex;

        System.out.println("Now is: " + remainingTime + " Position: " + position);
        return remainingTime - shortestTimeForDoingNextStep;
    }

    private int getMoveTime(int position, int index) {
        int moveTime;
        switch (Math.abs(position - index)) {
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

    private void calculateNextStepTime(int rgvPosition, CNC[] oddCNC, CNC[] evenCNC) {
        for (int i = 0; i < oddCNC.length; i++) {
            int moveTime = getMoveTime(rgvPosition, i);
            switch (oddCNC[i].getNextStep()) {
                case CNC.GIVE_SOMETHING_FIRST_TIME:
                    //Not doing anything now
                    oddCNC[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_ODD_CNC);
                    break;
                case CNC.FINISH_PROCESSING_FIRST_TIME:
                    oddCNC[i].setTimeForDoingNextStep(moveTime + oddCNC[i].getProcessRemainingTime()
                            + GIVE_OR_EJECT_SOMETHING_TO_ODD_CNC);
                    break;
            }
            System.out.println("Odd Position: " + i + " Next step time: " + oddCNC[i].getTimeForDoingNextStep()
                    + " Processing remaining time: " + oddCNC[i].getProcessRemainingTime() + " Next step: " + oddCNC[i].getNextStep());
        }

        for (int i = 0; i < evenCNC.length; i++) {
            int moveTime = getMoveTime(rgvPosition, i);
            switch (evenCNC[i].getNextStep()) {
                case CNC.GIVE_SOMETHING_SECOND_TIME:
                    //Not doing anything now
                    evenCNC[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_EVEN_CNC);
                    break;
                case CNC.FINISH_PROCESSING_SECOND_TIME:
                    evenCNC[i].setTimeForDoingNextStep(moveTime + evenCNC[i].getProcessRemainingTime()
                            + GIVE_OR_EJECT_SOMETHING_TO_EVEN_CNC + WASH);
                    break;
                case CNC.EJECT_AND_WASH:
                    evenCNC[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_EVEN_CNC + WASH);
                    break;
            }

            System.out.println("Even Position: " + i + " Next step time: " + evenCNC[i].getTimeForDoingNextStep()
                    + " Processing remaining time: " + evenCNC[i].getProcessRemainingTime() + " Next step: " + evenCNC[i].getNextStep());
        }
    }

    private void timeLapse(CNC[] oddCNC, CNC[] evenCNC, boolean isOddCNC, int CNCIndex, int shortestElapsedTime) {
        for (int i = 0; i < oddCNC.length; i++) {
            if(!(isOddCNC && i == CNCIndex)) {
                if (oddCNC[i].getNextStep() == CNC.FINISH_PROCESSING_FIRST_TIME) {
                    if(oddCNC[i].getProcessRemainingTime() - shortestElapsedTime == 0) {
                        oddCNC[i].setProcessRemainingTime(CNC.PROCESS_FOR_TWO_FIRST_STEP);
                    } else if(oddCNC[i].getProcessRemainingTime() - shortestElapsedTime > 0) {
                        oddCNC[i].setProcessRemainingTime(oddCNC[i].getProcessRemainingTime() - shortestElapsedTime);
                    } else {
                        oddCNC[i].setProcessRemainingTime(0);
                        oddCNC[i].setNextStep(CNC.GIVE_SOMETHING_FIRST_TIME);
                    }
                }
            }

            if(!(!isOddCNC && i == CNCIndex)) {
                if(evenCNC[i].getNextStep() == CNC.FINISH_PROCESSING_SECOND_TIME) {
                    if(evenCNC[i].getProcessRemainingTime() - shortestElapsedTime == 0) {
                        evenCNC[i].setProcessRemainingTime(0);
                        evenCNC[i].setNextStep(CNC.EJECT_AND_WASH);
                    } else if(evenCNC[i].getProcessRemainingTime() - shortestElapsedTime > 0){
                        evenCNC[i].setProcessRemainingTime(evenCNC[i].getProcessRemainingTime() - shortestElapsedTime);
                    } else {
                        evenCNC[i].setProcessRemainingTime(0);
                        evenCNC[i].setNextStep(CNC.EJECT_AND_WASH);
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
