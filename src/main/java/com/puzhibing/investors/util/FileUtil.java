package com.puzhibing.investors.util;

import org.springframework.stereotype.Component;

import java.io.*;

/**
 * 文件工具类
 */
@Component
public class FileUtil {

    private final String FILE_PATH = "C:\\investorsFiles\\";

    private final String CHARSET = "UTF-8";


    /**
     * 写入内容到文件中
     * @param content   写入内容
     * @param fileName  文件名称
     * @param append    是否追加写入
     */
    public void write(String fileName, String content, Boolean append){
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(FILE_PATH + fileName);
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file, append);
            fileOutputStream.write(content.getBytes(CHARSET));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null != fileOutputStream){
                    fileOutputStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取文件内容
     * @param fileName
     * @return
     */
    public String read(String fileName){
        StringBuffer sb = new StringBuffer();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            File file = new File(FILE_PATH + fileName);
            if(file.exists()){
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                String str = "";
                while ((str = bufferedReader.readLine()) != null){
                    sb.append(str);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(null != fileReader){
                    fileReader.close();
                }
                if(null != bufferedReader){
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    public void remove(String fileName){
        File file = new File(FILE_PATH + fileName);
        if(file.exists()){
            file.delete();
        }
    }
}
