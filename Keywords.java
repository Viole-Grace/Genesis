package Genesis;

import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.Scanner;
import java.util.*;

import java.io.FileInputStream; 
import java.io.InputStream;  

import opennlp.tools.postag.POSModel; 
import opennlp.tools.postag.POSSample; 
import opennlp.tools.postag.POSTaggerME; 
import opennlp.tools.tokenize.WhitespaceTokenizer; 


public class Keywords
{
	static Scanner sc=new Scanner(System.in);
    String language;
    String stopWordsPattern;

    Keywords(String language) 
    {
        this.language = language;
        InputStream stream = this.getClass().getResourceAsStream(language+".txt");
        String line;

        if (stream != null) 
        {
            try 
            {
                ArrayList<String> stopWords = new ArrayList<>();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                while ((line = bufferedReader.readLine()) != null)
                    stopWords.add(line.trim());
                ArrayList<String> regexList = new ArrayList<>();
                for (String word : stopWords) 
                {
                    String regex = "\\b" + word + "(?![\\w-])";
                    regexList.add(regex);
                }
                this.stopWordsPattern = String.join("|", regexList);
            }
            catch (Exception e) 
            {
               throw new Error("An error occurred reading stop words for language " + language);
            }
        }
        else throw new Error("Could not find stop words required for language " + language);

    }
    private String[] getSentences(String text) 
    {
        return text.split("[.!?,;:\\t\\\\\\\\\"\\\\(\\\\)\\\\'\\u2019\\u2013]|\\\\s\\\\-\\\\s");
    }
    private String[] separateWords(String text, int size) 
    {
        String[] split = text.split("[^a-zA-Z0-9_\\\\+/-\\\\]");
        ArrayList<String> words = new ArrayList<>();

        for (String word : split) 
        {
            String current = word.trim().toLowerCase();
            int len = current.length();
            if (len > size && len > 0 && !current.contains("U+"))
                words.add(current);
        }

        return words.toArray(new String[words.size()]);
    }
    private String[] getKeywords(String[] sentences) 
    {
        ArrayList<String> phraseList = new ArrayList<>();

        for (String sentence : sentences) 
        {
            String temp = sentence.trim().replaceAll(this.stopWordsPattern, "|");
            String[] phrases = temp.split("\\|");

            for (String phrase : phrases) 
            {
                phrase = phrase.trim().toLowerCase();

                if (phrase.length() > 0)
                    phraseList.add(phrase);
            }
        }

        return phraseList.toArray(new String[phraseList.size()]);
    }
    private LinkedHashMap<String, Double> calculateWordScores(String[] phrases) 
    {
        LinkedHashMap<String, Integer> wordFrequencies = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> wordDegrees = new LinkedHashMap<>();
        LinkedHashMap<String, Double> wordScores = new LinkedHashMap<>();

        for (String phrase : phrases) 
        {
            String[] words = this.separateWords(phrase, 0);
            int length = words.length;
            int degree = length - 1;

            for (String word : words) 
            {
                wordFrequencies.put(word, wordDegrees.getOrDefault(word, 0) + 1);
                wordDegrees.put(word, wordFrequencies.getOrDefault(word, 0) + degree);
            }
        }

        for (String item : wordFrequencies.keySet()) 
        {
            wordDegrees.put(item, wordDegrees.get(item) + wordFrequencies.get(item));
            wordScores.put(item, wordDegrees.get(item) / (wordFrequencies.get(item) * 1.0));
        }

        return wordScores;
    }
    private LinkedHashMap<String, Double> getCandidateKeywordScores(String[] phrases, LinkedHashMap<String, Double> wordScores) 
    {
        LinkedHashMap<String, Double> keywordCandidates = new LinkedHashMap<>();
        for (String phrase : phrases) 
        {
            double score = 0.0;

            String[] words = this.separateWords(phrase, 0);

            for (String word : words) 
            {
                score += wordScores.get(word);
            }

            keywordCandidates.put(phrase, score);
        }

        return keywordCandidates;
    }
    private LinkedHashMap<String, Double> sortHashMap(LinkedHashMap<String, Double> map) 
    {
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        List<Map.Entry<String, Double>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(list);

        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext(); ) 
        {
            Map.Entry<String, Double> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
    public LinkedHashMap<String, Double> getKeywordsFromText(String text) 
    {
        String[] sentences = this.getSentences(text);
        String[] keywords = this.getKeywords(sentences);

        LinkedHashMap<String, Double> wordScores = this.calculateWordScores(keywords);
        LinkedHashMap<String, Double> keywordCandidates = this.getCandidateKeywordScores(keywords, wordScores);

        return this.sortHashMap(keywordCandidates);
    }
    public String[] Extract(String user_text, String language, String notInTag, String inTag) throws IOException
    {
        LinkedHashMap<String, Double> key_eval = new LinkedHashMap<String, Double>();
        Keywords obj = new Keywords(language); //pass language to the constructor
        key_eval=getKeywordsFromText(user_text);
        String answer[]=new String[key_eval.size()];        
        Set<String> keys = key_eval.keySet();
        int sz=key_eval.size()/5;
        int count=key_eval.size();
        for(String k:keys)
            answer[--count]=k;
        String answers[]=new String[100];
        int getval=0;
        for(int i=0;i<key_eval.size();i++)
        {
            if(answer[i].length() !=1 && i%sz==0)
                {
                    answers[getval++]=answer[i];
                }
        }
        String temp="";
        for(int i=0;i<getval;i++)
        {
            for(int j=0;j<getval-i-1;j++)
            {
                if(answers[j].length()<answers[j+1].length())
                {
                    temp=answers[j];
                    answers[j]=answers[j+1];
                    answers[j+1]=temp;
                }
            }
        }
        System.out.println("\nKeywords with priority list : ");
        for(int i=0;i<getval;i++)
            System.out.print("'"+answers[i]+"' ");
        
        InputStream inputStream = new FileInputStream("en-pos-maxent.bin"); 
        POSModel model = new POSModel(inputStream); 
        POSTaggerME tagger = new POSTaggerME(model);        
        String[] tags = tagger.tag(answer);      
        POSSample sample = new POSSample(answer, tags); 
        String[] x = sample.toString().split(" ");
        ArrayList<String> list = new ArrayList<String>();  

        for(int i=0;i<x.length;i++)
        {
            if(!(x[i].substring(x[i].lastIndexOf("_")+1).contains(notInTag)) && !(x[i].substring(x[i].lastIndexOf("_")+1).contains("NNS")))
            {
                if (x[i].substring(x[i].lastIndexOf("_")+1).contains(inTag)) //|| x[i].substring(x[i].lastIndexOf("_")+1).contains("JJ"))
                {
                    list.add(x[i].split("_")[0]);
                }
            }
        }
        if(list.size()<=6)
            for(int i=0;i<list.size();i++)
            {
                System.out.println(list.get(i)); 
                answers[getval++]=(String)list.get(i);
            }
        else
        {
            //System.out.println("'"+list.get(0)+"' , '"+list.get(1)+"' , '"+list.get(2)+"' , '"+list.get(list.size()/2 -1)+"' , '"+list.get(list.size()/2 +1)+"' , '"+list.get(list.size()-1)+"' , '"+list.get(list.size()-2)+"'");
            answers[getval++]=(String)list.get(0);
            answers[getval++]=(String)list.get(1);
            answers[getval++]=(String)list.get(2);
            answers[getval++]=(String)list.get(list.size()/2 -1);
            answers[getval++]=(String)list.get(list.size()/2 +1);
            answers[getval++]=(String)list.get(list.size()-1);
            answers[getval++]=(String)list.get(list.size()-2);
        }
        return answers;

    }

}