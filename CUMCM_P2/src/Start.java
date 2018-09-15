public class Start {
    public static void main(String[] args) {
        CNC[] oddCNCs = new CNC[4];
        CNC[] evenCNCs = new CNC[4];

        oddCNCs[0] = new CNC(CNC.GIVE_SOMETHING_FIRST_TIME);
        oddCNCs[1] = new CNC(CNC.GIVE_SOMETHING_FIRST_TIME);
        oddCNCs[2] = new CNC(CNC.GIVE_SOMETHING_FIRST_TIME);
        oddCNCs[3] = new CNC(CNC.GIVE_SOMETHING_FIRST_TIME);

        evenCNCs[0] = new CNC(CNC.GIVE_SOMETHING_SECOND_TIME);
        evenCNCs[1] = new CNC(CNC.GIVE_SOMETHING_SECOND_TIME);
        evenCNCs[2] = new CNC(CNC.GIVE_SOMETHING_SECOND_TIME);
        evenCNCs[3] = new CNC(CNC.GIVE_SOMETHING_SECOND_TIME);

        int remainingTime = Constraint.SHIFT_TIME;
        int nProducts = 0;
        RGV rgv = new RGV();

        while(remainingTime > 0) {
            remainingTime = rgv.process(oddCNCs, evenCNCs, remainingTime);
        }

        for(int i = 0; i < oddCNCs.length; i++) {
            nProducts += oddCNCs[i].getNProducts();
            nProducts += evenCNCs[i].getNProducts();
        }

        System.out.println(nProducts);
    }
}
