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
     */
    public void write(String fileName, String content){
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        try {
            File file = new File(FILE_PATH + fileName);
            file.createNewFile();
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null != bufferedWriter){
                    bufferedWriter.close();
                }
                if(null != fileWriter){
                    fileWriter.close();
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


    /**
     * 删除文件或文件夹
     * @param fileName
     */
    public void remove(String fileName){
        File file = new File(FILE_PATH + fileName);
        if(file.exists()){
            file.delete();
        }
    }


    /**
     * 获取文件目录下文件的数量
     * @return
     */
    public int findFileCount(){
        File file = new File(FILE_PATH);
        File[] files = file.listFiles();
        int count = 0;
        for(File f : files){
            if(f.isFile()){
                count++;
            }
        }
        return count;
    }
}
