package telb1;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import telb1.dbl.DbManager;
import telb1.dbl.WashingModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by User on 13.03.2018.
 */
public class ReportBuilder {
    public ByteArrayOutputStream build(String messageText) throws IOException {
        YearMonth yearMonth = YearMonth.parse(messageText, DateTimeFormat.forPattern("MM/yy"));
        List<WashingModel> washings = DbManager.INSTANCE.getMonthReport(yearMonth);
        Workbook wb = new XSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet("new sheet");
        sheet.autoSizeColumn(0);
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
            row.createCell(4).setCellValue(washing.getSmena());
            row.createCell(5).setCellValue(washing.getChatId());
            i++;
        }
        //FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out;
    }
}
