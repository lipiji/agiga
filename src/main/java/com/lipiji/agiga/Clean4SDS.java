package com.lipiji.agiga;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.jhu.agiga.*;


public class Clean4SDS {
    private static Map<String, Integer> badWords = new HashMap<>();
    
    private static void buildBadWords() {
        badWords.put("update#", 0);
        badWords.put("update", 0);
        badWords.put("recasts", 0);
        badWords.put("undated", 0);
        badWords.put("grafs", 0);
        badWords.put("corrects", 0);
        badWords.put("retransmitting", 0);
        badWords.put("dateline", 0);
        badWords.put("writethru", 0);
        badWords.put("recaps", 0);
        badWords.put("incorporates", 0);
        badWords.put("inserts", 0);
        badWords.put("adv##", 0);
        badWords.put("ld-writethru", 0);
        badWords.put("djlfx", 0);
        badWords.put("edits", 0);
        badWords.put("byline", 0);
        badWords.put("repetition", 0);
        badWords.put("background", 0);
        badWords.put("thruout", 0);
        badWords.put("quotes", 0);
        badWords.put("attention", 0);
        badWords.put("ny###", 0);
        badWords.put("overline", 0);
        badWords.put("embargoed", 0);
        badWords.put("ap", 0);
        badWords.put("gmt", 0);
        badWords.put("adds", 0);
        badWords.put("embargo", 0);
        badWords.put("urgent", 0);
        badWords.put("?", 0);
        badWords.put(" i ", 0);
        badWords.put(" : ", 0);
        badWords.put(" - ", 0);
        badWords.put(" by ", 0);
        badWords.put("-lrb-", 0);
        badWords.put("-rrb-", 0);
    }
    
    private static boolean isGood(String hl) {
        if (StringUtils.isBlank(hl)) {
            return false;
        }
        String s[] = StringUtils.split(hl);
        if (s.length <= 3 || s.length > 50) {
            return false;
        }
        for (String bdw : badWords.keySet()) {
            if (hl.contains(bdw)) {
                return false;
            }
        }
        
        return true;
    }
    
    public static void main(String args[]) throws Exception {
        buildBadWords();
        
        
        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);
        
        
        String sources[] = {"AFP", "APW", "CNA", "LTW", "NYT", "WPB", "XIN"};
        //String sources[] = {"APW"};
        
        ExecutorService pool = Executors.newFixedThreadPool(8);
        
        for (final String source : sources) {
            pool.execute(new Runnable() {
                public void run() {
                    try {
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
                                System.out.println(source + ": " + i + " / " + files.length);
                                String year = fname.substring(0, fname.length() - 9);
                                try {
                                    print4Word2Vec(dSource + fname, target + source + "/" + year + "/", prefs);
                                } catch (Exception e) {
                                    System.out.println(source + ":" + fname);
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                        }
                        Thread.sleep(0);
                    } catch (Exception  e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Finished.");
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
    
    private static void print4Word2Vec(String inputFile, String out, AgigaPrefs prefs) throws IOException {
        StreamingDocumentReader reader = new StreamingDocumentReader(inputFile, prefs);
        for (AgigaDocument doc : reader) {
            //System.out.println();
            String hl = getHeadlineText(doc.getHeadline());
            if (!isGood(hl.toLowerCase())) {
                continue;
            }

            StringBuilder text = new StringBuilder();
            for (AgigaSentence sent : doc.getSents()) {
                for (AgigaToken tok : sent.getTokens()) {
                    text.append(tok.getWord() + " ");
                }
                text.append("\n");
            }
           
            String s = text.toString().replaceAll("-LRB-|-RRB-", "");
            FileUtils.writeStringToFile(new File(out + doc.getDocId() +".cont"), s);
            FileUtils.writeStringToFile(new File(out + doc.getDocId() +".head"), hl);
        }
    }
}
