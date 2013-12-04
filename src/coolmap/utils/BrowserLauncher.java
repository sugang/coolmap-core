/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.utils;

import com.google.common.base.Joiner;
import java.awt.Desktop;
import java.net.URI;

/**
 *
 * @author sugang
 */
public class BrowserLauncher {
    public static final String googleSearchURL = "http://www.google.com/search?q=";
    public static final String googleScholarURL = "http://scholar.google.com/scholar?q=";
    public static final String pubmedURL = "http://www.ncbi.nlm.nih.gov/pubmed/?term=";

    public static void search(String url, Object... terms) {

        if (terms == null || terms.length == 0) {
            return;
        }

        try {
            String term = Joiner.on("+").join(terms);
            
            term = term.replaceAll("\\s+", "+");
            term = term.replaceAll("\"", "");
            
            URI searchURL = new URI(url + term);
            
            Desktop.getDesktop().browse(searchURL);
        } catch (Exception e) {
            
        }
    }    
}
