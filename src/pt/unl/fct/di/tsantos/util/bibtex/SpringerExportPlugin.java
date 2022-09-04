package pt.unl.fct.di.tsantos.util.bibtex;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringerExportPlugin {

    public static String getInformation(URL url) throws IOException {
        try {
            URL exportPage = new URL(url.toString() + "/export-citation/");
            System.out.println(exportPage);
            URLConnection doiConnection = exportPage.openConnection();
            doiConnection.setRequestProperty("User-Agent",
               "Mozilla 5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.11) ");

            BufferedInputStream buffer = new BufferedInputStream(
                    doiConnection.getInputStream());

            StringBuilder builder = new StringBuilder();
            int byteRead;
            while ((byteRead = buffer.read()) != -1) {
                builder.append((char) byteRead);
            }
            buffer.close();

            String source = builder.toString();

            // Find the __VIEWSTATE variable. 
            String viewstate = "";
            Pattern pattern = Pattern.compile(
                    "<input type=\"hidden\" name=\"__VIEWSTATE\" "
                    + "id=\"__VIEWSTATE\" value=\".*\" />", Pattern.UNIX_LINES);
            Matcher matcher = pattern.matcher(source);
            if (matcher.find() == true) {
                viewstate = source.substring(matcher.start() + 64, matcher.end() - 4);
                //System.out.println(viewstate);
            }

            // Find the __EVENTVALIDATION variable. 
            String eventvalidation = "";
            pattern = Pattern.compile(
                    "<input type=\"hidden\" name=\"__EVENTVALIDATION\" "
                    + "id=\"__EVENTVALIDATION\" value=\".*\" />",
                    Pattern.UNIX_LINES);
            matcher = pattern.matcher(source);
            if (matcher.find() == true) {
                eventvalidation = source.substring(matcher.start() + 76, matcher.end() - 4);
                //System.out.println(eventvalidation);
            }

            doiConnection = exportPage.openConnection();
            doiConnection.setRequestProperty("User-Agent",
               "Mozilla 5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.11) ");
            doiConnection.setDoInput(true);
            doiConnection.setDoOutput(true);
            doiConnection.setUseCaches(false);
            String content = "__VIEWSTATE="
                    + URLEncoder.encode(viewstate, "UTF-8")
                    + "&ctl00%24ctl18%24cultureList=en-us"
                    + "&ctl00%24ctl18%24SearchControl%24BasicSearchForTextBox="
                    + "&ctl00%24ctl18%24SearchControl%24BasicAuthorOrEditorTextBox="
                    + "&ctl00%24ctl18%24SearchControl%24BasicPublicationTextBox="
                    + "&ctl00%24ctl18%24SearchControl%24BasicVolumeTextBox="
                    + "&ctl00%24ctl18%24SearchControl%24BasicIssueTextBox="
                    + "&ctl00%24ctl18%24SearchControl%24BasicPageTextBox="
                    + "&ctl00%24ContentPrimary%24ctl00%24ctl00%24Export=AbstractRadioButton"
                    + "&ctl00%24ContentPrimary%24ctl00%24ctl00%24Format=TextRadioButton"
                    + "&ctl00%24ContentPrimary%24ctl00%24ctl00%24CitationManagerDropDownList=BibTex"
                    + "&ctl00%24ContentPrimary%24ctl00%24ctl00%24ExportCitationButton=Export+Citation"
                    + "&__EVENTVALIDATION="
                    + URLEncoder.encode(eventvalidation, "UTF-8");
            //System.out.println(content);

            DataOutputStream printout =
                    new DataOutputStream(doiConnection.getOutputStream());
            printout.writeBytes(content);
            printout.flush();
            printout.close();

            buffer = new BufferedInputStream(
                    doiConnection.getInputStream());

            builder = new StringBuilder();
            while ((byteRead = buffer.read()) != -1) {
                builder.append((char) byteRead);
            }
            buffer.close();

            // Put the source together.
            source = builder.toString();

            return source;
        } catch (MalformedURLException x) {
            IllegalArgumentException y = new IllegalArgumentException();
            y.initCause(x);
            throw y;
        }
    }

    public static String getInformationFromDOI(String doi) throws IOException {
        // Get the page with search results for the DOI.
        URL resolvedPage;
        try {
            if (doi.contains("dx.doi.org")) {
                resolvedPage = new URL(doi);
            } else {
                resolvedPage = new URL("http://dx.doi.org/" + doi);
            }
        } catch (MalformedURLException e) {
            throw e;
        }

        URLConnection doiConnection = resolvedPage.openConnection();
        doiConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (compatible; Googlebot/2.1; "
                + "+http://www.google.com/bot.html)");

        doiConnection.getInputStream(); // force redirection

        URL exportPage = new URL("http://www.springerlink.com/content/"
                + doiConnection.getURL().getPath().split("/")[2]);
        System.out.println(exportPage);
        return getInformation(exportPage);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(
                getInformationFromDOI("10.1007/978-3-540-28644-8_2"));
    }
}
