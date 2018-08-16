package viewcoder.operation.impl.company;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import viewcoder.operation.entity.Orders;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/8/15.
 */
public class CompanyOpt {

    private static Logger logger = Logger.getLogger(CompanyOpt.class);

    public static ResponseData getCompanyDiscountOrder(Object msg){
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";

        try {
            //获取
            String companyCredit = FormData.getParam(msg, Common.COMPANY_CREDIT);
            List<Orders> list = sqlSession.selectList(Mapper.GET_COMPANY_DISCOUNT_ORDER, companyCredit);
            Assemble.responseSuccessSetting(responseData, list);

        }catch (Exception e){
            message = "getCompanyDiscountOrder err";
            CompanyOpt.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        }finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }
}
