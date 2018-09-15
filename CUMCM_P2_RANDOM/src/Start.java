public class Start {
    public static void main(String[] args) {
        for(int i = 0; i < Constraint.CNCS_COUNT_ONE_ROW * 2; i++) {
            for(int j = i; j < Constraint.CNCS_COUNT_ONE_ROW * 2; j++) {
                for(int k = 1; k < Constraint.CNCS_COUNT_ONE_ROW * 2; k++) {
                    int[] secondStepCNCIndexs = new int[k];
                    for(int l = 0; l < k; l++) {
                        secondStepCNCIndexs[l] = j + l;
                    }

                    CNC[] CNCs = createCNCs(secondStepCNCIndexs);

                    int remainingTime = Constraint.SHIFT_TIME;
                    int nProducts = 0;
                    RGV rgv = new RGV();

                    while(remainingTime > 0) {
                        remainingTime = rgv.process(CNCs, remainingTime);
                    }

                    for (CNC CNC : CNCs) {
                        if (!CNC.isForFirstStep()) {
                            nProducts += CNC.getNProducts();
                        }
                    }

                    for(Integer m : secondStepCNCIndexs) {
                        System.out.print(m + " ");
                    }
                    System.out.print(": ");
                    System.out.print(nProducts);
                    System.out.println();
                }
            }
        }

        /*CNC[] CNCs = createCNCs(new int[]{0, 1, 2, 3, 4, 5, 6});

        int remainingTime = Constraint.SHIFT_TIME;
        int nProducts = 0;
        RGV rgv = new RGV();

        while(remainingTime > 0) {
            remainingTime = rgv.process(CNCs, remainingTime);
        }

        for (CNC CNC : CNCs) {
            if (!CNC.isForFirstStep()) {
                nProducts += CNC.getNProducts();
            }
        }

        System.out.println(nProducts);*/
    }

    private static CNC[] createCNCs(int[] secondStepCNCIndexes) {
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
}
