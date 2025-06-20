package io.dataease.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ZipUtils {


    /**
     * 解压文件
     *
     * @param zipFilePath  解压文件路径
     * @param outputFolder 输出解压文件路径
     */
    public static void unZipIt(String zipFilePath, String outputFolder) {
        byte[] buffer = new byte[1024];

        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {
            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = protectZipSlip(fileName, outputFolder);
                //大部分网络上的源码，这里没有判断子目录
                if (ze.isDirectory()) {
                    if (!newFile.mkdirs()) {
                    }
                } else {
                    if (!new File(newFile.getParent()).mkdirs()) {
                    }
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unzip(File source, String out) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(source));
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            File file = protectZipSlip(entry.getName(), out);

            if (entry.isDirectory()) {
                if (!file.mkdirs()) {
                }
            } else {
                File parent = file.getParentFile();

                if (!parent.exists()) {
                    if (!parent.mkdirs()) {
                    }
                }

                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {

                    byte[] buffer = new byte[Math.toIntExact(entry.getSize())];

                    int location;

                    while ((location = zis.read(buffer)) != -1) {
                        bos.write(buffer, 0, location);
                    }
                }
            }
            entry = zis.getNextEntry();
        }
    }

    /**
     * 把所有文件都直接解压到指定目录(忽略子文件夹)
     *
     * @param zipFile
     * @param folderPath
     * @throws ZipException
     * @throws IOException
     */
    public static void upZipFile(File zipFile, String folderPath) throws ZipException, IOException {
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            if (!desDir.mkdirs()) {
            }
        }
        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            InputStream in = zf.getInputStream(entry);
            File desFile = new File(folderPath, java.net.URLEncoder.encode(entry.getName(), "UTF-8"));

            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    if (!fileParentDir.mkdirs()) {
                    }
                }
            }

            OutputStream out = new FileOutputStream(desFile);
            byte[] buffer = new byte[1024 * 1024];
            int realLength = in.read(buffer);
            while (realLength != -1) {
                out.write(buffer, 0, realLength);
                realLength = in.read(buffer);
            }

            out.close();
            in.close();

        }
    }
    public static File protectZipSlip(String fileName, String destDir) throws IOException{
        Path destPath = Paths.get(destDir);
        Path resolvedDest = destPath.resolve(fileName);
        Path normalizedPath = resolvedDest.normalize();

        // checking whether zipEntry filename has changed the destination
        if (!normalizedPath.startsWith(destDir)) {
            throw new IOException("Malicious zip entry found: " + fileName);
        }

        File newFile = normalizedPath.toFile();
        return newFile;
    }


    /**
     * @Title: compress
     * @Description: TODO
     * @param filePaths 需要压缩的文件地址列表（绝对路径）
     * @param zipFilePath 需要压缩到哪个zip文件路径（无需创建这样一个zip，只需要指定一个全路径）
     * @return int   压缩成功的文件个数
     */
    public static int compress(List<String> filePaths, String zipFilePath) {
        int fileCount = 0;//记录压缩了几个文件
        try {
            byte[] buf = new byte[1024];
            File zipFile = new File(zipFilePath);
            if(!zipFile.exists()){
                zipFile.createNewFile();//zip文件不存在，则创建文件，用于压缩
            }

            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for(int i = 0; i < filePaths.size(); i++){
                String relativePath = filePaths.get(i);
                if(StringUtils.isEmpty(relativePath)){
                    continue;
                }
                File sourceFile = new File(relativePath);//绝对路径找到file
                if(sourceFile == null || !sourceFile.exists()){
                    continue;
                }

                FileInputStream fis = new FileInputStream(sourceFile);

                zos.putNextEntry(new ZipEntry(sourceFile.getName()));//压缩文件

                int len;
                while((len = fis.read(buf)) > 0){
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                fis.close();
                fileCount++;
            }
            zos.close();
            System.out.println("压缩完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileCount;
    }

    public static boolean checkPath(String path){
        try {
            File filePath = new File(path);
            if(!filePath.exists() || !filePath.isDirectory() ){
                boolean flag = filePath.mkdirs();
                return flag;
            }else {
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


}
