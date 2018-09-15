public class Start {
    public static void main(String[] args) {
        CNC[] oddCNCs = new CNC[4];
        CNC[] evenCNCs = new CNC[4];

        oddCNCs[0] = new CNC();
        oddCNCs[1] = new CNC();
        oddCNCs[2] = new CNC();
        oddCNCs[3] = new CNC();

        evenCNCs[0] = new CNC();
        evenCNCs[1] = new CNC();
        evenCNCs[2] = new CNC();
        evenCNCs[3] = new CNC();

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
