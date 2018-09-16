import java.util.ArrayList;

class RGV {
    //RGV移动一个单位所需要的时间
    private static final int MOVE_ONCE = 23;
    //RGV移动两个单位所需要的时间
    private static final int MOVE_TWICE = 41;
    //RGV移动三个单位所需要的时间
    private static final int MOVE_THIRD_TIMES = 59;
    //RGV放置或取出CNC所需要的时间（快速）
    private static final int GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC = 30;
    //RGV放置或取出CNC所需要的时间（慢速）
    private static final int GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC = 35;
    //RGV清洗产品所需要的时间
    private static final int WASH = 30;

    //记录RGV的当前位置
    private int position = 0;
    //记录RGV的手上是否有中间产品
    private boolean hasIntermediateProductOnHand = false;

    private ArrayList<ArrayList<CNCLog>> cncLogs;

    RGV(ArrayList<ArrayList<CNCLog>> cncLogs) {
        this.cncLogs = cncLogs;
    }

    //进行RGV操作
    int process(CNC[] CNCs, int remainingTime) {
        //计算所有CNC做下一步操作的时间
        calculateNextStepTime(position, CNCs);

        int shortestTimeForDoingNextStep = Integer.MAX_VALUE;
        int shortestTimeForDoingNextStepCNCIndex = 0;

        //找出下一步工作时间最短的CNC及其位置，并将这两个值存入变量shortestTimeForDoingNextStep，shortestTimeForDoingNextStepCNCIndex
        if(hasIntermediateProductOnHand) {
            //必须先将RGV手中的中间产品放入做第二步的CNC上
            for(int i = 0; i < CNCs.length; i++) {
                //仅仅比较做第二步操作的CNC时间
                if(!CNCs[i].isForFirstStep() && CNCs[i].getTimeForDoingNextStep() < shortestTimeForDoingNextStep) {
                    shortestTimeForDoingNextStep = CNCs[i].getTimeForDoingNextStep();
                    shortestTimeForDoingNextStepCNCIndex = i;
                }
            }
        } else {
            //比较所有CNC做下一步操作的时间，因为此时RGV手中没有中间产品
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

        //如果没有足够的时间让做最快的下一步操作的CNC做完操作，则停止所有工作，否则就做最快操作
        if(remainingTime < shortestTimeForDoingNextStep) {
            return 0;
        }

        //做下一步操作
        doSomething(CNCs[shortestTimeForDoingNextStepCNCIndex], remainingTime, shortestTimeForDoingNextStepCNCIndex);

        //时间流逝
        timeLapse(CNCs, shortestTimeForDoingNextStepCNCIndex, remainingTime, shortestTimeForDoingNextStep);

        //更新RGV位置
        position = shortestTimeForDoingNextStepCNCIndex % Constraint.CNCS_COUNT_ONE_ROW;

        //返回剩余时间
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

    //计算所有CNC做下一步操作需要的时间
    private void calculateNextStepTime(int rgvPosition, CNC[] CNCs) {
        for (int i = 0; i < CNCs.length; i++) {
            //计算RGV移动至该CNC的时间
            int moveTime = getMoveTime(rgvPosition, i);
            switch (CNCs[i].getNextStep()) {
                case CNC.GIVE_SOMETHING_FIRST_TIME:
                    /*
                       该CNC为第一步操作所需CNC
                       此时该CNC未做任何事
                       根据CNC的位置来计算下一步操作所需要的时间
                       下一步操作：RGV移动至该CNC，并放置生料至该CNC上
                     */
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                    } else {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                    }
                    break;
                case CNC.FINISH_PROCESSING_FIRST_TIME:
                    /*
                       该CNC为第一步操作所需CNC
                       该CNC正在进行第一步加工操作
                       根据CNC的位置来计算下一步操作所需要的时间
                       下一步操作：RGV移动至该CNC，CNC完成该加工操作所需要的时间，RGV从该CNC上取下中间产品, RGV放置生料至该CNC
                     */
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC * 2);
                    } else {
                        CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC * 2);
                    }
                    break;
                case CNC.EJECT_FROM_FIRST_STEP_CNC:
                    /*
                       该CNC为第一步操作所需CNC
                       该CNC已进行完操作
                       根据CNC的位置来计算下一步操作所需要的时间
                       下一步操作：RGV移动至该CNC，RGV从该CNC上取下中间产品, RGV放置生料至该CNC
                     */
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC * 2);
                    } else {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC * 2);
                    }
                    break;
                case CNC.GIVE_SOMETHING_SECOND_TIME:
                    /*
                       该CNC为第二步操作所需CNC
                       此时该CNC未做任何事
                       根据CNC的位置来计算下一步操作所需要的时间
                       下一步操作：RGV移动至该CNC，并放置中间产物至该CNC上
                     */
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                    } else {
                        CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                    }
                    break;
                case CNC.FINISH_PROCESSING_SECOND_TIME:
                    /*
                       该CNC为第二步操作所需CNC
                       此时该CNC正在进行第二步加工操作
                       根据CNC的位置来计算下一步操作所需要的时间
                       下一步操作：RGV移动至该CNC，CNC完成该加工操作做需要的时间，RGV从该CNC上取下已完成的产品，清洗中间产品
                     */
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        if(hasIntermediateProductOnHand) {
                            CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                    + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC * 2 + WASH);
                        } else {
                            CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                    + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC + WASH);
                        }
                    } else {
                        if(hasIntermediateProductOnHand) {
                            CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                    + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC * 2 + WASH);
                        } else {
                            CNCs[i].setTimeForDoingNextStep(moveTime + CNCs[i].getProcessRemainingTime()
                                    + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC + WASH);
                        }
                    }
                    break;
                case CNC.EJECT_AND_WASH:
                    /*
                       该CNC为第二步操作所需CNC
                       此时该CNC已完成第二部加工操作
                       根据CNC的位置来计算下一步操作所需要的时间
                       下一步操作：RGV移动至该CNC，RGV从该CNC上取下已完成的产品，清洗中间产品
                     */
                    if(i >= Constraint.CNCS_COUNT_ONE_ROW) {
                        if(hasIntermediateProductOnHand) {
                            CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC * 2 + WASH);
                        } else {
                            CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC + WASH);
                        }
                    } else {
                        if(hasIntermediateProductOnHand) {
                            CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC * 2 + WASH);
                        } else {
                            CNCs[i].setTimeForDoingNextStep(moveTime + GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC + WASH);
                        }
                    }
                    break;
            }
        }
    }

    //时间流逝
    private void timeLapse(CNC[] CNCs, int CNCIndex, int time, int shortestElapsedTime) {
        for (int i = 0; i < CNCs.length; i++) {
            //仅更新下一步未进行操作的CNC的状态
            if(i != CNCIndex) {
                //仅需更新正在处理产品的CNC的状态
                if (CNCs[i].getNextStep() == CNC.FINISH_PROCESSING_FIRST_TIME) {
                    if(CNCs[i].getProcessRemainingTime() - shortestElapsedTime <= 0) {
                        //当该CNC在时间流逝之后刚好能完成操作，或在时间流逝中就能完成操作，则将该CNC的下一步操作改为EJECT
                        CNCs[i].setProcessRemainingTime(0);
                        CNCs[i].setNextStep(CNC.EJECT_FROM_FIRST_STEP_CNC);
                    } else {
                        //仅更新操作剩余时间，不需更新状态，因为流逝的时间不足以让该CNC完成操作
                        CNCs[i].setProcessRemainingTime(CNCs[i].getProcessRemainingTime() - shortestElapsedTime);
                    }
                } else if(CNCs[i].getNextStep() == CNC.FINISH_PROCESSING_SECOND_TIME) {
                    if(CNCs[i].getProcessRemainingTime() - shortestElapsedTime <= 0) {
                        //当该CNC在时间流逝之后刚好能完成操作，或在时间流逝中就能完成操作，则将该CNC的下一步操作改为EJECT_AND_WASH
                        CNCs[i].setProcessRemainingTime(0);
                        CNCs[i].setNextStep(CNC.EJECT_AND_WASH);
                    } else {
                        //仅更新操作剩余时间，不需更新状态，因为流逝的时间不足以让该CNC完成操作
                        CNCs[i].setProcessRemainingTime(CNCs[i].getProcessRemainingTime() - shortestElapsedTime);
                    }
                }
            }
        }
    }

    private void doSomething(CNC cnc, int time, int cncIndex) {
        switch (cnc.getNextStep()) {
            case CNC.GIVE_SOMETHING_FIRST_TIME:
                //此时该CNC正在等待产品被放入，这种情况出现在第一次处理产品之前
                cnc.setNextStep(CNC.FINISH_PROCESSING_FIRST_TIME);
                cnc.setProcessRemainingTime(CNC.PROCESS_FOR_TWO_FIRST_STEP);
                hasIntermediateProductOnHand = false;

                CNCLog cncLog = new CNCLog();
                cncLog.setTime(time);
                cncLog.setOperation(CNC.GIVE_SOMETHING_FIRST_TIME);
                cncLog.setIndex(cncIndex);
                cncLog.setIsFirstStepCNC(true);
                cncLogs.get(cncIndex).add(cncLog);

                break;
            case CNC.FINISH_PROCESSING_FIRST_TIME:
                //此时该CNC正在加工产品
                cnc.setProcessRemainingTime(CNC.PROCESS_FOR_TWO_FIRST_STEP);
                hasIntermediateProductOnHand = true;

                CNCLog cncLog1 = new CNCLog();
                cncLog1.setTime(time);
                cncLog1.setOperation(CNC.EJECT_FROM_FIRST_STEP_CNC);
                cncLog1.setIndex(cncIndex);
                cncLog1.setIsFirstStepCNC(true);
                cncLogs.get(cncIndex).add(cncLog1);

                CNCLog cncLogGive = new CNCLog();
                if(cncIndex >= Constraint.CNCS_COUNT_ONE_ROW) {
                    cncLogGive.setTime(time - RGV.GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                } else {
                    cncLogGive.setTime(time - RGV.GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                }
                cncLogGive.setOperation(CNC.GIVE_SOMETHING_FIRST_TIME);
                cncLogGive.setIndex(cncIndex);
                cncLogGive.setIsFirstStepCNC(true);
                cncLogs.get(cncIndex).add(cncLogGive);

                break;
            case CNC.EJECT_FROM_FIRST_STEP_CNC:
                //此时该CNC已经加工完成
                cnc.setNextStep(CNC.FINISH_PROCESSING_FIRST_TIME);
                cnc.setProcessRemainingTime(CNC.PROCESS_FOR_TWO_FIRST_STEP);
                hasIntermediateProductOnHand = true;

                CNCLog cncLog2 = new CNCLog();
                cncLog2.setTime(time);
                cncLog2.setOperation(CNC.EJECT_FROM_FIRST_STEP_CNC);
                cncLog2.setIndex(cncIndex);
                cncLog2.setIsFirstStepCNC(true);
                cncLogs.get(cncIndex).add(cncLog2);

                CNCLog cncLogGive1 = new CNCLog();
                if(cncIndex >= Constraint.CNCS_COUNT_ONE_ROW) {
                    cncLogGive1.setTime(time - RGV.GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                } else {
                    cncLogGive1.setTime(time - RGV.GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                }
                cncLogGive1.setOperation(CNC.GIVE_SOMETHING_FIRST_TIME);
                cncLogGive1.setIndex(cncIndex);
                cncLogGive1.setIsFirstStepCNC(true);
                cncLogs.get(cncIndex).add(cncLogGive1);

                break;
            case CNC.GIVE_SOMETHING_SECOND_TIME:
                //此时该CNC等待被放入中间产品
                cnc.setNextStep(CNC.FINISH_PROCESSING_SECOND_TIME);
                cnc.setProcessRemainingTime(CNC.PROCESS_FOR_TWO_SECOND_STEP);

                hasIntermediateProductOnHand = false;

                CNCLog cncLog3 = new CNCLog();
                cncLog3.setTime(time);
                cncLog3.setOperation(CNC.GIVE_SOMETHING_SECOND_TIME);
                cncLog3.setIndex(cncIndex);
                cncLog3.setIsFirstStepCNC(false);
                cncLogs.get(cncIndex).add(cncLog3);

                break;
            case CNC.FINISH_PROCESSING_SECOND_TIME:
                //此时该CNC正在操作中间产品
                if(hasIntermediateProductOnHand) {
                    cnc.setNextStep(CNC.FINISH_PROCESSING_SECOND_TIME);
                    cnc.setProcessRemainingTime(CNC.PROCESS_FOR_TWO_SECOND_STEP);

                    CNCLog cncLog4 = new CNCLog();
                    cncLog4.setTime(time);
                    cncLog4.setOperation(CNC.EJECT_FROM_FIRST_STEP_CNC);
                    cncLog4.setIndex(cncIndex);
                    cncLog4.setIsFirstStepCNC(false);
                    cncLogs.get(cncIndex).add(cncLog4);

                    CNCLog cncLogGive2 = new CNCLog();
                    if(cncIndex >= Constraint.CNCS_COUNT_ONE_ROW) {
                        cncLogGive2.setTime(time - RGV.GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                    } else {
                        cncLogGive2.setTime(time - RGV.GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                    }
                    cncLogGive2.setOperation(CNC.GIVE_SOMETHING_SECOND_TIME);
                    cncLogGive2.setIndex(cncIndex);
                    cncLogGive2.setIsFirstStepCNC(false);
                    cncLogs.get(cncIndex).add(cncLogGive2);
                } else {
                    cnc.setNextStep(CNC.GIVE_SOMETHING_SECOND_TIME);
                    cnc.setProcessRemainingTime(0);

                    CNCLog cncLog4 = new CNCLog();
                    cncLog4.setTime(time);
                    cncLog4.setOperation(CNC.EJECT_FROM_FIRST_STEP_CNC);
                    cncLog4.setIndex(cncIndex);
                    cncLog4.setIsFirstStepCNC(false);
                    cncLogs.get(cncIndex).add(cncLog4);
                }
                cnc.setNProducts(cnc.getNProducts() + 1);
                hasIntermediateProductOnHand = false;

                break;
            case CNC.EJECT_AND_WASH:
                //此时该CNC已完成处理中间产品
                if(hasIntermediateProductOnHand) {
                    cnc.setNextStep(CNC.FINISH_PROCESSING_SECOND_TIME);
                    cnc.setProcessRemainingTime(CNC.PROCESS_FOR_TWO_SECOND_STEP);

                    CNCLog cncLog5 = new CNCLog();
                    cncLog5.setTime(time);
                    cncLog5.setOperation(CNC.EJECT_AND_WASH);
                    cncLog5.setIndex(cncIndex);
                    cncLog5.setIsFirstStepCNC(false);
                    cncLogs.get(cncIndex).add(cncLog5);

                    CNCLog cncLogGive3 = new CNCLog();
                    if(cncIndex >= Constraint.CNCS_COUNT_ONE_ROW) {
                        cncLogGive3.setTime(time - RGV.GIVE_OR_EJECT_SOMETHING_TO_SLOW_CNC);
                    } else {
                        cncLogGive3.setTime(time - RGV.GIVE_OR_EJECT_SOMETHING_TO_FAST_CNC);
                    }
                    cncLogGive3.setOperation(CNC.GIVE_SOMETHING_SECOND_TIME);
                    cncLogGive3.setIndex(cncIndex);
                    cncLogGive3.setIsFirstStepCNC(false);
                    cncLogs.get(cncIndex).add(cncLogGive3);
                } else {
                    cnc.setNextStep(CNC.GIVE_SOMETHING_SECOND_TIME);
                    cnc.setProcessRemainingTime(0);

                    CNCLog cncLog5 = new CNCLog();
                    cncLog5.setTime(time);
                    cncLog5.setOperation(CNC.EJECT_AND_WASH);
                    cncLog5.setIndex(cncIndex);
                    cncLog5.setIsFirstStepCNC(false);
                    cncLogs.get(cncIndex).add(cncLog5);
                }
                cnc.setNProducts(cnc.getNProducts() + 1);
                hasIntermediateProductOnHand = false;
        }
    }
}
