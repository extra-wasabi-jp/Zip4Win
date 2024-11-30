package com.futsalud.tool;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.eclipse.collections.api.list.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class Zip4WinGenerator {

    public static int generate(
            final String zipFileName,
            final boolean isCheckedForWin,
            final char[] password,
            ImmutableList<File> fileList
    ) {

        try (final ZipFile zipFile = (password.length != 0)
                ? new ZipFile(zipFileName, password)
                : new ZipFile(zipFileName)) {
            if (isCheckedForWin) {
                zipFile.setCharset(Charset.forName("MS932"));
            }
            ZipParameters params = new ZipParameters();
            params.setFileNameInZip(zipFileName);
            params.setEncryptFiles(false);
            params.setCompressionMethod(CompressionMethod.DEFLATE);
            params.setCompressionLevel(CompressionLevel.NORMAL);
            params.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            params.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
            if (password.length != 0) {
                params.setEncryptFiles(true);
            }
            for (final File file : fileList) {
                params.setFileNameInZip(file.getName());
                zipFile.addFile(file, params);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

}
