package Others;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Date: 14/03/2010
 * @author Bui Quoc Chinh
 */
public class test {

    /**
     * Regconizing keywords (protein, rel word, preps) and determining whether the input sentence is qualify for extracting relation.
     * input : Sentence with protein name replaced by predefined keyword such
     * as PROTEINx or sentence with tag for protein name.
     * output: Simplified sentence with a list of keywords and relation words
     */
    public test() {
        p = Pattern.compile("PROTEIN\\d{1,2}");
        initRelList("PPIs_tool/Rel_Word.txt");
        initRel_POS("PPIs_tool/Rel_Pos.txt");
        for (String st : preps) {
            prepHash.put(st, st);
        }
        for (String st : to_be) {
            tobeHash.put(st, st);
        }
    }

    /**
     * Init component for input sentence. The following components are initilized:
     * keywords: determine the positions of proteins in the sentence, the number of proteins
     * relation words: indentify the relation words, theirs position in the sentence, number of 
     * @param txt
     * @return
     */
    public String initText(String text) {
        // reset counter values
        rels.clear();
        keycount = 0;
        relcount = 0;
        is_a = 0;
        hasPattern = false;
        text = preprocess(text);
        List<String> prs = getProteins(text);
        keycount = prs.size() ;
        words = text.split(",\\s|;\\s|:\\s|\\s|\\.");
        for (int i = 0; i < words.length; i++) {
            if (relList.containsKey(words[i].toLowerCase())) {
                relcount++;
                rels.add(words[i]); // list of rels
            }else if(tobeHash.containsKey(words[i].toLowerCase())){
                is_a++ ;
            }

        }
        return text;
    }
    
    private int getPIndex(String pname){
        int i= pname.length()-1 ;
        while (Character.isDigit(pname.charAt(i))){
          i--;   
        }
        return Integer.parseInt(pname.substring(i+1));
    }

    private List<String> getProteins(String s){
        List<String> list = new ArrayList<String>();
        m = p.matcher(s);
        while (m.find()) {
            list.add(m.group()); // creating a list of proteins from text ;
        }
        return list ;
    }
    /**
     * Removing text inside parentheses if not containing Proteins
     */
    public String removeComment(String txt) {
        StringBuilder sb = new StringBuilder(txt);
        int i = 0;
        int[] openP = new int[15];
        int index = -1;
        String sub;
        // remove ()
        alterHash.clear();
        List<String> alter = new ArrayList<String>();
        while (i < sb.length()) {
            if (sb.charAt(i) == '(') {
                openP[++index] = i;
            } else {
                if (sb.charAt(i) == ')') {
                    int k = i + 1;
                    if (index >= 0) {
                        sub = sb.substring(openP[index], k);
                        alter= getProteins(sub);
                        if (alter.size()==0) {
                            sb = sb.replace(openP[index], k, "");
                            i = openP[index];
                        }else {
                            // have proteins, now create a list
                            // check whether this list belongs to the protein closed to this list
                            int pidx = getPIndex(alter.get(0));
                            if(pidx >0){
                                String pr1 ="PROTEIN"+(pidx-1);
                                int idx1, idx2 ;
                                idx1 = txt.indexOf(pr1);
                                idx2 = txt.indexOf(sub);
                                if(idx2 - idx1 < 15 && idx2 - idx1 >= 0){
                                    alterHash.put(pr1, alter);
                                }
                            }
                        }
                        index--;
                    }
                }
            }
            i++;
        }
        // remove []
        i = 0;
        openP = new int[15];
        index = -1;
        while (i < sb.length()) {
            if (sb.charAt(i) == '[') {
                openP[++index] = i;
            } else {
                if (sb.charAt(i) == ']') {
                    int k = i + 1;
                    if (index >= 0) {
                        sub = sb.substring(openP[index], k);
                        if (!sub.contains("PROTEIN") && sub.length()>=6) {
                            sb = sb.replace(openP[index], k, "");
                            i = openP[index];
                        }
                        index--;
                    }
                }
            }
            i++;
        }
        return sb.toString().trim();
    }

    public String removeChar(String txt) {
        txt = txt.replaceAll("\\s{2,}", " ");
        txt = txt.replaceAll("^(([A-Z])+\\s){0,}([A-Z])+:\\s", "");
        return txt;
    }

