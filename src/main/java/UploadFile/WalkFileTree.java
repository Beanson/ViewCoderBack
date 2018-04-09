package UploadFile;


import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * Created by Administrator on 2016/10/30.
 */
public class WalkFileTree {
//private static final Logger logger = LoggerFactory.getLogger(WalkFileTree.class);

//    public static void main(String[] args) {
//      List<String> list=getDirectoryFiles(Paths.get("F:","XAMPP","htdocs","CourseSupport","Inserted","haha"));
//      list.forEach(System.out::println);
//
//        System.out.println("1");
//        DefaultOfficeManagerConfiguration configuration  = new DefaultOfficeManagerConfiguration();
//        configuration.setOfficeHome(OFFICE_HOME);//设置OpenOffice.org安装目录
//        System.out.println("2");
//        configuration.setPortNumbers(8099); //设置转换端口，默认为8100
//        System.out.println("3");
//        configuration.setTaskExecutionTimeout(1000 * 60 * 5L);//设置任务执行超时为5分钟
//        //configuration.setTaskExecutionTimeout(5000);//设置任务执行超时为5分钟
//        System.out.println("4");
//        configuration.setTaskQueueTimeout(1000 * 60 * 60 * 24L);//设置任务队列超时为24小时
//        System.out.println("5");
//        officeManager = configuration.buildOfficeManager();
//        System.out.println("6");
//        officeManager.start();
//        System.out.println("7");
//
//        multiThreadConvert();
////
////
////        Path path = Paths.get("F:\\XAMPP\\htdocs\\CourseSupport\\Inserted\\2016_ftp");
////        try {
////            changeToPdf(path);
////        } catch (Exception e) {
////            officeManager.stop();
////            e.printStackTrace();
////        }
////
//          officeManager.stop();
////        System.out.println("congratulation finish~");
//    }

    public static List<String> getDirectoryFiles(Path path) {
        //Path path=Paths.get("E:","Inserted",subPath);
        final List<String> fileNames = new ArrayList<String>();
        try {
            Files.walkFileTree(path, Collections.<FileVisitOption>emptySet(), 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    fileNames.add(file.getFileName().toString());
                    //System.out.println("path:"+path);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            //logger.error("An error occurred while walking directory: " + path, e);
            System.out.println("An error occurred while walking directory: " + path + e);
        }
        return fileNames;
    }
}