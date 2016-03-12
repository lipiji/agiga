package com.lipiji.agiga;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import edu.jhu.agiga.*;


public class Clean4Word2Vec {
    private static final String chars = "[^0-9a-zA-Z\u4e00-\u9fa5.\\s]+";
    
    public static void main(String args[]) throws Exception {
        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);
        
        
        String sources[] = {"AFP", "APW", "CNA", "LTW", "NYT", "WPB", "XIN"};
        
        try (BufferedWriter out = new BufferedWriter(new FileWriter(target))) {
            for (String source : sources) {
                String dSource = path + source + "/";
                File dir = new File(dSource);
                File files[] = dir.listFiles();
                if (files == null || files.length < 1) {
                    return;
                }
                int i = 0;
                for (File f : files) {
                    String fname = f.getName();
                    if (fname.contains(".xml.gz")) {
                        i++;
                        print4Word2Vec(dSource + fname, out, prefs);
                        System.out.println(source + ": " + i + " / " + files.length);
                    }
                }
            }

        }
        
        
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
    
    private static void print4Word2Vec(String inputFile, BufferedWriter out, AgigaPrefs prefs) throws IOException {
        StreamingDocumentReader reader = new StreamingDocumentReader(inputFile, prefs);
        for (AgigaDocument doc : reader) {
            StringBuilder text = new StringBuilder();
            text.append(getHeadlineText(doc.getHeadline())).append(" ");
            for (AgigaSentence sent : doc.getSents()) {
                for (AgigaToken tok : sent.getTokens()) {
                    text.append(tok.getWord() + " ");
                }
            }
            String s = text.toString().toLowerCase();
            s = s.replaceAll("-lrb-|-rrb-", "");
            s = s.replaceAll(chars, "");
            s = s.replaceAll("\\p{P}", "");
            out.append(s);
        }
    }
}
