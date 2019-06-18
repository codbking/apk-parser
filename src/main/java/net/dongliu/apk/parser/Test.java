package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.MetaData;
import net.dongliu.apk.parser.bean.UseFeature;

import java.io.File;
import java.util.List;

/**
 * @author wulang
 * @version 1.0.1
 * @ClassName Test.java
 * @Description TODO
 * @createTime 2019年04月24日 17:31:00
 */
public class Test {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\wulang\\Desktop\\apktest\\app-release.apk";
        try (ApkFile apkFile = new ApkFile(new File(filePath))) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            System.out.println(apkMeta.getLabel());
            System.out.println(apkMeta.getPackageName());
            System.out.println(apkMeta.getVersionCode());
            for (UseFeature feature : apkMeta.getUsesFeatures()) {
                System.out.println(feature.getName());
            }
            List<MetaData> metaDatas = apkMeta.getMetaDatas();
//            for (MetaData metaData : metaDatas) {
//                System.out.println(metaData.getName()+"="+metaData.getValue());
//            }
        }catch (Exception e){
            e.printStackTrace();
        }




    }
}
