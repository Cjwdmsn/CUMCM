import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
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

        try {
            makeStartExcel(allLogs);
            makeStepOneFinishExcel(allLogs);
            makeStepTwoFinishExcel(allLogs);
            makeFinishExcel(allLogs);
        } catch (IOException e) {
            e.printStackTrace();
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

    private static String[] stepOneStartColumns = {"工序1的CNC编号", "上料开始时间"};

    private static void makeStartExcel(ArrayList<CNCLog> allCNCLogs) throws IOException {
        // Create a Workbook
        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        /* CreationHelper helps us create instances of various things like DataFormat,
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
        CreationHelper createHelper = workbook.getCreationHelper();

        // Create a Sheet
        Sheet sheet = workbook.createSheet("Start");

        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        // Create cells
        for(int i = 0; i < stepOneStartColumns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(stepOneStartColumns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Cell Style for formatting Date
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

        // Create Other rows and cells with data
        int rowNum = 1;
        for(CNCLog cncLog: allCNCLogs) {
            if(cncLog.getOperation() == CNC.GIVE_SOMETHING_FIRST_TIME) {
                Row row = sheet.createRow(rowNum++);

                int position;
                if(cncLog.getIndex() < Constraint.CNCS_COUNT_ONE_ROW) {
                    position = cncLog.getIndex() * 2 + 1;
                } else {
                    position = (cncLog.getIndex() - Constraint.CNCS_COUNT_ONE_ROW + 1) * 2;
                }

                row.createCell(0)
                        .setCellValue(position);

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

                row.createCell(1)
                        .setCellValue(hour + ":" + formattedMinute + ":" + formattedSecond);
            }
        }

        // Resize all stepOneStartColumns to fit the content size
        for(int i = 0; i < stepOneStartColumns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("P2_G3_Step_One_Start.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }

    private static String[] stepOneFinishColumns = {"工序1的CNC编号", "下料开始时间"};

    private static void makeStepOneFinishExcel(ArrayList<CNCLog> allCNCLogs) throws IOException {
        // Create a Workbook
        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        /* CreationHelper helps us create instances of various things like DataFormat,
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
        CreationHelper createHelper = workbook.getCreationHelper();

        // Create a Sheet
        Sheet sheet = workbook.createSheet("Step one finish");

        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        // Create cells
        for(int i = 0; i < stepOneFinishColumns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(stepOneFinishColumns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Cell Style for formatting Date
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

        // Create Other rows and cells with data
        int rowNum = 1;
        for(CNCLog cncLog: allCNCLogs) {
            if(cncLog.getOperation() == CNC.EJECT_FROM_FIRST_STEP_CNC) {
                Row row = sheet.createRow(rowNum++);

                int position;
                if(cncLog.getIndex() < Constraint.CNCS_COUNT_ONE_ROW) {
                    position = cncLog.getIndex() * 2 + 1;
                } else {
                    position = (cncLog.getIndex() - Constraint.CNCS_COUNT_ONE_ROW + 1) * 2;
                }

                row.createCell(0)
                        .setCellValue(position);

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

                row.createCell(1)
                        .setCellValue(hour + ":" + formattedMinute + ":" + formattedSecond);
            }
        }

        // Resize all stepOneStartColumns to fit the content size
        for(int i = 0; i < stepOneFinishColumns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("P2_G3_Step_One_Finish.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }

    private static String[] stepTwoStartColumns = {"工序2的CNC编号", "上料开始时间"};

    private static void makeFinishExcel(ArrayList<CNCLog> allCNCLogs) throws IOException {
        // Create a Workbook
        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        /* CreationHelper helps us create instances of various things like DataFormat,
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
        CreationHelper createHelper = workbook.getCreationHelper();

        // Create a Sheet
        Sheet sheet = workbook.createSheet("Finish");

        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        // Create cells
        for(int i = 0; i < stepTwoStartColumns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(stepTwoStartColumns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Cell Style for formatting Date
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

        // Create Other rows and cells with data
        int rowNum = 1;
        for(CNCLog cncLog: allCNCLogs) {
            if(cncLog.getOperation() == CNC.GIVE_SOMETHING_SECOND_TIME) {
                Row row = sheet.createRow(rowNum++);

                int position;
                if(cncLog.getIndex() < Constraint.CNCS_COUNT_ONE_ROW) {
                    position = cncLog.getIndex() * 2 + 1;
                } else {
                    position = (cncLog.getIndex() - Constraint.CNCS_COUNT_ONE_ROW + 1) * 2;
                }

                row.createCell(0)
                        .setCellValue(position);

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

                row.createCell(1)
                        .setCellValue(hour + ":" + formattedMinute + ":" + formattedSecond);
            }
        }

        // Resize all stepTwoStartColumns to fit the content size
        for(int i = 0; i < stepTwoStartColumns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("P2_G3_Step_Two_Start.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }

    private static String[] stepTwofinishColumns = {"工序2的CNC编号", "下料开始时间"};

    private static void makeStepTwoFinishExcel(ArrayList<CNCLog> allCNCLogs) throws IOException {
        // Create a Workbook
        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        /* CreationHelper helps us create instances of various things like DataFormat,
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
        CreationHelper createHelper = workbook.getCreationHelper();

        // Create a Sheet
        Sheet sheet = workbook.createSheet("Finish");

        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        // Create cells
        for(int i = 0; i < stepTwofinishColumns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(stepTwofinishColumns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Cell Style for formatting Date
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

        // Create Other rows and cells with data
        int rowNum = 1;
        for(CNCLog cncLog: allCNCLogs) {
            if(cncLog.getOperation() == CNC.EJECT_AND_WASH) {
                Row row = sheet.createRow(rowNum++);

                int position;
                if(cncLog.getIndex() < Constraint.CNCS_COUNT_ONE_ROW) {
                    position = cncLog.getIndex() * 2 + 1;
                } else {
                    position = (cncLog.getIndex() - Constraint.CNCS_COUNT_ONE_ROW + 1) * 2;
                }

                row.createCell(0)
                        .setCellValue(position);

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

                row.createCell(1)
                        .setCellValue(hour + ":" + formattedMinute + ":" + formattedSecond);
            }
        }

        // Resize all stepTwoFinishColumns to fit the content size
        for(int i = 0; i < stepTwofinishColumns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("P2_G3_Step_Two_Finish.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }
}