    /**
     * Check whether this sentence is qualify to extract PPI.
     * A sentence is qualify if it contains at least 2 protein and one relation word
     * @return: true 
     */
    public boolean isQualify() {
        return ((keycount >= 2 && relcount >= 1) || (hasPattern && keycount >= 2) || (is_a>0 && keycount>=2));
    }

    // Removing and cleaning text 
    public String preprocess(String txt) {
        pList.clear();
        mc = pt.matcher(txt);
        String s;
        while (mc.find()) {
            s = mc.group(0);
            sl = s.split("-");
            if (sl.length == 2) {
                if (relList.containsKey(sl[1].toLowerCase()) && sl[0].contains("PROTEIN")) {
                    pList.add(sl);
                    hasPattern = true;
                }
            } else {
                if (sl[0].contains("PROTEIN") || relList.containsKey(sl[0].toLowerCase())) {
                    txt = txt.replace(s, sl[0]);
                }
            }
        }
        txt = removeComment(txt);
        txt = removeChar(txt);
        return txt.trim();
    }

    public static void main(String[] r) {
        test sen = new test();
        String txt ="The sterol-independent regulatory element (SIRE) of the PROTEIN0 (PROTEIN1) promoter mediates PROTEIN2-induced transcription of the PROTEIN3-cuttao gene through a cholesterol-independent pathway";
        sen.Test(txt);

    }

    public Hashtable<String, List<String>> getAlterHash(){
        return alterHash ;
    }
    
    public String getPOS(String txt){
        txt = initText(txt);
        String pos ;
        for(String s:rels){
            pos = relPOS.get(s.toLowerCase());
            if(pos!=null){
                txt = txt.replace(" "+s+" ", " "+pos+" ");
            }
        }
        return txt ;
    }
    
    public void Test(String txt) {
        txt = initText(txt);
        System.out.println("Text "+txt);
    }

    public void initRelList(String filename) {
        BufferedReader in = null;
        String[] term = null;
        String txt = "";
        try {
            in = new BufferedReader(new FileReader(filename));
            try {
                while (true) {
                    txt = in.readLine();
                    if (txt == null) {
                        break;
                    } else {
                        if (!relList.containsKey(txt)) {
                            relList.put(txt, txt);
                        }
                    }
                }
            } finally {
                in.close();
            }
        } catch (Exception e) {
            System.out.println("Text: " + txt);
            System.out.println("Length  " + term.length);
            e.printStackTrace();
        }
    }
    public void initRel_POS(String filename) {
        BufferedReader in = null;
        String[] term = null;
        String txt = "";
        try {
            in = new BufferedReader(new FileReader(filename));
            try {
                while (true) {
                    txt = in.readLine();
                    if (txt == null) {
                        break;
                    } else {
                        term = txt.split("_");
                        if (!relPOS.containsKey(term[0])) {
                             relPOS.put(term[0], txt);
                        }
                    }
                }
            } finally {
                in.close();
            }
        } catch (Exception e) {
            System.out.println("Text: " + txt);
            System.out.println("Length  " + term.length);
            e.printStackTrace();
        }
    }
    private String[] words;
    private List<String> rels = new ArrayList<String>();
    public Hashtable<String, String> relList = new Hashtable<String, String>(450);
    public Hashtable<String, String> relPOS = new Hashtable<String, String>(450);
    public final String[] preps = {"of", "with", "to", "between", "and", "or", "by", "for", "through"};
    public Hashtable<String, String> prepHash = new Hashtable<String, String>();
    public Hashtable<String, String> tobeHash = new Hashtable<String, String>();
    public int keycount = 0; // number of keywords
    public int relcount = 0; // number of relations words
    public int is_a =0;
    private Pattern p;
    private Matcher m;
    private Pattern pt = Pattern.compile("(\\w{2,})?(-\\w{2,})|(PROTEIN\\d{1,2}-)|\\(PROTEIN\\d{1,2}\\)-");
    private Matcher mc;
    private String sl[];
    public boolean hasPattern = false;
    public List<String[]> pList = new ArrayList<String[]>(); // store pattern
    public Hashtable<String, List<String>> alterHash = new Hashtable<String, List<String>>();
    public String to_be[] = {"is", "are"};//,"being","be","was","were"};

}