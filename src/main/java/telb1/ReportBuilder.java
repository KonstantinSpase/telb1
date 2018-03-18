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
        //List<WashingModel> fzSumm = DbManager.INSTANCE.getFzMonthReport(yearMonth);
        Workbook wb = new XSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet("new sheet");
        Sheet sheetFz = wb.createSheet("Суммы по FZ");

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
            //row.createCell(4).setCellValue(washing.getWasherName());

            i++;
        }
        for(int columnIndex = 0; columnIndex < 5; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
        }




        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out;
    }
}
