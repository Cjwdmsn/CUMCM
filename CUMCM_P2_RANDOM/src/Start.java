import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Start {
    public static void main(String[] args) {
        ArrayList<Integer> CNCsCount = new ArrayList<>();
        ArrayList<ArrayList<Integer>> allArrangements = new ArrayList<>();
        Map<ArrayList<Integer>, Integer> allArrangementResults = new HashMap<>();

        for(int i = 0; i < Constraint.CNCS_COUNT_ONE_ROW * 2; i++) {
            CNCsCount.add(i);
        }

        for(int i = 0; i < Constraint.CNCS_COUNT_ONE_ROW * 2; i++) {
            generateAllStepTwoCNCsArrangement(CNCsCount, new ArrayList<>(), allArrangements, allArrangementResults, i);
        }

        ArrayList<ArrayList<Integer>> optimizedArrangements = new ArrayList<>();
        int maxProductsCount = -1;
        for(ArrayList<Integer> arrayLists : allArrangements) {
            if(allArrangementResults.get(arrayLists) > maxProductsCount) {
                optimizedArrangements.clear();
                optimizedArrangements.add(arrayLists);
                maxProductsCount = allArrangementResults.get(arrayLists);
            } else if(allArrangementResults.get(arrayLists) == maxProductsCount) {
                optimizedArrangements.add(arrayLists);
            }
        }

        System.out.println("\nOptimized step two CNCs arrangement: ");
        for(ArrayList<Integer> arrayList : optimizedArrangements) {
            for(Integer i : arrayList) {
                System.out.print(i + " ");
            }
            System.out.print(": " + allArrangementResults.get(arrayList));
            System.out.println();
        }
    }

    private static CNC[] createCNCs(ArrayList<Integer> secondStepCNCIndexes) {
        CNC[] cncs = new CNC[8];
        for (int secondStepCNCIndex : secondStepCNCIndexes) {
            CNC cnc = new CNC(CNC.GIVE_SOMETHING_SECOND_TIME);
            cnc.setForFirstStep(false);
            cncs[secondStepCNCIndex] = cnc;
        }
        for(int i = 0; i < cncs.length; i++) {
            if(cncs[i] == null) {
                CNC cnc = new CNC(CNC.GIVE_SOMETHING_FIRST_TIME);
                cnc.setForFirstStep(true);
                cncs[i] = cnc;
            }
        }

        return cncs;
    }

    private static void generateAllStepTwoCNCsArrangement(ArrayList<Integer> CNCsIndexes, ArrayList<Integer> stepTwoCNCsArrangement,
                                                          ArrayList<ArrayList<Integer>> allArangements,
                                                          Map<ArrayList<Integer>, Integer> allArrangementResults,
                                                          int stepTwoCNCsCount) {
        ArrayList<Integer> identicalCNCsIndexes;
        ArrayList<Integer> identicalStepTwoCNCsArrangement;

        if(stepTwoCNCsArrangement.size() == stepTwoCNCsCount) {
            CNC[] CNCs = createCNCs(stepTwoCNCsArrangement);

            int remainingTime = Constraint.SHIFT_TIME;
            int nProducts = 0;
            RGV rgv = new RGV();

            while(remainingTime > 0) {
                remainingTime = rgv.process(CNCs, remainingTime);
            }

            System.out.print("Arrangement: ");
            for(Integer i : stepTwoCNCsArrangement) {
                System.out.print(i + " ");
            }

            System.out.print(": ");

            for (CNC CNC : CNCs) {
                if (!CNC.isForFirstStep()) {
                    nProducts += CNC.getNProducts();
                }
            }

            allArangements.add(stepTwoCNCsArrangement);
            allArrangementResults.put(stepTwoCNCsArrangement, nProducts);

            System.out.print(nProducts);
            System.out.println();
        } else {
            for(int i = 0; i < CNCsIndexes.size(); i++) {
                identicalCNCsIndexes = new ArrayList<>(CNCsIndexes);
                identicalStepTwoCNCsArrangement = new ArrayList<>(stepTwoCNCsArrangement);

                identicalStepTwoCNCsArrangement.add(identicalCNCsIndexes.get(i));

                for(int j = i; j >=  0; j--) {
                    identicalCNCsIndexes.remove(j);
                }

                generateAllStepTwoCNCsArrangement(identicalCNCsIndexes, identicalStepTwoCNCsArrangement,
                        allArangements, allArrangementResults, stepTwoCNCsCount);
            }
        }
    }
}
