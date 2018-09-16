import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
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
        try {
            makeStartExcel(allLogs);
            makeFinishExcel(allLogs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] startColumns = {"加工CNC编号", "上料开始时间"};

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
        for(int i = 0; i < startColumns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(startColumns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Cell Style for formatting Date
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

        // Create Other rows and cells with data
        int rowNum = 1;
        for(CNCLog cncLog: allCNCLogs) {
            if(cncLog.getOperation() == CNC.GIVE_SOMETHING) {
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

        // Resize all startColumns to fit the content size
        for(int i = 0; i < startColumns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("P1_G3_Start.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }

    private static String[] finishColumns = {"加工CNC编号", "下料开始时间"};

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
        for(int i = 0; i < finishColumns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(finishColumns[i]);
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

        // Resize all finishColumns to fit the content size
        for(int i = 0; i < finishColumns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("P1_G3_Finish.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }
}
