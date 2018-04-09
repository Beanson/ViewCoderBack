package UploadFile;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/5.
 */
public class ConvertToPdf {

    private static String OFFICE_HOME = "F:\\AllPattern\\module\\JODConverter\\OpenOffice3.41\\software";
    private static OfficeManager officeManager;

    static {
        System.out.println("1");
        DefaultOfficeManagerConfiguration configuration  = new DefaultOfficeManagerConfiguration();
        configuration.setOfficeHome(OFFICE_HOME);//设置OpenOffice.org安装目录
        System.out.println("2");
        configuration.setPortNumbers(8099); //设置转换端口，默认为8100
        System.out.println("3");
        configuration.setTaskExecutionTimeout(1000 * 60 * 5L);//设置任务执行超时为5分钟
        //configuration.setTaskExecutionTimeout(5000);//设置任务执行超时为5分钟
        System.out.println("4");
        configuration.setTaskQueueTimeout(1000 * 60 * 60 * 24L);//设置任务队列超时为24小时
        System.out.println("5");
        officeManager = configuration.buildOfficeManager();
        System.out.println("6");
        officeManager.start();
        System.out.println("7");
    }

    public static void changeFileToPdf(String inputFileBefore,String fileName){
        //reconstruct the prefix of the string to delete "2016_pdf" string
        //重构inputFile字符串，去除"2016_pdf"字段
        String inputFile="F:/XAMPP/htdocs/CourseSupport/ftp/"+inputFileBefore;
        String pdfFile = "F:/XAMPP/htdocs/CourseSupport/pdf/"+inputFileBefore+".pdf";
        String temp[]=fileName.split("\\.");
        String type=temp[temp.length-1];
        // if not the exe file, convert; else copy
        //如果不是可执行程序就转换成pdf，如果是则拷贝副本
        try{
            System.out.println("file type is: "+type);
            if(!(type.equals("exe")||type.equals("EXE")||type.equals("pdf")||type.equals("zip"))){
                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
                System.out.println("进行文档格式转换:" + inputFile + " --> " + pdfFile);
                converter.convert(new File(inputFile),new File(pdfFile));

            }else{
                try {
                    System.out.println("进行拷贝转换:" + inputFile + " --> " + pdfFile);
                    copyFile(new File(inputFile),new File(pdfFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();

        }finally {
//            if(officeManager.isRunning()){
//                officeManager.stop();
//            }
        }
    }


    /**
     * simulate multi thread environment to convert files to pdf
     * 模拟多线程情况下进行文件转换成pdf
     */
    public void multiThreadConvert(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                String inputFile="./src/main/java/UploadFile/test/01.jpg";
                String pdfFile="./src/main/java/UploadFile/result/01.pdf";
                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
                System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
                converter.convert(new File(inputFile),new File(pdfFile));
            }
        }).start();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String inputFile="./src/main/java/UploadFile/test/02.jpg";
//                String pdfFile="./src/main/java/UploadFile/result/02.pdf";
//                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
//                System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
//                converter.convert(new File(inputFile),new File(pdfFile));
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String inputFile="./src/main/java/UploadFile/test/03.jpg";
//                String pdfFile="./src/main/java/UploadFile/result/03.pdf";
//                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
//                System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
//                converter.convert(new File(inputFile),new File(pdfFile));
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String inputFile="./src/main/java/UploadFile/test/04.jpg";
//                String pdfFile="./src/main/java/UploadFile/result/04.pdf";
//                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
//                System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
//                converter.convert(new File(inputFile),new File(pdfFile));
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String inputFile="./src/main/java/UploadFile/test/05.jpg";
//                String pdfFile="./src/main/java/UploadFile/result/05.pdf";
//                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
//                System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
//                converter.convert(new File(inputFile),new File(pdfFile));
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String inputFile="./src/main/java/UploadFile/test/06.jpg";
//                String pdfFile="./src/main/java/UploadFile/result/06.pdf";
//                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
//                System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
//                converter.convert(new File(inputFile),new File(pdfFile));
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String inputFile="./src/main/java/UploadFile/test/07.txt";
//                String pdfFile="./src/main/java/UploadFile/result/07.pdf";
//                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
//                System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
//                converter.convert(new File(inputFile),new File(pdfFile));
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String inputFile="./src/main/java/UploadFile/test/08.docx";
//                String pdfFile="./src/main/java/UploadFile/result/08.pdf";
//                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
//                System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
//                converter.convert(new File(inputFile),new File(pdfFile));
//            }
//        }).start();
    }


    /**
     * change any file type to pdf
     * 把任何类型的文件转换成pdf格式
     * @param path
     * @return
     */
    public List<String> changeToPdf(Path path) {
        //Path path=Paths.get("E:","Inserted",subPath);
        String targetPath = "F:\\XAMPP\\htdocs\\CourseSupport\\Inserted\\2016_pdf";
        final List<String> fileNames = new ArrayList<String>();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    //fileNames.add(file.getFileName().toString());
                    String temp[] = path.relativize(file).toString().split("\\.");
                    String pathStr = targetPath + "\\" + path.relativize(file).toString() + ".pdf";
                    File newFile = new File(pathStr);
                    newFile.getParentFile().mkdirs();//have return value true or false

                    String inputFile = file.toString();
                    String pdfFile = pathStr;

                    if (temp[1].equals("pdf")) {
                        if(!new File(pdfFile).exists()){
                            System.out.println("*******************************************");
                            System.out.println("copy "+inputFile+"--->to--->"+pdfFile);
                            copyFile(new File(inputFile),new File(pdfFile));
                        }else{
                            return FileVisitResult.CONTINUE;
                        }

                    } else {
                        if(!new File(pdfFile).exists()){
                            System.out.println("------------------------------------------");
                            System.out.println(inputFile);
                            System.out.println(pdfFile);
                            OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
                            System.out.println("进行文档转换转换:" + inputFile + " --> " + pdfFile);
                            converter.convert(new File(inputFile),new File(pdfFile));

                        }else {
                            return FileVisitResult.CONTINUE;
                        }
                    }
                    //System.out.println(pathStr);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("An error occurred while walking directory: " + path + e);
        }
        return fileNames;
    }

    /**
     * Nio file copy improve the performance
     * 利用Nio来拷贝文件可以提升性能
     * @param sourceFile 源文件
     * @param destFile 目标文件
     * @throws IOException
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }

    }
}
