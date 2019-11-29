package com.futsalud.tool;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Zip4WinGenerator {

    public static int generate(final String zipFileName, final char[] password, ImmutableList<File> fileList) {

        try {
            ZipFile zipFile = new ZipFile(zipFileName);
            zipFile.setFileNameCharset("Windows-31j");
            ZipParameters params = new ZipParameters();
            params.setEncryptFiles(false);
            params.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            params.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            params.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            params.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            if (password.length != 0) {
                params.setEncryptFiles(true);
                params.setPassword(password);
            }

            zipFile.addFiles(new ArrayList<File>(fileList.toList()), params);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

}
