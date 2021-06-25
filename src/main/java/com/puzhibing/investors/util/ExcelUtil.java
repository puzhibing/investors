package com.puzhibing.investors.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定义Excel工具类
 */
@Component
public class ExcelUtil {


    public List<List<List<String>>> upload(MultipartFile file){
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取文件名
        String fileName=file.getOriginalFilename();
        List<List<List<String>>> list = null;
        if(validateExcel(fileName)) {
            // 根据版本选择创建Workbook的方式
            Workbook wb = null;
            // 根据文件名判断文件是2003版本还是2007版本
            if (isExcel2007(fileName)) {
                try {
                    wb = new XSSFWorkbook(inputStream);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                try {
                    wb = new HSSFWorkbook(inputStream);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            int num = wb.getNumberOfSheets();//获取页数
            list = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                List<List<String>> sheetList = new ArrayList<>();
                //获取每一页对象
                Sheet sheet = wb.getSheetAt(i);
                // 得到Excel的行数
                int totalRows = sheet.getPhysicalNumberOfRows();
                for (int j = 0; j < totalRows; j++) {
                    if (j == 0) {
                        continue;// 标题行
                    }
                    Row row = sheet.getRow(j);// 获取索引为i的行数据
                    if(null == row){
                        continue;
                    }
                    int index = sheet.getRow(0).getPhysicalNumberOfCells();//获取标题的列数用于遍历
                    List<String> strings = new ArrayList<>();
                    int in = 0;//用于遍历单元格判断该行是否全为空值
                    for (int k = 0; k < index; k++) {//遍历获取每个单元格的数据
                        String str = null;
                        Cell cell = row.getCell(k);
                        if(cell == null) {
                            str = "";
                            in++;
                        }else {
                            switch (cell.getCellType()) {//判断数据类型取值
                                case NUMERIC :
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        Date theDate = cell.getDateCellValue();
                                        str = String.valueOf(theDate.getTime());
                                    }else{
                                        String string = String.valueOf(cell.getNumericCellValue());
                                        str = string.substring(0, string.indexOf("."));
                                    }

                                    break;
                                case STRING :
                                    str = cell.getStringCellValue();
                                    if(str == null) {
                                        str = "";
                                        in++;
                                    }
                                    break;
                                case _NONE :
                                    System.err.println("_NONE");
                                    break;
                                case FORMULA :
                                    System.err.println("FORMULA");
                                    break;
                                case BLANK :
                                    str = cell.getStringCellValue();
                                    if(str.equals("")) {
                                        str = "";
                                        in++;
                                    }
                                    break;
                                case BOOLEAN :
                                    System.err.println("BOOLEAN");
                                    break;
                                case ERROR :
                                    System.err.println("ERROR");
                                    break;
                                default:
                                    break;
                            }
                        }
                        strings.add(String.valueOf(str).trim());
                    }

                    if(in != index) {//判断如果每个单元格都为null则不需要添加到集合中
                        sheetList.add(strings);
                    }
                }
                list.add(sheetList);
            }


        }

        return list;
    }



    // @描述：是否是2003的excel，返回true是2003
    public static boolean isExcel2003(String filePath)  {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }


    //@描述：是否是2007的excel，返回true是2007
    public static boolean isExcel2007(String filePath)  {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }


    /**
     * 验证EXCEL文件
     * @param filePath
     * @return
     */
    public static boolean validateExcel(String filePath){
        if (filePath == null || !(isExcel2003(filePath) || isExcel2007(filePath))){
            return false;
        }
        return true;
    }


    /**
     * 将数据写入Excel中
     * @param titles    标题
     * @param datas     数据
     * @return
     */
    public HSSFWorkbook writeDataToExcel(List<List<String>> titles, List<List<List<String>>> datas) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        for(int i = 0; i < titles.size(); i++){
            HSSFSheet hssfSheet = hssfWorkbook.createSheet();
            hssfSheet.setColumnWidth(0, 6 * 256);
            hssfSheet.setDefaultRowHeightInPoints(20f);

            HSSFRow hssfRow = hssfSheet.createRow(0);//设置第一行数据（标题）
            HSSFCellStyle style = hssfWorkbook.createCellStyle();
            HSSFFont font = hssfWorkbook.createFont();
            font.setBold(true);
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            for (int l = 0; l < titles.get(i).size(); l++) {
                HSSFCell hssfCell = hssfRow.createCell(l);
                hssfCell.setCellType(CellType.STRING);//设置表格类型
                hssfCell.setCellValue(titles.get(i).get(l));
                hssfCell.setCellStyle(style);
                if(l > 0) {
                    hssfSheet.setColumnWidth(l , 20 * 256);
                }

            }

            //将数据添加到表格中
            List<String> data = null;
            for (int l = 0; l < datas.get(i).size(); l++) {
                hssfRow = hssfSheet.createRow(l + 1);
                data = datas.get(i).get(l);
                for (int j = 0; j < data.size(); j++) {
                    HSSFCell hssfCell = hssfRow.createCell(j);
                    hssfCell.setCellType(CellType.STRING);//设置表格类型
                    hssfCell.setCellValue(data.get(j));
                }
            }
        }
        return hssfWorkbook;
    }
}
