package com.lipiji.agiga;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import edu.jhu.agiga.*;


public class Clean4Word2Vec {

    public static void main(String args[]) throws Exception {
        Util.initializeLogging();
        String inputFile = "/misc/projdata12/info_fil/pjli/data/LDC2012T21/XIN/xin_eng_199501.xml.gz";
        print4Word2Vec(inputFile);
        
    }
    
    private static String getHeadlineText(String tree) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isBlank(tree)) {
            return "";
        }
        tree = tree.replaceAll("\\)", "");
        String ws[] = StringUtils.split(tree);
        for (String w : ws) {
            if (!w.contains("(")) {
                sb.append(w + " ");
            }
        }
        return sb.toString();
    }
    
    private static void print4Word2Vec(String inputFile) throws IOException {
        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);
        StreamingDocumentReader reader = new StreamingDocumentReader(inputFile, prefs);
        for (AgigaDocument doc : reader) {
            System.out.println(getHeadlineText(doc.getHeadline()));
            for (AgigaSentence sent : doc.getSents()) {
                for (AgigaToken tok : sent.getTokens()) {
                    System.out.print(tok.getWord() + " ");
                }
                System.out.println();
            }
            System.out.println("============");
        }
    }
}
