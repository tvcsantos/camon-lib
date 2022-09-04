/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.pdf;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 *
 * @author tvcsantos
 */
public class PDFUtilities {

    private PDFUtilities() {
        throw new UnsupportedOperationException();
    }

    public static String pdfToText(File file)
            throws IOException {
        PDDocument doc = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(doc);
        doc.close();
        return text;
    }

    public static String pdfToText(File file, int startPage, int endPage)
            throws IOException {
        PDDocument doc = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(startPage);
        stripper.setEndPage(endPage);
        String text = stripper.getText(doc);
        doc.close();
        return text;
    }

    public static String pdfToText(File file, int endPage) throws IOException {
        return pdfToText(file, 1, endPage);
    }
}
