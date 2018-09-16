import java.util.ArrayList;

public class Start {
    public static void main(String[] args) {
        CNC[] CNCs = new CNC[Constraint.CNCS_COUNT_ONE_ROW * 2];
        for(int i = 0; i < Constraint.CNCS_COUNT_ONE_ROW * 2; i++) {
            CNCs[i] = new CNC();
        }

        int remainingTime = Constraint.SHIFT_TIME;
        int nProducts = 0;

        ArrayList<ArrayList<CNCLog>> cncLogs = new ArrayList<>();
        for(int i = 0; i < Constraint.CNCS_COUNT_ONE_ROW * 2; i++) {
            cncLogs.add(new ArrayList<>());
        }

        RGV rgv = new RGV(cncLogs);

        while(remainingTime > 0) {
            remainingTime = rgv.process(CNCs, remainingTime);
        }

        for (CNC CNC : CNCs) {
            nProducts += CNC.getNProducts();
        }

        ArrayList<CNCLog> allLogs = new ArrayList<>();
        for(ArrayList<CNCLog> cncLogs1 : cncLogs) {
            allLogs.addAll(cncLogs1);
        }

        allLogs.sort((cncLog, t1) -> Integer.compare(t1.getTime(), cncLog.getTime()));

        for (CNCLog cncLog : allLogs) {
            String operation = "";
            switch (cncLog.getOperation()) {
                case CNC.GIVE_SOMETHING:
                    operation = "Up料";
                    break;
                case CNC.FINISH_PROCESSING:
                case CNC.EJECT_AND_WASH:
                    operation = "Down料";
                    break;
                case CNC.BREAK:
                    operation = "损坏";
                    break;
                case CNC.REPAIRED:
                    operation = "已修复";
                    break;
            }

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

            System.out.println("时间: " + hour + ":" + formattedMinute + ":" + formattedSecond
                    + " CNC位置: " + position + " 操作: " + operation);
        }
        System.out.println(nProducts);
    }
}
