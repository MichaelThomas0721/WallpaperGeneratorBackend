package com.thomas.WallpaperGenerator;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hibernate.engine.jdbc.StreamUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

@RestController
public class GreetingController {

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/greeting", method = RequestMethod.POST)
    public void getGreeting(@RequestParam MultipartFile file, HttpServletResponse response)
            throws FileNotFoundException, IOException {
        File fileIn = new File("./Frames/test.tmp");

        try (OutputStream os = new FileOutputStream(fileIn)) {
            os.write(file.getBytes());
        }

        GifProcessor gifProcessor = new GifProcessor();
        File[] fileOut = gifProcessor.DivideFile(fileIn);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment;filename=download.zip");
        response.setStatus(HttpServletResponse.SC_OK);

        // List<String> fileNames = service.getFileName();

        try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
            for (File fileC : fileOut) {
                FileSystemResource resource = new FileSystemResource(fileC);

                ZipEntry e = new ZipEntry(resource.getFilename());
                e.setSize(resource.contentLength());
                e.setTime(System.currentTimeMillis());
                zippedOut.putNextEntry(e);

                StreamUtils.copy(resource.getInputStream(), zippedOut);
                zippedOut.closeEntry();
            }
            zippedOut.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * response.setContentType("application/zip");
         * response.setHeader("Content-Disposition",
         * "attachment;filename=\"download.zip\"");
         * response.setStatus(HttpServletResponse.SC_OK);
         * 
         * ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         * BufferedOutputStream bufferedOutputStream = new
         * BufferedOutputStream(byteArrayOutputStream);
         * ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
         * 
         * for (File fileC : fileOut) {
         * zipOutputStream.putNextEntry(new ZipEntry(fileC.getName()));
         * FileInputStream fileInputStream = new FileInputStream(fileC);
         * 
         * IOUtils.copy(fileInputStream, zipOutputStream);
         * 
         * fileInputStream.close();
         * zipOutputStream.closeEntry();
         * }
         * 
         * if (zipOutputStream != null) {
         * zipOutputStream.finish();
         * zipOutputStream.flush();
         * IOUtils.closeQuietly(zipOutputStream);
         * }
         * IOUtils.closeQuietly(bufferedOutputStream);
         * IOUtils.closeQuietly(byteArrayOutputStream);
         * return byteArrayOutputStream.toByteArray();
         */

    }

}
