package com.kkl.kklplus.b2b.sf.http.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.sf.entity.SfOrderHandle;
import com.kkl.kklplus.b2b.sf.http.command.OperationCommand;
import com.kkl.kklplus.b2b.sf.http.config.B2BSFProperties;
import com.kkl.kklplus.b2b.sf.http.response.OrderHandleResponse;
import com.kkl.kklplus.b2b.sf.http.response.ResponseBody;
import com.kkl.kklplus.b2b.sf.utils.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.http.entity.ContentType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@Slf4j
public class OkHttpUtils {

    private static OkHttpClient okHttpClient = SpringContextHolder.getBean(OkHttpClient.class);
    private static B2BSFProperties sfProperties = SpringContextHolder.getBean(B2BSFProperties.class);
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static <T> ResponseBody<T> postSyncGenericNew(OperationCommand command,Class<T> dataClass) {
        ResponseBody<T> responseBody = null;
        B2BSFProperties.DataSourceConfig dataSourceConfig = sfProperties.getDataSourceConfig();
        if (dataSourceConfig != null && command != null && command.getOpCode() != null &&
                command.getReqBody() != null && command.getReqBody().getClass().getName().equals(command.getOpCode().reqBodyClass.getName())) {
            String reqbodyJson = gson.toJson(command.getReqBody());
            // 系统参数
            FormBody.Builder param = new FormBody.Builder(Charset.forName("UTF-8"));
            try {
                Long timestamp = System.currentTimeMillis();
                param.add("partnerID", dataSourceConfig.getPartnerID());
                param.add("serviceCode", command.getOpCode().serviceCode);
                param.add("requestID",UUID.randomUUID().toString());
                param.add("msgData", reqbodyJson);
                param.add("timestamp", timestamp.toString());
                //构建签名
                String msgDigest = genDigest(timestamp.toString(),reqbodyJson, dataSourceConfig.getMd5Key());
                param.add("msgDigest", msgDigest);
            }catch (Exception e){
                return new ResponseBody<>(ResponseBody.ErrorCode.DATA_PARSE_FAILURE, e);
            }
            String url = dataSourceConfig.getRequestMainUrl();
            Request request = new Request.Builder()
                    .url(url)
                    .post(param.build())
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBodyJson = response.body().string();
                        try {
                            responseBody = new ResponseBody<>();
                            responseBody.setOriginalJson(responseBodyJson);
                            T data = gson.fromJson(responseBodyJson, dataClass);
                            responseBody.setData(data);
                        } catch (Exception e) {
                            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.JSON_PARSE_FAILURE, e);
                            responseBody.setOriginalJson(responseBodyJson);
                            return responseBody;
                        }
                    } else {
                        responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_RESPONSE_BODY_ERROR);
                    }
                } else {
                    responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_STATUS_CODE_ERROR);
                }
            } catch (Exception e) {
                return new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE, e);
            }
        } else {
            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_PARAMETER_FORMAT_ERROR);
        }

        return responseBody;
    }

    public static String genDigest(String timestamp, String mgsData, String md5key) throws Exception {
        //将业务报文+时间戳+秘钥组合成需加密的字符串(注意顺序)
        String toVerifyText = mgsData + timestamp + md5key;
        //因业务报文中可能包含加号、空格等特殊字符，需要urlEnCode处理
        toVerifyText = URLEncoder.encode(toVerifyText, "UTF-8");
        //进行Md5加密
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(toVerifyText.getBytes("UTF-8"));
        byte[] md = md5.digest();
        //通过BASE64生成数字签名
        String msgDigest = new String(new BASE64Encoder().encode(md));
        return msgDigest;
    }
    public static String getDigest(Integer imageNumber, Integer imageType, String taskCode,String waybillNo) throws Exception {
        //将业务报文+时间戳+秘钥组合成需加密的字符串(注意顺序)
        String toVerifyText = new StringBuilder().
                append(sfProperties.getDataSourceConfig().getClientCode()).
                append(Integer.valueOf(imageNumber)).
                append(imageType).
                append(waybillNo).
                append(taskCode).
                append(sfProperties.getDataSourceConfig().getMd5Key()).toString();
        //因业务报文中可能包含加号、空格等特殊字符，需要urlEnCode处理
        toVerifyText = URLEncoder.encode(toVerifyText, "UTF-8");
        //进行Md5加密
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(toVerifyText.getBytes("UTF-8"));
        byte[] md = md5.digest();
        //通过BASE64生成数字签名
        String msgDigest = new String(new BASE64Encoder().encode(md));
        return msgDigest;
    }

    /**
     * 拉图片
     * @param sfOrderHandle
     * @param url
     * @return
     */
    public static SfOrderHandle getPics(SfOrderHandle sfOrderHandle ,String url){
        FormBody.Builder param = new FormBody.Builder(Charset.forName("UTF-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(param.build())
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    String picture = response.body().string();
                    try {
                        List<String> list = gson.fromJson(picture,new TypeToken<List<String>>(){}.getType());
                        sfOrderHandle.setPics(list);
                    }catch (Exception e){
                        sfOrderHandle.setPics(null);
                    }
                }
            }
        }catch (Exception e){
                sfOrderHandle.setPics(null);
        }
        return sfOrderHandle;
    }


    /**
     * url转化成MultipartFile
     * @param url
     * @return
     */
    public static MultipartFile getRequestFile(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        MultipartFile multipartFile = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    InputStream ins = new ByteArrayInputStream(response.body().bytes());
                    String fileName = url.trim().substring(url.lastIndexOf("/")+1);
                    multipartFile = new MockMultipartFile(fileName, fileName,
                            ContentType.APPLICATION_OCTET_STREAM.toString(), ins);
//                    File file =  inputStreamToFile(ins,UUID.randomUUID().toString());
//                    multipartFile = fileToMultipartFile(file);
                }
            }
        }catch (Exception e){
          log.error("请求错误图片:{}",url,e);
        }
        return multipartFile;
    }


    public static File inputStreamToFile(InputStream ins, String name) throws Exception{

        File file = new File(name);

        OutputStream os = new FileOutputStream(file);
        int bytesRead;
        int len = 1024;
        byte[] buffer = new byte[len];
        while ((bytesRead = ins.read(buffer, 0, len)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();
        return file;
    }

    public static MultipartFile fileToMultipartFile(File file) {
        FileInputStream inputStream = null;
        MultipartFile multipartFile = null;
        try {
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                    ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        }catch (Exception e){

        }
        return multipartFile;
    }




}
