package viewcoder.psd;

import viewcoder.exception.project.PSDAnalysisException;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.operation.impl.project.CreateProject;
import viewcoder.psd.entity.*;
import viewcoder.psd.layer.PsdLayer;
import viewcoder.psd.layer.PsdLayerType;
import viewcoder.psd.parse.PSDParseDev;
import viewcoder.psd.parse.PsdImage;
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

import static viewcoder.tool.common.Common.COMMON_BACKGROUND;

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
    public PsdAnalysis(int projectId, int userId, String projectName, SqlSession sqlSession, OSSClient ossClient) {
        this.projectId = projectId;
        this.userId = userId;
        this.projectName = projectName;
        this.sqlSession = sqlSession;
        this.ossClient = ossClient;
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
            //插入该psd项目的文件夹资源
            //由于查找资源时根据user_id和file_type查找。如file_type为1查不到音频文件。因此，是创建项目-->file_type下的文件夹，不是项目下的文件夹
            CreateProject.insertWidgetToDB(projectId, userId, null, Common.FILE_TYPE_IMAGE, Common.FOLDER_FILE,
                    CommonService.getTimeStamp(), null, projectName, "", String.valueOf(0),
                    null, CommonService.getDateTime(), sqlSession);

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

                    //对字体的height进行修正，Photoshop获取不到正确的字体height。
                    layer_info.put("height", 40);

                    //对字体大于20的文字进行修正，分别对font的size和font的width进行调节，磨平Photoshop和网页之间的视觉差距
                    if ((int) (Float.parseFloat(fontData[4])) > 20) {
                        //重新设置font-size
                        int font_size = (int) (Float.parseFloat(fontData[4]));
                        int font_size_beautify = (int) ((font_size - 20) * 0.7 * 0.7 + 20);
                        layer_info.put("font-size", font_size_beautify);

                        //重新设置font的width
                        int font_width = layer.getWidth();
                        int font_width_beautify = (int) (font_width * 1.3 * 1.3);
                        layer_info.put("width", font_width_beautify);
                    }
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
        LayerInfo.InitLayerImageInfo(layer_info, layer, src);
        //把image数据设置进psdInfo中的allTools里
        psdInfo.getAll_tools().get(Common.COMMON_IMAGE).put(String.valueOf(layer.getLayerId()), layer_info);

        //把图层保存到OSS中
        if (layer.getImage() != null) {
            //图层数据输出成byte数据
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(layer.getImage(), IMAGE_PNG_TYPE, byteArrayOutputStream);
            //图层数据写到OSS中
            String ossFileName = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) + timeStampName + Common.IMG_PNG;
            OssOpt.uploadFileToOss(ossFileName, byteArrayOutputStream.toByteArray(), ossClient);

            //保存文件信息到数据库,relative_path设置为"该项目名称/"，即默认每个项目都会以项目名称为起始位置的图片资源列表
            CreateProject.insertWidgetToDB(projectId, userId, Common.COMMON_IMAGE, Common.FILE_TYPE_IMAGE, Common.NOT_FOLDER_FILE,
                    timeStampName, IMAGE_PNG_TYPE, layer.getName(), projectName + Common.RELATIVE_PATH_SUFFIX,
                    String.valueOf(byteArrayOutputStream.size()), null, CommonService.getDateTime(), sqlSession);
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
        overall.put("bg-color", "rgba(255,255,255,1)");
        overall.put("max_id", maxId);
        overall.put("max_rate", maxRate);
        psdInfo.setOverall(overall);
        System.out.println("===After parse PSD, we get: \n\r" + psdInfo.toString());
        return psdInfo;
    }
}