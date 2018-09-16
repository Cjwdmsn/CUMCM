import java.util.ArrayList;

class RGV {
    private static final int MOVE_ONCE = 23;
    private static final int MOVE_TWICE = 41;
    private static final int MOVE_THIRD_TIMES = 59;
    private static final int GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC = 30;
    private static final int GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC = 35;
    private static final int WASH = 30;

    private int position = 0;

    private ArrayList<ArrayList<CNCLog>> cncLogs;

    RGV(ArrayList<ArrayList<CNCLog>> cncLogs) {
        this.cncLogs = cncLogs;
    }

    int process(CNC[] CNCs, int remainingTime) {
        calculateNextStepTime(position, CNCs);

        int shortestTimeForDoingNextStep = CNCs[0].getTimeForDoingNextStep();
        int shortestTimeForDoingNextStepCNCIndex = 0;

        //找出下一步工作时间最短的CNC及其位置，并将这两个值存入变量shortestTimeForDoingNextStep，shortestTimeForDoingNextStepCNCIndex
        for(int i = 0; i < CNCs.length; i++) {
            if(CNCs[i].getTimeForDoingNextStep() < shortestTimeForDoingNextStep) {
                shortestTimeForDoingNextStep = CNCs[i].getTimeForDoingNextStep();
                shortestTimeForDoingNextStepCNCIndex = i;
            }
        }

        System.out.println("Shortest index: " + shortestTimeForDoingNextStepCNCIndex);
        System.out.println("Is fast? " + (shortestTimeForDoingNextStepCNCIndex < 4));
        System.out.println("Shortest time: " + shortestTimeForDoingNextStep);
        if(remainingTime < shortestTimeForDoingNextStep) {
            return 0;
        }

        doSomething(CNCs[shortestTimeForDoingNextStepCNCIndex], remainingTime, shortestTimeForDoingNextStepCNCIndex);

        timeLapse(CNCs, shortestTimeForDoingNextStepCNCIndex, shortestTimeForDoingNextStep);

        //更新RGV位置
        position = shortestTimeForDoingNextStepCNCIndex % Constraint.CNCS_COUNT_ONE_ROW;

        System.out.println("Now is: " + remainingTime + " Position: " + position);
        return remainingTime - shortestTimeForDoingNextStep;
    }

    //计算从RGV此时的位置移动到该CNC的位置的时间
    private int getMoveTime(int position, int index) {
        int moveTime = 0;
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
        }
        return moveTime;
    }

    private void calculateNextStepTime(int rgvPosition, CNC[] CNCs) {
        for (int i = 0; i < CNCs.length; i++) {
            int moveTime = getMoveTime(rgvPosition, i);
            if(i < Constraint.CNCS_COUNT_ONE_ROW) {
                switch (CNCs[i].getNextStep()) {
                    case CNC.GIVE_SOMETHING:
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                        break;
                    case CNC.FINISH_PROCESSING:
                        CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC * 2 + WASH);
                        break;
                    case CNC.EJECT_AND_WASH:
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC * 2 + WASH);
                        break;
                }
                System.out.println("Fast Position: " + i + " Next step time: " + CNCs[i].getTimeForDoingNextStep()
                        + " Processing remaining time: " + CNCs[i].getProcessRemainingTime() + " Next step: " + CNCs[i].getNextStep());
            } else {
                switch (CNCs[i].getNextStep()) {
                    case CNC.GIVE_SOMETHING:
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                        break;
                    case CNC.FINISH_PROCESSING:
                        CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC * 2 + WASH);
                        break;
                    case CNC.EJECT_AND_WASH:
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC * 2 + WASH);
                        break;
                }

                System.out.println("Slow Position: " + i + " Next step time: " + CNCs[i].getTimeForDoingNextStep()
                        + " Processing remaining time: " + CNCs[i].getProcessRemainingTime() + " Next step: " + CNCs[i].getNextStep());
            }
        }
    }

    private void timeLapse(CNC[] CNCs, int CNCIndex, int shortestElapsedTime) {
        for (int i = 0; i < CNCs.length; i++) {
            if(i != CNCIndex) {
                if (CNCs[i].getNextStep() == CNC.FINISH_PROCESSING) {
                    if(CNCs[i].getProcessRemainingTime() - shortestElapsedTime <= 0) {
                        CNCs[i].setProcessRemainingTime(0);
                        CNCs[i].setNextStep(CNC.EJECT_AND_WASH);
                    } else {
                        CNCs[i].setProcessRemainingTime(CNCs[i].getProcessRemainingTime() - shortestElapsedTime);
                    }
                }
            }
        }
    }

    private void doSomething(CNC cnc, int time, int cncIndex) {
        switch (cnc.getNextStep()) {
            case CNC.GIVE_SOMETHING:
                cnc.setNextStep(CNC.FINISH_PROCESSING);
                cnc.setProcessRemainingTime(CNC.PROCESS);

                CNCLog cncLog = new CNCLog();
                cncLog.setTime(time);
                cncLog.setOperation(CNC.GIVE_SOMETHING);
                cncLog.setIndex(cncIndex);
                cncLogs.get(cncIndex).add(cncLog);
                break;
            case CNC.FINISH_PROCESSING:
                cnc.setNextStep(CNC.FINISH_PROCESSING);
                cnc.setProcessRemainingTime(CNC.PROCESS);
                cnc.setNProducts(cnc.getNProducts() + 1);

                CNCLog cncLog1 = new CNCLog();
                cncLog1.setTime(time);
                cncLog1.setOperation(CNC.EJECT_AND_WASH);
                cncLog1.setIndex(cncIndex);
                cncLogs.get(cncIndex).add(cncLog1);

                CNCLog cncLogGive = new CNCLog();
                if(cncIndex >= Constraint.CNCS_COUNT_ONE_ROW) {
                    cncLogGive.setTime(time - GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                } else {
                    cncLogGive.setTime(time - GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                }
                cncLogGive.setOperation(CNC.GIVE_SOMETHING);
                cncLogGive.setIndex(cncIndex);
                cncLogs.get(cncIndex).add(cncLogGive);
                break;
            case CNC.EJECT_AND_WASH:
                cnc.setNextStep(CNC.FINISH_PROCESSING);
                cnc.setProcessRemainingTime(CNC.PROCESS);
                cnc.setNProducts(cnc.getNProducts() + 1);

                CNCLog cncLog2 = new CNCLog();
                cncLog2.setTime(time);
                cncLog2.setOperation(CNC.EJECT_AND_WASH);
                cncLog2.setIndex(cncIndex);
                cncLogs.get(cncIndex).add(cncLog2);

                CNCLog cncLogGive1 = new CNCLog();
                if(cncIndex >= Constraint.CNCS_COUNT_ONE_ROW) {
                    cncLogGive1.setTime(time - GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                } else {
                    cncLogGive1.setTime(time - GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                }
                cncLogGive1.setOperation(CNC.GIVE_SOMETHING);
                cncLogGive1.setIndex(cncIndex);
                cncLogs.get(cncIndex).add(cncLogGive1);
                break;
        }
    }
}
