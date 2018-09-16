import java.util.ArrayList;
import java.util.Comparator;

public class Start {
    public static void main(String[] args) {
        //设计一种第二步操作所需所有CNC的位置情况
        ArrayList<Integer> stepTwoCNCsArrangement = new ArrayList<>();
        stepTwoCNCsArrangement.add(1);
        stepTwoCNCsArrangement.add(3);
        stepTwoCNCsArrangement.add(5);

        //根据该情况来初始化所有CNC
        CNC[] CNCs = createCNCs(stepTwoCNCsArrangement);

        //设置剩余时间为该班次的时间
        int remainingTime = Constraint.SHIFT_TIME;
        //初始化生成的所有产品
        int nProducts = 0;

        ArrayList<ArrayList<CNCLog>> cncLogs = new ArrayList<>();
        for(int i = 0; i < Constraint.CNCS_COUNT_ONE_ROW * 2; i++) {
            cncLogs.add(new ArrayList<>());
        }

        RGV rgv = new RGV(cncLogs);

        //计算生成的产品数目
        while(remainingTime > 0) {
            remainingTime = rgv.process(CNCs, remainingTime);
        }

        //显示出该情况中所有操作第二步所需的CNC的位置
        System.out.print("Arrangement: ");
        for(Integer i : stepTwoCNCsArrangement) {
            System.out.print(i + " ");
        }

        System.out.print(": ");

        //计算生成的产品数目
        for (CNC CNC : CNCs) {
            if (!CNC.isForFirstStep()) {
                nProducts += CNC.getNProducts();
            }
        }

        //显示生成的产品数目
        System.out.print(nProducts);
        System.out.println();

        ArrayList<CNCLog> allLogs = new ArrayList<>();
        for(ArrayList<CNCLog> cncLogs1 : cncLogs) {
            allLogs.addAll(cncLogs1);
        }

        allLogs.sort(new Comparator<CNCLog>() {
            @Override
            public int compare(CNCLog cncLog, CNCLog t1) {
                return Integer.compare(t1.getTime(), cncLog.getTime());
            }
        });

        for (CNCLog cncLog : allLogs) {
            String operation = "";
            switch (cncLog.getOperation()) {
                case CNC.GIVE_SOMETHING_FIRST_TIME:
                    operation = "Up料";
                    break;
                case CNC.FINISH_PROCESSING_FIRST_TIME:
                case CNC.EJECT_FROM_FIRST_STEP_CNC:
                    operation = "Down料";
                    break;
                case CNC.GIVE_SOMETHING_SECOND_TIME:
                    operation = "Up料";
                    break;
                case CNC.FINISH_PROCESSING_SECOND_TIME:
                case CNC.EJECT_AND_WASH:
                    operation = "Down料";
                    break;
            }

            String CNCType = cncLog.isFirstStepCNC() ? " 第1步操作所需CNC" : " 第2步操作所需CNC";
            int position;
            if(cncLog.getIndex() < Constraint.CNCS_COUNT_ONE_ROW) {
                position = cncLog.getIndex() * 2 + 1;
            } else {
                position = (cncLog.getIndex() - Constraint.CNCS_COUNT_ONE_ROW + 1) * 2;
            }

            int hour = 0;
            int minute;
            int second;
            int rawTime = Constraint.SHIFT_TIME - cncLog.getTime();

            second = rawTime % 60;
            minute = (rawTime - second) / 60;
            if(minute >= 60) {
                hour = minute / 60;
                minute = minute % 60;
            }

            String formattedMinute = String.format("%02d", minute);
            String formattedSecond = String.format("%02d", second);

            System.out.println("时间: " + hour + ":" + formattedMinute + ":" + formattedSecond + CNCType
                    + " CNC位置: " + position + " 操作: " + operation);
        }
    }

    //根据生成的操作第二步的所有CNC的位置情况来初始化所有CNC
    private static CNC[] createCNCs(ArrayList<Integer> secondStepCNCIndexes) {
        CNC[] cncs = new CNC[8];
        for (int secondStepCNCIndex : secondStepCNCIndexes) {
            //初始化此类CNC为操作第二步所需的CNC
            CNC cnc = new CNC(CNC.GIVE_SOMETHING_SECOND_TIME);
            cnc.setForFirstStep(false);

            int actualIndex;
            if(secondStepCNCIndex % 2 != 0) {
                actualIndex = secondStepCNCIndex / 2;
            } else {
                actualIndex = (secondStepCNCIndex - 1) / 2;
            }
            cncs[actualIndex] = cnc;
        }
        for(int i = 0; i < cncs.length; i++) {
            //初始化此类CNC为操作第一步所需的CNC
            if(cncs[i] == null) {
                CNC cnc = new CNC(CNC.GIVE_SOMETHING_FIRST_TIME);
                cnc.setForFirstStep(true);
                cncs[i] = cnc;
            }
        }

        //返回该情况下所有的CNC
        return cncs;
    }
}
