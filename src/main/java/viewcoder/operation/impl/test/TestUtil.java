package viewcoder.operation.impl.test;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.junit.Test;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.common.CommonService;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/19.
 */
public class TestUtil {

    private static Logger logger = Logger.getLogger(TestUtil.class);

    public static ResponseData getAllOss() {
        ResponseData responseData = new ResponseData();
        List<String> list = new ArrayList<>();
        OSSClient ossClient = OssOpt.initOssClient();
        // 列举文件。 如果不设置KeyPrifex，则列举存储空间下所有的文件。如果设置了KeyPrifex，则列举包含指定前缀的文件。
        ObjectListing objectListing = ossClient.listObjects("viewcoder-bucket", "single_export");
        List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
        for (OSSObjectSummary s : sums) {
            list.add(s.getKey().split("-")[0]);
        }
        Assemble.responseSuccessSetting(responseData, list);
        OssOpt.shutDownOssClient(ossClient);
        return responseData;
    }


    public static ResponseData deleteOssFile(Object msg) {
        ResponseData responseData = new ResponseData();
        OSSClient ossClient = OssOpt.initOssClient();

        try {
            String str = FormData.getParam(msg, "src");
            ossClient.deleteObject("viewcoder-bucket", "single_export/" + str + "-index.html");
            ossClient.deleteObject("viewcoder-bucket", "project_data/" + str + ".txt");
            Assemble.responseSuccessSetting(responseData,null);

        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500, "delete file occurs error");

        } finally {
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }
}








