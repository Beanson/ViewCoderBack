package FrontEnd.psd;

import FrontEnd.exceptions.project.PSDAnalysisException;
import FrontEnd.helper.common.Common;
import FrontEnd.helper.common.OssOpt;
import FrontEnd.helper.config.GlobalConfig;
import FrontEnd.myBatis.operation.common.CommonService;
import FrontEnd.myBatis.operation.project.CreateProject;
import FrontEnd.psd.entity.*;
import FrontEnd.psd.layer.PsdLayer;
import FrontEnd.psd.layer.PsdLayerType;
import FrontEnd.psd.parse.PSDParseDev;
import FrontEnd.psd.parse.PsdImage;
import com.aliyun.oss.OSSClient;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONException;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import org.apache.log4j.Logger;

import static FrontEnd.helper.common.Common.COMMON_BACKGROUND;

public class PsdAnalysis {
    private static Logger logger = Logger.getLogger(PsdAnalysis.class.getName());

    private int projectId, userId, totalWidth, totalHeight, maxId = 0, maxRate = 0;
    private static final String IMAGE_PNG_TYPE = "png";
    private PsdInfo psdInfo = new PsdInfo();
    private SqlSession sqlSession;
    private OSSClient ossClient;
    private String projectName;
    public File file;


    /**
     * 构造函数收录关键数据
     */
    public PsdAnalysis(int projectId, int userId, String projectName, SqlSession sqlSession,OSSClient ossClient) {
        this.projectId = projectId;
        this.userId = userId;
        this.projectName = projectName;
        this.sqlSession = sqlSession;
        this.ossClient=ossClient;
    }


    /**
     * 解析psd file 总调用方法
     */
    public void parse(File file) throws PSDAnalysisException {
        try {
            CommonService.calculateTime("Parse PSD file begin");
            processPsd(file);
            CommonService.calculateTime("Parse PSD file end");
        } catch (Exception e) {
            PsdAnalysis.logger.error("Parse PSD file occurs error", e);
            throw new PSDAnalysisException(e);
        }

    }


    /**
     * PSD 文件解析进度追踪
     *
     * @param inputFile 传入上传的文件File对象
     * @throws PSDAnalysisException
     */
    private void processPsd(File inputFile)
            throws PSDAnalysisException {
        try {
            //获取PSD文件对象整体信息
            PsdImage psdFile = new PsdImage(inputFile);
            totalWidth = psdFile.getWidth();
            totalHeight = psdFile.getHeight();
            maxRate = psdFile.getLayers().size();
            PsdAnalysis.logger.debug("===Parsing PSD File, total width and height is :" + totalHeight + ";" + totalWidth);

            //循环解析PSD文件中每个图层的信息
            for (int i = 0; i < maxRate; i++) {
                PsdLayer layer = psdFile.getLayer(i);
                writeLayer(layer, i + 1);
                //图层的number从1开始算起，所以需要i+1
                PsdAnalysis.logger.debug("=== Parse PSD layer: " + layer.getName() + " - " + ((i + 1) * 100 / maxRate) + "%");
            }

            //解析PSD图层完毕后，默认情况在前端显示在项目名为文件夹目录下进行上传文件
            if (psdInfo.getAll_tools().get(Common.COMMON_IMAGE).size() > 0) {
//                int emptyFolderInsertNum = CreateProject.insertWidgetToDB(projectId, userId, null, Common.FILE_TYPE_IMAGE,
//                        Common.FOLDER_FILE, null, null, projectName, "", null, null,
//                        Common.getDateTime(), sqlSession);
//                //对插入数据库影响的条目进行分析
//                if (emptyFolderInsertNum <= 0) {
//                    PsdAnalysis.logger.debug("===Insert empty folder to DB error, num is: " + emptyFolderInsertNum);
//                    throw new PSDAnalysisException("Insert empty folder to DB error: ");
//                }
            }

            //打印最终psd消息
            PsdAnalysis.logger.debug("===Final PSD parse layers info: ");
            for (int i = 0; i < maxRate; i++) {
                PsdAnalysis.logger.info("psd: " + i + " : " + psdFile.getLayer(i).getName() + " type:" + psdFile.getLayer(i).getType());
            }
        } catch (Exception e) {
            PsdAnalysis.logger.error("===Parse PSD file occurs error: ", e);
            throw new PSDAnalysisException(e);
        }
    }


