import java.util.ArrayList;
import java.util.HashMap;

public class Start {
    public static void main(String[] args) {
        //存储所有CNC
        ArrayList<Integer> CNCsCount = new ArrayList<>();
        //记录所有操作第二步的CNC的位置情况
        ArrayList<ArrayList<Integer>> allArrangements = new ArrayList<>();
        //存储所有操作第二步的CNC的位置情况及其生产的产品数目
        HashMap<ArrayList<Integer>, Integer> allArrangementResults = new HashMap<>();

        //添加所有CNC到变量CNCsCount
        for(int i = 0; i < Constraint.CNCS_COUNT_ONE_ROW * 2; i++) {
            CNCsCount.add(i);
        }

        //生成所有操作第二步的CNC的位置情况并且根据每个位置情况算出该情况下能生产的产品数目
        for(int i = 0; i < Constraint.CNCS_COUNT_ONE_ROW * 2; i++) {
            generateAllStepTwoCNCsArrangementAndRun(CNCsCount, new ArrayList<>(), allArrangements, allArrangementResults, i);
        }

        //以保存所有第二步操作所需的CNC的位置的形式存储所有生产最多产品数目的情况
        ArrayList<ArrayList<Integer>> optimizedArrangements = new ArrayList<>();
        //记录生产最多的产品数目
        int maxProductsCount = -1;
        //保存所有生产最多产品数目的情况至变量optimizedArrangements
        for(ArrayList<Integer> arrayLists : allArrangements) {
            if(allArrangementResults.get(arrayLists) > maxProductsCount) {
                optimizedArrangements.clear();
                optimizedArrangements.add(arrayLists);
                maxProductsCount = allArrangementResults.get(arrayLists);
            } else if(allArrangementResults.get(arrayLists) == maxProductsCount) {
                optimizedArrangements.add(arrayLists);
            }
        }

        //以显示所有第二步操作所需的CNC的位置的形式列出所有生产最多产品数目的情况及其所能生产的产品数目
        System.out.println("\nOptimized step two CNCs arrangement: ");
        for(ArrayList<Integer> arrayList : optimizedArrangements) {
            for(Integer i : arrayList) {
                System.out.print(i + " ");
            }
            System.out.print(": " + allArrangementResults.get(arrayList));
            System.out.println();
        }
    }

    //根据生成的操作第二步的所有CNC的位置情况来初始化所有CNC
    private static CNC[] createCNCs(ArrayList<Integer> secondStepCNCIndexes) {
        CNC[] cncs = new CNC[8];
        for (int secondStepCNCIndex : secondStepCNCIndexes) {
            //初始化此类CNC为操作第二步所需的CNC
            CNC cnc = new CNC(CNC.GIVE_SOMETHING_SECOND_TIME);
            cnc.setForFirstStep(false);
            cncs[secondStepCNCIndex] = cnc;
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

    //以递归的形式生成所有操作第二步所需的CNC的位置情况并计算该情况下能生产的产品数目
    private static void generateAllStepTwoCNCsArrangementAndRun(ArrayList<Integer> CNCsIndexes,
                                                                ArrayList<Integer> stepTwoCNCsArrangement,
                                                                ArrayList<ArrayList<Integer>> allArangements,
                                                                HashMap<ArrayList<Integer>, Integer> allArrangementResults,
                                                                int stepTwoCNCsCount) {
        ArrayList<Integer> identicalCNCsIndexes;
        ArrayList<Integer> identicalStepTwoCNCsArrangement;

        if(stepTwoCNCsArrangement.size() == stepTwoCNCsCount) {
            //计算完成一种情况

            //根据该情况来初始化所有CNC
            CNC[] CNCs = createCNCs(stepTwoCNCsArrangement);

            //设置剩余时间为该班次的时间
            int remainingTime = Constraint.SHIFT_TIME;
            //初始化生成的所有产品
            int nProducts = 0;

            RGV rgv = new RGV();

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

            //将该情况保存至变量allArangements
            allArangements.add(stepTwoCNCsArrangement);
            //将该情况及其生产的产品数目保存至变量allArrangementResults
            allArrangementResults.put(stepTwoCNCsArrangement, nProducts);

            //显示生成的产品数目
            System.out.print(nProducts);
            System.out.println();
        } else {
            //递归部分，生成操作第二步所需的CNC的位置
            for(int i = 0; i < CNCsIndexes.size(); i++) {
                identicalCNCsIndexes = new ArrayList<>(CNCsIndexes);
                identicalStepTwoCNCsArrangement = new ArrayList<>(stepTwoCNCsArrangement);

                identicalStepTwoCNCsArrangement.add(identicalCNCsIndexes.get(i));

                for(int j = i; j >=  0; j--) {
                    identicalCNCsIndexes.remove(j);
                }

                generateAllStepTwoCNCsArrangementAndRun(identicalCNCsIndexes, identicalStepTwoCNCsArrangement,
                        allArangements, allArrangementResults, stepTwoCNCsCount);
            }
        }
    }
}
