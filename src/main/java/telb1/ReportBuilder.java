package telb1;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import telb1.dbl.DbManager;
import telb1.dbl.WashingModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by User on 13.03.2018.
 */
public class ReportBuilder {
    public ByteArrayOutputStream build(String month) throws IOException {
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormat.forPattern("MM/yy"));
        List<WashingModel> washings = DbManager.INSTANCE.getMonthReport(yearMonth);
        Workbook wb = new XSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet("new sheet");

        CellStyle dataStyle = wb.createCellStyle();
        dataStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MM/yy hh:mm"));

        int i = 2;
        for (WashingModel washing : washings) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
                    cell.setCellValue(washing.getDateTime().toDate());
                    cell.setCellStyle(dataStyle);
            row.createCell(1).setCellValue(washing.getGosNomer());
            row.createCell(2).setCellValue(washing.getFz());
            row.createCell(3).setCellValue(washing.getPoint());

            i++;
        }
        for(int columnIndex = 0; columnIndex < 5; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
        }

        List<Integer[]> washingsByFz = DbManager.INSTANCE.getWashingsByFz(yearMonth);
        Sheet sheet1 = wb.createSheet("Суммы по ФЗ");

      /*  CellStyle dataStyle = wb.createCellStyle();
        dataStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MM/yy hh:mm"));*/

        i = 2;
        int washingPrice=Integer.parseInt(Config.INSTANCE.WASHING_PRICE);
        for (Integer [] line :washingsByFz) {
            Row row = sheet1.createRow(i);
            row.createCell(0).setCellValue(line[0]);
            row.createCell(1).setCellValue(line[1]*washingPrice);
            i++;
        }
        for(int columnIndex = 0; columnIndex < 2; columnIndex++) {
            sheet1.autoSizeColumn(columnIndex);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out;
    }
}