    /**
     * 图层数据保存到list中，并把图片资源保存文件和写入数据
     *
     * @param layer PSD每个图层对象
     * @param num   PSD每个图层的rate，即用来设置为HTML的z-index
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws JSONException
     */
    private void writeLayer(PsdLayer layer, int num)
            throws IOException, ParserConfigurationException, JSONException {

        HashMap<String, Object> layer_info = new HashMap<>();
        LayerInfo.InitLayerBaseInfo(layer_info, layer, num);//填充基本数据
        maxId = layer.getLayerId() > maxId ? layer.getLayerId() : maxId; //更新至最大的max_id

        //NORMAL图层信息数据的填充
        if (layer.getType() == PsdLayerType.NORMAL) {

            if (layer.getTypeTool() != null) {
                //Common_Text 图层设置 *********************************************
                //用于装载文字样式信息的string数组 [0]-[3]装载rgba，[4]装载字体大小，
                String[] fontData = new String[]{"1", "0", "0", "0", "0"};
                new PSDParseDev().ParseFont(layer.getEngineData(), fontData);

                if (!Objects.equals(fontData[4], "0")) {
                    //如果解析出来可以获得文字大小则直接转化为文字处理
                    //填充font类型的图层数据数据
                    LayerInfo.InitLayerTextInfo(layer_info, layer, fontData);
                    psdInfo.getAll_tools().get(Common.COMMON_TEXT).put(String.valueOf(layer.getLayerId()), layer_info);
                } else {
                    //如果解析不出文字大小则证明无法识别该字体，转到最后的默认保存为图片设置，
                    saveAsImage(layer, layer_info);
                }

            } else if (layer.getName().contains("bg") || layer.getName().contains("background") || layer.getName().contains("背景")) {
                //Common_Background 图层设置 ****************************************

                //获取RGBA 方法一：
                //Color mycolor = new Color(layer.getImage().getRGB(0,0)); int red = mycolor.getRed();

                //获取RGBA 方法二：
                int color = layer.getImage().getRGB(0, 0);
                int blue = color & 0xff;
                int green = (color & 0xff00) >> 8;
                int red = (color & 0xff0000) >> 16;
                int alpha = (color & 0xff000000) >>> 24;

                //填充background类型的图层数据数据
                LayerInfo.InitLayerBackGroundInfo(layer_info, layer, "rgba(" + red + "," + green + "," + blue + "," +
                        (float) (alpha / 255) + ")");

                //把background类型的信息设置到all_tools里
                psdInfo.getAll_tools().get(COMMON_BACKGROUND).put(String.valueOf(layer.getLayerId()), layer_info);

            } else {
                //Common_Image 图层设置************************************************
                saveAsImage(layer, layer_info);
            }
        }
    }

    /**
     * 保存PSD解析出来的image文件并插入新image组件条目到数据库
     *
     * @param layer      PSD每个图层对象
     * @param layer_info 用来装载所有组件信息的list
     * @throws IOException
     */
    public void saveAsImage(PsdLayer layer, HashMap<String, Object> layer_info) throws IOException {
        //设置该image的文件名，即以时间戳为命名
        String timeStampName = CommonService.getTimeStamp();
        //该image的http请求src
        String src = GlobalConfig.getHttpFileUrl(Common.UPLOAD_FILES) + timeStampName + Common.IMG_PNG;
        //填充image类型的图层数据
        LayerInfo.InitLayerImageInfo(layer_info, layer.getName(), src);
        //把image数据设置进psdInfo中的allTools里
        psdInfo.getAll_tools().get(Common.COMMON_IMAGE).put(String.valueOf(layer.getLayerId()), layer_info);

        //把图层保存到OSS中
        if (layer.getImage() != null) {
            //图层数据输出成byte数据
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(layer.getImage(), IMAGE_PNG_TYPE, byteArrayOutputStream);
            //图层数据写到OSS中
            String ossFileName = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) + timeStampName + Common.IMG_PNG;
            OssOpt.uploadFileToOss(ossFileName,byteArrayOutputStream.toByteArray(),ossClient);

            //保存文件信息到数据库,relative_path设置为null，即默认在该项目目录下的一级目录
            CreateProject.insertWidgetToDB(projectId, userId, Common.COMMON_IMAGE, Common.FILE_TYPE_IMAGE, Common.NOT_FOLDER_FILE, timeStampName,
                    IMAGE_PNG_TYPE, layer.getName(), "", String.valueOf(byteArrayOutputStream.size()), null, CommonService.getDateTime(), sqlSession);
        } else {
            logger.warn("===PSD Analizer-->saveAsImage null error: " + layer.getName());
        }
    }

    /**
     * 导出psd数据
     */
    public PsdInfo exportData() {
        //初始化HashMap数据
        HashMap<String, Object> overall = new HashMap<>();
        overall.put("width", totalWidth);
        overall.put("height", totalHeight);
        overall.put("is_mobile", false); //标识是否是mobile网页，默认是PC网页
        overall.put("scale", false);
        overall.put("bg-color", "rgba(250,0,0,0.04)");
        overall.put("max_id", maxId);
        overall.put("max_rate", maxRate);
        psdInfo.setOverall(overall);
        System.out.println("===After parse PSD, we get: \n\r" + psdInfo.toString());
        return psdInfo;
    }
}